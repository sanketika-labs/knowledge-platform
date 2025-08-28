package org.sunbird.content.competency.mgr

import org.sunbird.content.competency.mgr.validator.{CompetencyFramework, CompetencyValidator, CompetencyLevel}
import org.sunbird.content.competency.mgr.constants.CompetencyConstants._
import org.sunbird.common.dto.Request
import org.sunbird.graph.OntologyEngineContext
import org.sunbird.graph.nodes.DataNode
import org.sunbird.graph.dac.model.Node

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._
import org.apache.commons.lang3.StringUtils
import org.sunbird.common.exception.ClientException

object CompetencyManager {
    private val validators: Map[String, CompetencyValidator] = Map(
        COMPETENCY_FRAMEWORK -> new CompetencyFramework,
        COMPETENCY_LEVEL     -> new CompetencyLevel
    )

    def getValidator(primaryCategory: String): Option[CompetencyValidator] = {
        Option(primaryCategory).filter(_.nonEmpty).flatMap(validators.get)
    }

    def validateNode(node: Node)(implicit oec: OntologyEngineContext, ec: ExecutionContext): Future[Unit] = {
        val metadata = node.getMetadata.asScala
        val primaryCategory = metadata.getOrElse("primaryCategory", "").toString

        getValidator(primaryCategory) match {
            case Some(validator) => validator.validate(node).map(Some(_))
            case None => Future.successful(None)
        }
    }

    // Validate that the given courseId exists and is of contentType=Course and status=Live.
    def validateCourseExists(courseId: String, fieldName: String, errors: ListBuffer[String])
                            (implicit oec: OntologyEngineContext, ec: ExecutionContext, parentNode: Node): Future[Unit] = {
        if (StringUtils.isBlank(courseId)) {
            val msg = s"$fieldName courseId is missing"
            errors += msg
            Future.failed(new ClientException("ERR_BLANK_COURSE_ID", msg))
        } else {
            val request = new Request()
            Option(request.getContext).getOrElse {
                val context = new java.util.HashMap[String, AnyRef]()
                request.setContext(context)
                context
            }

            val parentMetadata = parentNode.getMetadata.asScala
            val graphId    = parentMetadata.getOrElse("graphId", parentNode.getGraphId).toString.toLowerCase
            val channel    = parentMetadata.getOrElse("channel", "").toString.toLowerCase
            val objectType = parentMetadata.getOrElse("objectType", "").toString.toLowerCase

            val contextMap = Map[String, AnyRef](
                "identifier" -> courseId,
                "graph_id"   -> graphId,
                "channel"    -> channel,
                "schemaName" -> objectType,
                "version"    -> "1.0",
                "objectType" -> objectType
            )
            contextMap.foreach { case (k,v) => request.getContext.put(k,v) }
            request.put("identifier", courseId)
            request.put("objectType", objectType)

            DataNode.read(request).flatMap { node =>
                if (node == null) {
                    val msg = s"$fieldName courseId $courseId not found"
                    errors += msg
                    Future.failed(new ClientException("ERR_COURSE_NOT_FOUND", msg))
                } else {
                    val metadata    = node.getMetadata.asScala
                    val status      = metadata.getOrElse("status", "").toString
                    val contentType = metadata.getOrElse("contentType", "").toString

                    if (!"Course".equalsIgnoreCase(contentType)) {
                        val msg = s"$fieldName courseId $courseId has invalid contentType: $contentType (expected Course)"
                        errors += msg
                        Future.failed(new ClientException("ERR_INVALID_CONTENT_TYPE", msg))
                    } else if (!"Live".equalsIgnoreCase(status)) {
                        val msg = s"$fieldName courseId $courseId has invalid status: $status (expected Live)"
                        errors += msg
                        Future.failed(new ClientException("ERR_INVALID_STATUS", msg))
                    } else {
                        Future.unit
                    }
                }
            }
        }
    }
}