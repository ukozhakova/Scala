package Lab11

import java.io.{BufferedReader, File, InputStreamReader}

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsV2Request, ListObjectsV2Result, ObjectMetadata, PutObjectRequest}
import collection.JavaConverters._

object Boot extends App {

  val clientRegion = Regions.EU_CENTRAL_1
  //  BasicAWSCredentials awsCreds = new BasicAWSCredentials("access_key_id", "secret_key_id");
  //  AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
  //    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
  //    .build();

  val credentials = new BasicAWSCredentials("access-key","secret-key")

  val client = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(clientRegion)
    .build();


  val bucketName = "lab11-task2"
  val objectKey = "nice/file/abc.txt"
  val fileName = "./src/main/resources/s3/nice/file/abc.txt"

  def createBucket() = {

    if (!client.doesBucketExistV2(bucketName)) {
      client.createBucket(bucketName)
    }
    val location = client.getBucketLocation(bucketName)
    println(s"Bucket location: $location")


  }
  createBucket()
  def getObject(objectKey: String) = {
    val fullObject = client.getObject(new GetObjectRequest(bucketName, objectKey));

    val objectStream = fullObject.getObjectContent

    val reader = new BufferedReader(new InputStreamReader(objectStream));

    var str: String = reader.readLine()
    do {
      println(str)
      str = reader.readLine()
    } while (str != null)
  }
  //getObject(objectKey)

  def createObject(objectKey: String, filename: String) = {

    // Upload a file as a new object with ContentType and title specified.
    val request = new PutObjectRequest(bucketName, objectKey, new File(filename))

    val metadata = new ObjectMetadata()

    metadata.setContentType("plain/text")
    metadata.addUserMetadata("user-type", "customer")

    request.setMetadata(metadata)
    client.putObject(request)
  }
 //createObject(objectKey, fileName)

  def listObjects() = {
    val req = new ListObjectsV2Request().withBucketName(bucketName);
    //    val result: ListObjectsV2Result

    var flag = true

    while (flag) {
      val result = client.listObjectsV2(req)

      result.getObjectSummaries().asScala.toList.foreach { objectSummary =>
        printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
      }
      // If there are more than maxKeys keys in the bucket, get a continuation token
      // and list the next objects.
      val token = result.getNextContinuationToken()
      System.out.println("Next Continuation Token: " + token);
      req.setContinuationToken(token)

      if (!result.isTruncated) {
        flag = false
      }
    }
  }
 //listObjects()
}
