package org.sunbird.content.competency.mgr.constants

object CompetencyConstants {
    // Primary categories
    val COMPETENCY_FRAMEWORK: String = "Competency Framework"
    val COMPETENCY_LEVEL: String     = "Competency Level"

    // Schema attributes
    val SIGNUP_BY_ADMIN: String = "Admin"
    val SIGNUP_BY_USER: String  = "User"
    val VALID_SIGNUP_BY: Set[String] = Set(SIGNUP_BY_ADMIN, SIGNUP_BY_USER)

    val ENROLLMENT_FULL: String     = "Full Enrollement"
    val ENROLLMENT_ENTRANCE: String = "Entrance Exam Based"
    val ENROLLMENT_PROGRESS: String = "Progress Based"
    val VALID_ENROLLMENT_TYPES: Set[String] = Set(ENROLLMENT_FULL, ENROLLMENT_ENTRANCE, ENROLLMENT_PROGRESS)

    val SECTOR: String = "Education"
    val DOMAIN: String = "Preschool"
    val VALID_SECTORS: Set[String] = Set(SECTOR)
    val VALID_DOMAINS: Set[String] = Set(DOMAIN)

    val VALID_LEVEL_NAMES: Set[String] = Set("Beginner", "Intermediate", "Advanced")

    val TIME_LIMIT_ENABLED: String = "enabled"
    val TIME_LIMIT_DURATION: String = "duration"
    val TIME_LIMIT_VALUE: String    = "value"
    val TIME_LIMIT_UNIT: String     = "unit"
    val TIME_LIMIT_YES: String      = "Yes"
    val TIME_LIMIT_NO: String       = "No"

    val ENTRANCE_EXAM_ENABLED: String = "enabled"
    val ENTRANCE_EXAM_COURSE_ID: String  = "courseId"
    val ENTRANCE_EXAM_VALID: Set[String] = Set("Yes", "No")

    val LEVEL_EXAM_COURSE_ID: String = "courseId"
    val PASSING_CRITERIA: String  = "passingCriteria"
    val PASSING_CRITERIA_MUST_PASS: String = "mustPass"
    val PASSING_CRITERIA_VALID: Set[String] = Set("Yes", "No")

    val REQUIRED_COMPETENCY_FRAMEWORK_FIELDS: List[String] = List(
        "sector",
        "signupBy",
        "enrollmentType"
    )

    val REQUIRED_COMPETENCY_LEVEL_FIELDS: List[String] = List(
        "name",
        "timeLimit",
        "entranceExam",
        "levelExam"
    )

}

object CompetencyErrorMessages {
    import CompetencyConstants._

    def invalidLevelName(value: String): String =
        s"Invalid name: $value (must be one of ${VALID_LEVEL_NAMES.mkString(", ")})"

    def missingTimeLimitValue(): String =
        s"$TIME_LIMIT_DURATION.$TIME_LIMIT_VALUE is required when $TIME_LIMIT_ENABLED=$TIME_LIMIT_YES"

    def missingTimeLimitUnit(): String =
        s"$TIME_LIMIT_DURATION.$TIME_LIMIT_UNIT is required when $TIME_LIMIT_ENABLED=$TIME_LIMIT_YES"

    def invalidEntranceExamEnabled(value: String): String =
        s"entranceExam.$ENTRANCE_EXAM_ENABLED must be one of ${ENTRANCE_EXAM_VALID.mkString(", ")}"

    def missingEntranceExamCourseId(): String =
        s"entranceExam.$ENTRANCE_EXAM_COURSE_ID is required when $ENTRANCE_EXAM_ENABLED=$TIME_LIMIT_YES"

    def missingLevelExamCourseId(): String =
        s"levelExam.$LEVEL_EXAM_COURSE_ID is required"

    def invalidPassingCriteria(value: String): String =
        s"levelExam.$PASSING_CRITERIA.$PASSING_CRITERIA_MUST_PASS '$value' must be one of ${PASSING_CRITERIA_VALID.mkString(", ")}"

    def invalidSignupBy(value: String): String =
        s"Invalid signupBy: $value (must be one of ${VALID_SIGNUP_BY.mkString(", ")})"

    def invalidEnrollmentType(value: String): String =
        s"Invalid enrollmentType: $value (must be one of ${VALID_ENROLLMENT_TYPES.mkString(", ")})"

    def invalidSectorName(value: String): String =
        s"Invalid sector.name: $value (must be one of ${VALID_SECTORS.mkString(", ")})"

    def invalidSectorDomain(value: String): String =
        s"Invalid sector.domain: $value (must be one of ${VALID_DOMAINS.mkString(", ")})"
}
