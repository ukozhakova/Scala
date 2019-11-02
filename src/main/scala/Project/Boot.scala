package Project

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import Project.actor.{HealthCareManager, TestBot}
import Project.model.{ErrorResponse, Patient, Doctor, Response, SuccessfulResponse}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Boot extends App with SprayJsonSerializer {

  implicit val system: ActorSystem = ActorSystem("healthcare-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val healthcaremanager = system.actorOf(HealthCareManager.props(), "healthcare-manager")

  val route =
    path("healthcheck") {
      get {
       complete(StatusCodes.Accepted ->"hello")
      }
    }~
      pathPrefix("almaty-health") {
        path("patient" / Segment) { patientId =>
          get {
            val res= (healthcaremanager ? HealthCareManager.ReadPatient(patientId)).mapTo[Either[ErrorResponse, Patient]]
            onSuccess(res) {
              case Left(error) => complete(error.status, error)
              case Right(movie) => complete(200, movie)

            }
          }
        } ~
          path("patient") {
            post {
              entity(as[Patient]) { patient =>
                val res = (healthcaremanager ? HealthCareManager.AddPatient(patient)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
                onSuccess(res) {
                  case Left(error) =>complete(error.status, error)
                  case Right(success) =>complete(201, success)
                }
              }
            }
          } ~
        path("patient"){
          put{
            entity(as[Patient]) { patient =>
              val res=  (healthcaremanager ? HealthCareManager.UpdatePatient(patient)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
              onSuccess(res){
                case Left(error) =>complete(error.status, error)
                case Right(success) =>complete(success.status, success)
              }
            }
          }
        }~
        path("patient"/Segment){patientId =>
          delete {
              val res= (healthcaremanager ? HealthCareManager.DeletePatient(patientId)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            onSuccess(res){
              case Left(error) =>complete(error.status, error)
              case Right(success) =>complete(success.status, success)
            }
          }
        }
      }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8000)

}