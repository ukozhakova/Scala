package Project.actor

import Project.model.{Doctor, ErrorResponse, Patient, SuccessfulResponse}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object TestBot {

  case object TestCreate

  case object TestConflict

  case object TestRead

  case object TestNotFound

  case object TestUpdate

  case object TestDelete

  def props(manager: ActorRef) = Props(new TestBot(manager))
}

class TestBot(manager: ActorRef) extends Actor with ActorLogging {
  import TestBot._

  override def receive: Receive = {
    case TestCreate =>
    case TestConflict =>
    case TestRead =>
    case TestNotFound =>
    case TestUpdate =>
    case TestDelete =>
    case SuccessfulResponse(status, msg) =>
      log.info("Received Successful Response with status: {} and message: {}", status, msg)

    case ErrorResponse(status, msg) =>
      log.warning("Received Error Response with status: {} and message: {}", status, msg)

    case patient: Patient =>
      log.info("Received patient: [{}]", patient)
  }
}