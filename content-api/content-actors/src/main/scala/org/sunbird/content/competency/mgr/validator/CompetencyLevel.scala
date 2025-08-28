package org.sunbird.content.competency.mgr.validator

import org.sunbird.content.competency.mgr.constants.CompetencyConstants._
import org.sunbird.content.competency.mgr.constants.CompetencyErrorMessages._
import org.sunbird.content.competency.mgr.CompetencyManager.validateCourseExists
import org.sunbird.common.exception.ClientException
import org.sunbird.common.JsonUtils
import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.OntologyEngineContext

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}
import org.slf4j.{Logger, LoggerFactory}

class CompetencyLevel extends CompetencyValidator {

    val logger: Logger =
        LoggerFactory.getLogger("org.sunbird.content.competency.mgr.validator.CompetencyValidator")

    override def validate(node: Node)(implicit oec: OntologyEngineContext, ec: ExecutionContext): Future[Unit] = {
        implicit val parentNode: Node = node
        val errors = ListBuffer[String]()
        val metadata = node.getMetadata.asScala

        val validationFutures = ListBuffer[Future[Unit]]()
        val timeLimit = metadata.getOrElse("timeLimit", null)
        if (timeLimit != null) {
            timeLimit match {
                case tl: java.util.Map[_, _] =>
                    validateTimeLimit(tl.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap, errors)
                case s: String =>
                    parseJsonToMap(s, "timeLimit").foreach { timeLimitMap =>
                        validateTimeLimit(timeLimitMap, errors)
                    }
                case other =>
                    logger.warn(s"[COMPETENCY-LEVEL] timeLimit unknown type: ${other.getClass} => $other")
                    errors += s"Invalid timeLimit type: ${other.getClass.getSimpleName}"
            }
        }

        val entranceExam = metadata.getOrElse("entranceExam", null)
        if (entranceExam != null) {
            entranceExam match {
                case ee: java.util.Map[_, _] =>
                    val entranceExamFuture = validateEntranceExam(
                        ee.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap,
                        errors
                    )
                    validationFutures += entranceExamFuture
                case s: String =>
                    parseJsonToMap(s, "entranceExam").foreach { entranceExamMap =>
                        val entranceExamFuture = validateEntranceExam(entranceExamMap, errors)
                        validationFutures += entranceExamFuture
                    }
                case other =>
                    logger.warn(s"[COMPETENCY-LEVEL] entranceExam unknown type: ${other.getClass} => $other")
                    errors += s"Invalid entranceExam type: ${other.getClass.getSimpleName}"
            }
        }

        val levelExam = metadata.getOrElse("levelExam", null)
        if (levelExam != null) {
            levelExam match {
                case le: java.util.Map[_, _] =>
                    val levelExamFuture = validateLevelExam(
                        le.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap,
                        metadata.toMap,
                        errors
                    )
                    validationFutures += levelExamFuture
                case s: String =>
                    parseJsonToMap(s, "levelExam").foreach { levelExamMap =>
                        val levelExamFuture = validateLevelExam(levelExamMap, metadata.toMap, errors)
                        validationFutures += levelExamFuture
                    }
                case other =>
                    logger.warn(s"[COMPETENCY-LEVEL] levelExam unknown type: ${other.getClass} => $other")
                    errors += s"Invalid levelExam type: ${other.getClass.getSimpleName}"
            }
        }

        if (errors.nonEmpty) {
            Future.failed(
                new ClientException("ERR_COMPETENCY_LEVEL", "Competency Level: " + errors.mkString("; "))
            )
        } else if (validationFutures.nonEmpty) {
            Future.sequence(validationFutures).flatMap { _ =>
                if (errors.nonEmpty) {
                    Future.failed(
                        new ClientException("ERR_COMPETENCY_LEVEL", "Competency Level: " + errors.mkString("; "))
                    )
                } else {
                    Future.unit
                }
            }
        } else {
            Future.unit
        }
    }
    private def parseJsonToMap(jsonString: String, fieldName: String): Option[Map[String, AnyRef]] = {
        Try(JsonUtils.deserialize(jsonString, classOf[java.util.Map[String, AnyRef]])) match {
            case Success(javaMap) => Some(javaMap.asScala.toMap)
            case Failure(ex) =>
                logger.error(s"[COMPETENCY-LEVEL] Failed to parse $fieldName JSON: $jsonString", ex)
                None
        }
    }

    private def validateTimeLimit(timeLimit: Map[String, AnyRef], errors: ListBuffer[String]): Unit = {
        val enabled = timeLimit.getOrElse(TIME_LIMIT_ENABLED, TIME_NO).toString
        if (enabled == TIME_YES) {
            val duration: Map[String, AnyRef] = timeLimit.get(TIME_LIMIT_DURATION).collect {
                case d: java.util.Map[_, _] =>
                    d.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap
            }.getOrElse(Map.empty[String, AnyRef])

            val value = duration.getOrElse(TIME_LIMIT_VALUE, "").toString.trim
            val unit = duration.getOrElse(TIME_LIMIT_UNIT, "").toString.trim

            if (value.isEmpty) {
                errors += s"timeLimit.duration.$TIME_LIMIT_VALUE is required"
            }

            if (unit.isEmpty) {
                errors += s"timeLimit.duration.$TIME_LIMIT_UNIT is required"
            }
        }
    }

    private def validateLevelExam(
                                     exam: Map[String, AnyRef],
                                     metadata: Map[String, AnyRef],
                                     errors: ListBuffer[String]
                                 )(implicit oec: OntologyEngineContext, ec: ExecutionContext, parentNode: Node): Future[Unit] = {
        val courseId = exam.getOrElse(LEVEL_EXAM_COURSE_ID, "").toString.trim
        if (courseId.isEmpty) {
            errors += missingLevelExamCourseId()
            Future.unit
        } else {
            validateCourseExists(courseId, "Level Exam", errors)
        }
    }

    private def validateEntranceExam(
                                        exam: Map[String, AnyRef],
                                        errors: ListBuffer[String]
                                    )(implicit oec: OntologyEngineContext, ec: ExecutionContext, parentNode: Node): Future[Unit] = {
        val enabled = exam.getOrElse(ENTRANCE_EXAM_ENABLED, TIME_NO).toString
        if (enabled == TIME_YES) {
            val courseId = exam.getOrElse(ENTRANCE_EXAM_COURSE_ID, "").toString.trim
            if (courseId.isEmpty) {
                errors += missingEntranceExamCourseId()
                Future.unit
            } else {
                validateCourseExists(courseId, "Entrance Exam", errors)
            }
        } else {
            Future.unit
        }
    }
}