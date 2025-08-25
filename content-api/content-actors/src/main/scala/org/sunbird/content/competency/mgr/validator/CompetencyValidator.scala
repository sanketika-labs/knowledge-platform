package org.sunbird.content.competency.mgr.validator
import org.sunbird.graph.dac.model.Node
import org.sunbird.graph.OntologyEngineContext

import scala.concurrent.ExecutionContext

trait CompetencyValidator {
    def validate(node: Node)(implicit oec: OntologyEngineContext, ec: ExecutionContext): Unit
}
