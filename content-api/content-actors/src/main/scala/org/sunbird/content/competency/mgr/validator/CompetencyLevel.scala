package org.sunbird.content.competency.mgr.validator

import org.sunbird.content.competency.mgr.constants.CompetencyConstants._
import org.sunbird.content.competency.mgr.constants.CompetencyErrorMessages._
import org.sunbird.common.exception.ClientException
import org.sunbird.graph.dac.model.Node

import scala.collection.JavaConverters._
import com.google.gson.Gson
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.mutable.ListBuffer

class CompetencyLevel extends CompetencyValidator {

    private val gson = new Gson()
    val logger: Logger = LoggerFactory.getLogger("org.sunbird.content.competency.mgr.validator.CompetencyValidator")

    override def validate(node: Node): Unit = {
        val errors = ListBuffer[String]()
        val metadata = node.getMetadata.asScala.toMap

        metadata.get("timeLimit").foreach {
            case tl: java.util.Map[_, _] =>
                validateTimeLimit(tl.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap, errors)
            case s: String =>
                validateTimeLimit(gson.fromJson(s, classOf[java.util.Map[String, AnyRef]]).asScala.toMap, errors)
            case other =>
                logger.warn(s"[COMPETENCY-FRAMEWORK] timeLimit unknown type: ${other.getClass} => $other")
        }

        metadata.get("entranceExam").foreach {
            case ee: java.util.Map[_, _] =>
                validateEntranceExam(ee.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap, errors)
            case s: String =>
                validateEntranceExam(gson.fromJson(s, classOf[java.util.Map[String, AnyRef]]).asScala.toMap, errors)
            case other =>
                logger.warn(s"[COMPETENCY-FRAMEWORK] entranceExam unknown type: ${other.getClass} => $other")
        }

        metadata.get("levelExam").foreach {
            case le: java.util.Map[_, _] =>
                validateLevelExam(le.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap, metadata, errors)
            case s: String =>
                validateLevelExam(gson.fromJson(s, classOf[java.util.Map[String, AnyRef]]).asScala.toMap, metadata, errors)
            case other =>
                logger.warn(s"[COMPETENCY-FRAMEWORK] levelExam unknown type: ${other.getClass} => $other")
        }

        if (errors.nonEmpty) {
            throw new ClientException("ERR_COMPETENCY_FRAMEWORK", "Competency Level: " + errors.mkString("; "))
        }
    }

    private def validateTimeLimit(timeLimit: Map[String, AnyRef], errors: ListBuffer[String]): Unit = {
        if (timeLimit.getOrElse(TIME_LIMIT_ENABLED, TIME_LIMIT_NO).toString == TIME_LIMIT_YES) {
            val duration: Map[String, AnyRef] = timeLimit.get(TIME_LIMIT_DURATION).collect {
                case d: java.util.Map[_, _] => d.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap
            }.getOrElse(Map.empty[String, AnyRef])

            if (!duration.contains(TIME_LIMIT_VALUE) || duration(TIME_LIMIT_VALUE) == null || duration(TIME_LIMIT_VALUE).toString.trim.isEmpty) {
                errors += s"timeLimit.duration.$TIME_LIMIT_VALUE is required"
            }

            if (!duration.contains(TIME_LIMIT_UNIT) || duration(TIME_LIMIT_UNIT) == null || duration(TIME_LIMIT_UNIT).toString.trim.isEmpty) {
                errors += s"timeLimit.duration.$TIME_LIMIT_UNIT is required"
            }
        }
    }

    private def validateLevelExam(exam: Map[String, AnyRef], metadata: Map[String, AnyRef], errors: ListBuffer[String]): Unit = {
        if (!exam.contains(LEVEL_EXAM_COURSE_ID)) {
            errors += missingLevelExamCourseId()
        }
    }

    private def validateEntranceExam(exam: Map[String, AnyRef], errors: ListBuffer[String]): Unit = {
        val enabled = exam.getOrElse(ENTRANCE_EXAM_ENABLED, "No").toString
        if (enabled == "Yes") {
            val courseId = exam.getOrElse(ENTRANCE_EXAM_COURSE_ID, "").toString
            if (courseId.trim.isEmpty) {
                errors += missingEntranceExamCourseId()
            }
        }
    }
}
