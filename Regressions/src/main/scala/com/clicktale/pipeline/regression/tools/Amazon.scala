package com.clicktale.pipeline.regression.tools

import java.io.InputStream

import com.amazonaws.services.ec2.model.Tag
import com.amazonaws.{AmazonClientException, auth}
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import scala.collection.JavaConversions._
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.model.{GetObjectTaggingRequest, ObjectListing, S3ObjectSummary}
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3ClientBuilder, model}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3._

import scala.language.postfixOps

class Amazon {

  val AWS_ACCESS_KEY = conf.getString("WebRecorder.PushSession.AccessKey")
  val AWS_SECRET_KEY = conf.getString("WebRecorder.PushSession.SecretKey")
  val bucketName ="nv-q-s3-assets-01"; //"nv-q-recordings";//
  val provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY))
  val client = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion("us-east-1").build()

  var tag: Tag = new Tag()


  def removeObjectsFromBucket(){

    print("Removing objects from bucket")
      var object_listing: ObjectListing = client.listObjects(bucketName)
      var flag: Boolean = true
      while (flag) {
        val iterator: Iterator[_] = object_listing.getObjectSummaries.iterator()
        while (iterator.hasNext) {
          val summary: S3ObjectSummary = iterator.next().asInstanceOf[S3ObjectSummary]
          if(summary.getKey().contains("HTMLs/") ){
            if(summary.getKey().contains("HTMLs/[ADIDAS-PTCCODE]-233197-452.html") || (summary.getKey().contains("HTMLs/[LUXO-41]-233397-10.html")) || (summary.getKey().contains("HTMLs/[ROG-73]-233256-56.html")) || (summary.getKey().contains("HTMLs/[ZOOM-112]-233383-3012.html")) ||(summary.getKey().contains("HTMLs/[ADIDAS-675]-233197-452.html"))) {

              println("dont delete folder: " + summary.getKey())
            }
          } else {
            client.deleteObject(bucketName, summary.getKey())
            print(".")
          }

        }
        println()


        flag=false

      }

    }

  def countNumberOfObjectsInsideBucket(): Unit ={

    var object_listing: ObjectListing = client.listObjects(bucketName)
    var flag: Boolean = true
    var count=0
    while (flag) {
      val iterator: Iterator[_] = object_listing.getObjectSummaries.iterator()
      while (iterator.hasNext) {
        val summary: S3ObjectSummary = iterator.next().asInstanceOf[S3ObjectSummary]
        count+=1
      }

      flag=false
      println("Number of objects in " + bucketName  + " are: " + count)
     // println("Number of objects in " + bucketName  + " are: " + (count-5))

    }
  }
  def isBucketEmpty(): Boolean ={

    var isEmpty=false
    var object_listing: ObjectListing = client.listObjects(bucketName)
    var flag: Boolean = true
    var count=0
    while (flag) {
      val iterator: Iterator[_] = object_listing.getObjectSummaries.iterator()

      while (iterator.hasNext) {
        val summary: S3ObjectSummary = iterator.next().asInstanceOf[S3ObjectSummary]
        count+=1
      }

      flag=false
      if(count==0){
        isEmpty=true
        println("Bucket " + bucketName + " is: empty")
      }else{
        println("Bucket " + bucketName + " is: not empty")
      }

    }
    isEmpty
  }
  def retrieveObjectTags(keyName: String): Unit ={

    try {
      println("Listing objects")
      val req: ListObjectsV2Request =
        new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2)
      var result: ListObjectsV2Result = null
      do {


        result = client.listObjectsV2(req)
        for (objectSummary <- result.getObjectSummaries) {
          println(
            " - " + objectSummary.getKey + "  " + "(size = " + objectSummary.getSize +
              ")")

          //var s3obj: S3Object = client.getObject(new GetObjectRequest(bucketName,objectSummary.getKey))
         // var s3obj: S3Object = client.getObject(new GetObjectRequest(bucketName,"232897/201/CSS/https/optanon.blob.core.windows.net/skins/default_flat_top_two_button_black/v2/css/optanon.css" ))
//          val gtr = new GetObjectTaggingRequest(bucketName,"232897/201/CSS/https/optanon.blob.core.windows.net/skins/default_flat_top_two_button_black/v2/css/optanon.css")
//          val getTagsRes = client.getObjectTagging(gtr)
     //     println(gtr)
          // var objectData:InputStream = s3obj.getObjectContent
          //  println("Content Type: " + s3obj.getObjectMetadata.getContentType)
         //   println("Content Tag: " + s3obj.getObjectMetadata.getETag)
            //println(objectData)


//            var tag = new Tag(objectSummary.getKey)
//            println(tag.getValue)



        }

        println("Next Continuation Token : " + result.getNextContinuationToken)
        req.setContinuationToken(result.getNextContinuationToken)
      } while (result.isTruncated == true);
  }catch {
      case ase: AmazonServiceException => {
        println(
          "Caught an AmazonServiceException, " + "which means your request made it " +
            "to Amazon S3, but was rejected with an error response " +
            "for some reason.")
        println("Error Message:    " + ase.getMessage)
        println("HTTP Status Code: " + ase.getStatusCode)
        println("AWS Error Code:   " + ase.getErrorCode)
        println("Error Type:       " + ase.getErrorType)
        println("Request ID:       " + ase.getRequestId)
      }

      case ace: AmazonClientException => {
        println(
          "Caught an AmazonClientException, " + "which means the client encountered " +
            "an internal error while trying to communicate" +
            " with S3, " +
            "such as not being able to access the network.")
        println("Error Message: " + ace.getMessage)
      }

    }

//    var getTaggingRequest = new GetObjectTaggingRequest(bucketName, keyName);
//    var getTagsResult = client.getObjectTagging(getTaggingRequest);

//    println(getTagsResult)
    var tag: Tag = new Tag()
    println("tag name:" + tag.getValue)

  }

  def getTagName(instance: Instance){

    if(instance.getTags()!=null){
      for( tag <- instance.getTags() if tag.getKey.==("Name")){
        tag.getValue
      }
    }

   // var object = client.getObject(new GetObjectRequest(bucketName,"gagagag"))


  }

  def isBucketExist(): Boolean ={

    var exist = false
    if(client.doesBucketExist(bucketName))
      println(bucketName + " Exist")
    else println(bucketName+ " Not Exist")
    exist

  }

}
