package Lab11

import Lab11.model.{ErrorResponse, Path, SuccessfulResponse}
import spray.json.DefaultJsonProtocol

trait SprayJsonSerializer extends DefaultJsonProtocol{
  implicit val successfulResponse = jsonFormat2(SuccessfulResponse)
  implicit val errorResponse = jsonFormat2(ErrorResponse)
  implicit val path = jsonFormat1(Path)
}
