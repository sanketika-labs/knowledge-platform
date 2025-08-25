package org.sunbird.content.competency.mgr.validator

import org.sunbird.graph.dac.model.Node
import org.sunbird.common.exception.ClientException
import org.sunbird.graph.OntologyEngineContext

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

import org.slf4j.{Logger, LoggerFactory}
import com.google.gson.Gson

class CompetencyFramework extends CompetencyValidator {

    private val gson = new Gson()
    val logger: Logger =
        LoggerFactory.getLogger("org.sunbird.content.competency.mgr.validator.CompetencyValidator")

    override def validate(node: Node)(implicit oec: OntologyEngineContext, ec: ExecutionContext): Unit = {
        val errors = ListBuffer[String]()
        val metadata = node.getMetadata.asScala.toMap

        metadata.get("sector").foreach {
            case m: java.util.Map[_, _] =>
                val sector = m.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap
                validateSector(sector, errors)
            case s: String =>
                val sector = gson.fromJson(s, classOf[java.util.Map[String, AnyRef]]).asScala.toMap
                validateSector(sector, errors)
            case other =>
                logger.warn(s"[COMPETENCY-FRAMEWORK] sector unknown type: ${other.getClass} => $other")
        }

        if (errors.nonEmpty) {
            throw new ClientException("ERR_COMPETENCY_FRAMEWORK", "Competency Framework: " + errors.mkString("; "))
        }
    }

    private def validateSector(sector: Map[String, AnyRef], errors: ListBuffer[String]): Unit = {
        val name   = sector.getOrElse("name", "").toString
        val domain = sector.getOrElse("domain", "").toString

        if (name.trim.isEmpty)
            errors += "sector.name is required"

        if (domain.trim.isEmpty)
            errors += "sector.domain is required"
    }
}
