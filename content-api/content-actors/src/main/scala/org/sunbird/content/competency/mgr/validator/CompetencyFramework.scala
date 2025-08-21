package org.sunbird.content.competency.mgr.validator

import org.sunbird.graph.dac.model.Node
import org.sunbird.content.competency.mgr.constants.CompetencyConstants._
import org.sunbird.content.competency.mgr.constants.CompetencyErrorMessages._
import org.sunbird.common.exception.ClientException

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import org.slf4j.{Logger, LoggerFactory}
import com.google.gson.Gson

class CompetencyFramework extends CompetencyValidator {

    private val gson = new Gson()
    val logger: Logger = LoggerFactory.getLogger("org.sunbird.content.competency.mgr.validator.CompetencyValidator")

    override def validate(node: Node): Unit = {
        val errors = ListBuffer[String]()
        val metadata = node.getMetadata.asScala.toMap

        REQUIRED_COMPETENCY_FRAMEWORK_FIELDS.foreach { field =>
            if (!metadata.contains(field) || metadata(field) == null || metadata(field).toString.trim.isEmpty) {
                errors += s"Missing or empty required field: $field"
            }
        }

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

        metadata.get("signupBy").foreach { value =>
            val signupBy = value.toString
            if (!VALID_SIGNUP_BY.contains(signupBy))
                errors += invalidSignupBy(signupBy)
        }

        metadata.get("enrollmentType").foreach { value =>
            val enrollmentType = value.toString
            if (!VALID_ENROLLMENT_TYPES.contains(enrollmentType))
                errors += invalidEnrollmentType(enrollmentType)
        }

        if (errors.nonEmpty) {
            throw new ClientException("ERR_COMPETENCY_FRAMEWORK_REVIEW", "Competency Framework: " + errors.mkString("; "))
        } else Right(())
    }

    private def validateSector(sector: Map[String, AnyRef], errors: ListBuffer[String]): Unit = {
        val name   = sector.getOrElse("name", "").toString
        val domain = sector.getOrElse("domain", "").toString

        if (!VALID_SECTORS.contains(name))
            errors += invalidSectorName(name)

        if (!VALID_DOMAINS.contains(domain))
            errors += invalidSectorDomain(domain)
    }
}