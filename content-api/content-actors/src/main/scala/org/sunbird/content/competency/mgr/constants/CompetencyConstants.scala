package org.sunbird.content.competency.mgr.constants

object CompetencyConstants {
    val COMPETENCY_FRAMEWORK: String = "Competency Framework"
    val COMPETENCY_LEVEL: String     = "Competency Level"


    val TIME_LIMIT_ENABLED: String = "enabled"
    val TIME_LIMIT_DURATION: String = "duration"
    val TIME_LIMIT_VALUE: String    = "value"
    val TIME_LIMIT_UNIT: String     = "unit"
    val TIME_YES: String      = "Yes"
    val TIME_NO: String       = "No"

    val ENTRANCE_EXAM_ENABLED: String = "enabled"
    val ENTRANCE_EXAM_COURSE_ID: String  = "courseId"

    val LEVEL_EXAM_COURSE_ID: String = "courseId"

}

object CompetencyErrorMessages {

    import CompetencyConstants._

    def missingEntranceExamCourseId(): String =
        s"entranceExam.$ENTRANCE_EXAM_COURSE_ID is required when $ENTRANCE_EXAM_ENABLED=$TIME_YES"

    def missingLevelExamCourseId(): String =
        s"levelExam.$LEVEL_EXAM_COURSE_ID is required"

}