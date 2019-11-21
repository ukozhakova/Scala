package Lab11

import Lab11.model.{ErrorResponse, Path, SuccessfulResponse}
import akka.actor.{Actor, ActorLogging, Props}

// props
// messages
object ManagerActor {

  // Create
  case class CreateFile(path: Path)

  // Read
  case class ReadFile(path: Path)

  def props() = Props(new ManagerActor)
}

// know about existing movies
// can create a movie
// can manage movie
class ManagerActor extends Actor with ActorLogging {

  // import companion OBJECT
  import ManagerActor._

  var files: Map[String, Path] = Map()

  override def receive: Receive = {

    case CreateFile(file) =>
      files.get(file.path) match {
        case Some(existingFile) =>
          log.warning(s"Could not create a file with path: ${file.path} because it already exists.")
          sender() ! Left(ErrorResponse(409, s"File with path: ${file.path} already exists."))

        case None =>
          files = files + (file.path -> file)
          log.info("File with path: {} created.", file.path)
          sender() ! Right(SuccessfulResponse(201, s"File with ID: ${file.path} created."))
      }

    case ReadFile(file) =>
      files.get(file.path) match {
        case Some(existingFile) =>
          log.info(s"File with path: ${file.path} is:")
          sender() ! Right(existingFile)

        case None =>
          log.warning("File with path: {} does not exist.", file.path)
          sender() ! Left(ErrorResponse(404, s"File with path: ${file.path} not found."))
      }
  }
}
