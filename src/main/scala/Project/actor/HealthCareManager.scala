package Project.actor

import Project.model.{ErrorResponse, Patient, SuccessfulResponse}
import akka.actor.{Actor, ActorLogging, Props}

object HealthCareManager {
  case class AddPatient(patient: Patient)

  // Read
  case class ReadPatient(id: String)

  // Update
  case class UpdatePatient(patient: Patient)

  // Delete
  case class DeletePatient(id: String)

  def props() = Props(new HealthCareManager)
}
class HealthCareManager extends Actor with ActorLogging {

  // import companion OBJECT
  import HealthCareManager._

  var patients: Map[String, Patient] = Map()

  override def receive: Receive = {

    case AddPatient(patient: Patient) =>
      patients.get(patient.id) match {
        case Some(existingPatient) =>
          log.warning(s"Could not add a patient with ID: ${patient.id} because he/she already exists.")
          sender() ! Left(ErrorResponse(409, s"Patient with ID: ${patient.id} already exists."))

        case None =>
          patients = patients + (patient.id -> patient)
          log.info("Patient with ID: {} added.", patient.id)
          sender() ! Right(SuccessfulResponse(201, s"Patient with ID: ${patient.id} added."))
      }

    case ReadPatient(id) =>
      patients.get(id) match {
        case Some(existingPatient) =>
          log.info(s"Patient with ID: ${id} is:")
          sender() ! Right(existingPatient)

        case None =>
          log.warning("Patient with ID: {} does not exist.", id)
          sender() ! Left(ErrorResponse(404, s"Patient with ID: ${id} not found."))
      }

    case UpdatePatient(patient) =>
      patients.get(patient.id) match {
        case Some(existingPatient) =>
          patients = patients- existingPatient.id+ (patient.id->patient)
          log.info(s"Patient with ID ${patient.id} updated. ")
          sender() ! Right(SuccessfulResponse(200, s"Patient with ID ${patient.id} updated successfully. "))

        case None =>
          log.warning("Patient with ID: {} can not be updated.", patient.id)
          sender() ! Left(ErrorResponse(404, s"Patient with ID: ${patient.id} does not exist."))
      }

    case DeletePatient(id) =>
      patients.get(id) match {
        case Some(existingPatient) =>
          patients = patients - id
          log.info(s"Patient with ID ${id} deleted. ")
          sender() ! Right(SuccessfulResponse(204, s"Patine with ID ${id} deleted successfully. "))
        case None =>
          log.warning("Patient with ID: {} can not be deleted.", id)
          sender() ! Left(ErrorResponse(404, s"Patient with ID: ${id} does not exist."))
      }
  }

}