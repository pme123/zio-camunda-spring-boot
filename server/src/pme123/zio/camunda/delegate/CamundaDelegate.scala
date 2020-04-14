package pme123.zio.camunda.delegate

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}
import zio.{IO, ZIO}

trait CamundaDelegate extends JavaDelegate {

  implicit  class CamundaExecution(execution: DelegateExecution) {
    
    def stringVar(key: String): IO[Unit, String] =
      ZIO.fromOption(Option(execution.getVariable(key)).map(_.toString))
  }

}
