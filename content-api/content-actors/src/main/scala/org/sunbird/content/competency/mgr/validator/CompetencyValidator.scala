package org.sunbird.content.competency.mgr.validator
import org.sunbird.graph.dac.model.Node

trait CompetencyValidator {
  def validate(node: Node): Unit
}
