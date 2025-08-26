package org.sunbird.content.competency.mgr

import org.sunbird.content.competency.mgr.validator.{CompetencyFramework, CompetencyValidator, CompetencyLevel}
import org.sunbird.content.competency.mgr.constants.CompetencyConstants._
import org.sunbird.common.dto.Request
import org.sunbird.graph.OntologyEngineContext
import org.sunbird.graph.nodes.DataNode
import org.sunbird.graph.dac.model.Node

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import org.apache.commons.lang3.StringUtils

object CompetencyManager {
    private val defaultValidator = new CompetencyFramework
    private val validators: Map[String, CompetencyValidator] = Map(
        COMPETENCY_FRAMEWORK -> new CompetencyFramework,
        COMPETENCY_LEVEL     -> new CompetencyLevel
    )

    def getValidator(primaryCategory: String): CompetencyValidator = {
        validators.getOrElse(primaryCategory, defaultValidator)
    }

    //Validate that the given courseId exists and is of contentType=Course and status=Live.
    def validateCourseExists(courseId: String, fieldName: String, errors: ListBuffer[String])
                            (implicit oec: OntologyEngineContext, ec: ExecutionContext, parentNode: Node): Future[Unit] = {
        if (StringUtils.isBlank(courseId)) {
            errors += s"$fieldName courseId is missing"
            Future.successful(())
        } else {
            val request = new Request()
            if (request.getContext == null) {
                request.setContext(new java.util.HashMap[String, AnyRef]())
            }

            // Copy metadata from parent node (Competency Level)
            val parentMetadata = parentNode.getMetadata
            val graphId        = parentNode.getGraphId.toLowerCase()
            val channel        = parentMetadata.getOrDefault("channel", "").toString.toLowerCase()
            val objectType     = parentMetadata.getOrDefault("objectType", "").toString.toLowerCase()

            request.getContext.put("identifier", courseId)
            request.getContext.put("graph_id", graphId)
            request.getContext.put("channel", channel)
            request.getContext.put("schemaName", objectType)
            // Hardcoding version to 1.0 (course may not match competency level version)
            request.getContext.put("version", "1.0")
            request.getContext.put("objectType", objectType)

            request.put("identifier", courseId)
            request.put("objectType", objectType)

            DataNode.read(request).map { node =>
                if (node == null) {
                    errors += s"$fieldName courseId $courseId not found"
                } else {
                    val metadata    = node.getMetadata
                    val status      = metadata.getOrDefault("status", "").toString
                    val contentType = metadata.getOrDefault("contentType", "").toString

                    if (!"Course".equalsIgnoreCase(contentType)) {
                        errors += s"$fieldName courseId $courseId has invalid contentType: $contentType (expected Course)"
                    } else if (!"Live".equalsIgnoreCase(status)) {
                        errors += s"$fieldName courseId $courseId has invalid status: $status (expected Live)"
                    }
                }
            }
        }
    }
}