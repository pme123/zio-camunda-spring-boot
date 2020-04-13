package pme123.camunda.boot.delegate

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}

trait CamundaDelegate extends JavaDelegate {

  implicit  class CamundaExecution(execution: DelegateExecution) {
    
    def stringVar(key: String): String =
      Option(execution.getVariable(key)).map(_.toString).getOrElse("-")
  }

}
