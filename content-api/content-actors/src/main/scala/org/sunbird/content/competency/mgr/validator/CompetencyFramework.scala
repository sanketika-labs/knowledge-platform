package org.sunbird.content.competency.mgr.validator

import org.sunbird.graph.dac.model.Node
import org.sunbird.common.exception.ClientException
import org.sunbird.graph.OntologyEngineContext
import org.sunbird.common.JsonUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.{Try, Success, Failure}

import org.slf4j.{Logger, LoggerFactory}

class CompetencyFramework extends CompetencyValidator {

    val logger: Logger =
        LoggerFactory.getLogger("org.sunbird.content.competency.mgr.validator.CompetencyValidator")

    override def validate(node: Node)(implicit oec: OntologyEngineContext, ec: ExecutionContext): Future[Unit] = {
        val errors = ListBuffer[String]()
        val metadata = node.getMetadata.asScala

        val sector = metadata.getOrElse("sector", null)
        if (sector != null) {
            sector match {
                case m: java.util.Map[_, _] =>
                    val sectorMap = m.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap
                    validateSector(sectorMap, errors)
                case s: String =>
                    Try(JsonUtils.deserialize(s, classOf[java.util.Map[String, AnyRef]])) match {
                        case Success(sectorMap) =>
                            val sectorMapScala = sectorMap.asScala.toMap
                            validateSector(sectorMapScala, errors)
                        case Failure(ex) =>
                            logger.error(s"[COMPETENCY-FRAMEWORK] Failed to parse sector JSON: $s", ex)
                            errors += "Invalid sector JSON format"
                    }
                case other =>
                    logger.warn(s"[COMPETENCY-FRAMEWORK] sector unknown type: ${other.getClass} => $other")
                    errors += s"Invalid sector type: ${other.getClass.getSimpleName}"
            }
        }

        if (errors.nonEmpty) {
            Future.failed(new ClientException("ERR_COMPETENCY_FRAMEWORK", "Competency Framework: " + errors.mkString("; ")))
        } else {
            Future.unit
        }
    }

    private def validateSector(sector: Map[String, AnyRef], errors: ListBuffer[String]): Unit = {
        val name = sector.getOrElse("name", "").toString.trim
        val domain = sector.getOrElse("domain", "").toString.trim

        if (name.isEmpty)
            errors += "sector.name is required"

        if (domain.isEmpty)
            errors += "sector.domain is required"
    }
}