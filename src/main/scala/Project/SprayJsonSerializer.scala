package Project
import spray.json.DefaultJsonProtocol
import Project.model.{Doctor, ErrorResponse, Patient, SuccessfulResponse, Gender}
trait SprayJsonSerializer extends DefaultJsonProtocol{
  implicit val doctorFormat = jsonFormat5(Doctor)
  implicit val patientFormat = jsonFormat6(Patient)
  implicit val successfulResponse = jsonFormat2(SuccessfulResponse)
  implicit val errorResponse = jsonFormat2(ErrorResponse)
}
