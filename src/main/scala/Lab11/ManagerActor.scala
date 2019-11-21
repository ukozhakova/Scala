package Lab11
import java.io.{File, FilenameFilter}
import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging, Props}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsRequest, ObjectMetadata, PutObjectRequest, PutObjectResult}
import Lab11.model.{ErrorResponse, SuccessfulResponse}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions

import scala.util.{Failure, Success, Try}

object ManagerActor {
  private val path1 = "./src/main/resources/s3"
  private val path2 = "./src/main/resources/in"
  private val path3 = "./src/main/resources/out"


  def downloadFile(client: AmazonS3,bucketName: String, objectKey: String, fullPath: String): ObjectMetadata = {
    val file = new File(fullPath)
    createMissingDirectories(file)
    client.getObject(new GetObjectRequest(bucketName, objectKey), file)
  }

  def uploadFile(client: AmazonS3,bucketName: String, objectKey: String, fileName: String): PutObjectResult = {
    val request = new PutObjectRequest(bucketName, objectKey, new File(fileName))

    val metadata = new ObjectMetadata()

    metadata.setContentType("plain/text")
    metadata.addUserMetadata("user-type", "customer")

    request.setMetadata(metadata)
    client.putObject(request)
  }

  def createMissingDirectories(file: File): Unit = {
    val dirs = file.getParentFile()

    if (dirs != null) {
      dirs.mkdirs()
    }
  }

  case class GetFile(fileName: String)

  case class UploadFile(fileName: String)

  case object UploadAllFiles

  case object DownloadAllFiles

  def props(client: AmazonS3, bucketName: String) = Props(new ManagerActor(client, bucketName))

}

class ManagerActor(client: AmazonS3, bucketName: String) extends Actor with ActorLogging {

  import ManagerActor._

  override def receive: Receive = {
    case GetFile(fileName) =>
      val rootSender = sender()
      val objectKey = fileName

      if (client.doesObjectExist(bucketName, objectKey)) {
        val fullPath = s"${path1}/${fileName}"
        downloadFile(client: AmazonS3, bucketName, fileName, fullPath)

        rootSender ! Right(SuccessfulResponse(200, s"File ${fileName} downloaded successfully"))
        log.info("File {} downloaded from AWS S3", fileName)
      } else {
        rootSender ! Left(ErrorResponse(404, s"File ${fileName} does not exist"))
        log.info(s"Failed to download file ${fileName}. Something went wrong")
      }

    case UploadFile(fileName) =>
      val rootSender = sender()
      val objectKey = fileName

      if (client.doesObjectExist(bucketName, objectKey)) {
        rootSender ! Left(ErrorResponse(409, s"File ${fileName} already exists in this bucket"))
        log.info(s"Failed to upload file ${fileName}. It already exists")
      } else {
        val filePath = s"${path1}/${fileName}"

        Try(uploadFile(client: AmazonS3,bucketName, objectKey, filePath)) match {
          case Success(_) =>
            rootSender ! Right(SuccessfulResponse(201, s"File ${fileName} downloaded successfully"))
            log.info("File {} uploaded to AWS S3", fileName)
          case Failure(exception) =>
            rootSender ! Left(ErrorResponse(500, s"Internal error occurred while uploading a file ${fileName}"))
            log.info(s"Failed to upload ${fileName}. Error message: ${exception.getMessage}")
        }
      }

    case UploadAllFiles =>
      val mainDirectory: File = new File(path3)
      uploadDirectoryContents(mainDirectory)

      def uploadDirectoryContents(dir: File): Unit = {
        val files: Array[File] = dir.listFiles

        for (file <- files) {
          var path = Paths.get(file.getPath)
          path = path.subpath(5, path.getNameCount)

          if (file.isDirectory)
            uploadDirectoryContents(file)
          else
            uploadFile(client: AmazonS3, bucketName, path.toString, path3 + "/" + path.toString)

        }
      }

    case DownloadAllFiles =>
      val objects = client.listObjects(new ListObjectsRequest().withBucketName(bucketName))
      objects.getObjectSummaries.forEach(objectSummary => downloadFile(client: AmazonS3, bucketName, objectSummary.getKey, path2 + "/" + objectSummary.getKey))
  }
}
