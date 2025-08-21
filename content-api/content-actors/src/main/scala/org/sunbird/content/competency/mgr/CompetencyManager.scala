package org.sunbird.content.competency.mgr
import org.sunbird.content.competency.mgr.validator.{CompetencyFramework, CompetencyValidator, CompetencyLevel}
import org.sunbird.content.competency.mgr.constants.CompetencyConstants._

object CompetencyManager {
    private val defaultValidator = new CompetencyFramework
    private val validators: Map[String, CompetencyValidator] = Map(
        COMPETENCY_FRAMEWORK -> new CompetencyFramework,
        COMPETENCY_LEVEL     -> new CompetencyLevel
    )

    def getValidator(primaryCategory: String): CompetencyValidator = {
        validators.getOrElse(primaryCategory, defaultValidator)
    }
}