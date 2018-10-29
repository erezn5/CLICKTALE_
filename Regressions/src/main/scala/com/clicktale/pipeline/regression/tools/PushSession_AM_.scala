package com.clicktale.pipeline.regression.tools

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, FileOutputStream}
import java.util.Base64
import com.clicktale.parsing.avro.{AvroRecordingSerializer, AvroVersionExtractor, VersionedRecording}
import com.clicktale.parsing.xml.RecordingXmlWriter
import com.clicktale.pipeline.dataObjects.{PusherParams, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import com.clicktale.recordings.Recording
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.HttpClientBuilder
import org.scalacheck.Prop.Exception
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.ParSeq
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scalaj.http.Http


object PushSession_AM_ {

  //val src: Array[Array[String]] = CsvReader.GetAllLines("C:\\Git\\Regressions\\src\\main\\resources\\whatToPush.txt").drop(1).map(_.split("\t"))
  //src.foreach(line => println(line(0)))
  val amazon = new Amazon()
  val optionalPid: Int = conf.getString("WebRecorder.Environments.AwsQaLin.Pid").toInt
  val subsId: Int = conf.getString("WebRecorder.Environments.AwsQaLin.SubsId").toInt
  var badCssURL = ArrayBuffer[String]()
  var goodCssURL = ArrayBuffer[String]()
  val subsIdForCss: Int = conf.getString("WebRecorder.Environments.AwsQaLin.SubsId").toInt//conf.getString("WebRecorder.Environments.AwsCentral.AssetsManagerSubsId").toInt
  val pidForCss: Int =conf.getString("WebRecorder.Environments.AwsQaLin.Pid").toInt// conf.getString("WebRecorder.Environments.AwsCentral.Pid_AM").toInt
  val cageInstanceIp: String =
    conf.getString(s"WebRecorder.Cage.AwsQaLin.DNS")
  val pushArguments: Array[List[PusherParams]] =
    Array(
     // List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/REDHAT-254.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/ASU-146.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
     //List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/HBFW-491.json",optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //  List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/staging_partition37.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
       // List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/escalation.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
          //List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/Yaniv.json", optionalPid = 63, subsId = 69694, dnsName = "https://ing.clicktale.net/ctn_v2/"))
//      List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/adidas.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
        //  List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/rayBan.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
  //        List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/oakley_LUXO92.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
         //     List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/adidas_675.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/fido_ROG-73.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))//TODO check comment
     //  List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/ZOOM-112.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
   //     List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/TNF-969.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
//      List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/TRUEC-80.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
    //  List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/TMBUS-614.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
     // List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/HMDP-1611.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
//        List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/DSC-818.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
     // List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/CANFULL-200.json", optionalPid = 6, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
    // List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/ADIDAS-697.json", optionalPid = 6, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //  List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/ALSTTCA-296.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
       //  List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/MSCOMN-364.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
     // List.fill(1)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = optionalPid, subsId = subsId))
        List.fill(1)(PusherParams(recFile = "/recFiles/Assets_Manager_Jsons/adidas_mobile.json",optionalPid = optionalPid, subsId = subsId))
    )

  val archivedFilesFolder =
    """C:\temp\ArchivedFilesFromPusher"""

  if (!new File(archivedFilesFolder).exists) new File(archivedFilesFolder).mkdir()

  def paste(str: String): String = {
    return "https://s3.amazonaws.com/nv-q-s3-assets-01".concat(str)
  }

  def amazonActions(): Unit ={

    if(!amazon.isBucketExist()){
      if(!amazon.isBucketEmpty()) {
        amazon.countNumberOfObjectsInsideBucket()
        amazon.removeObjectsFromBucket()
      }
    }
  }


  def main(args: Array[String]): Unit = {
    amazonActions()

    pushArguments.par.foreach(
      recFilesToPush => push(recFilesToPush).par.foreach {
        case (subsId, pid, sid) =>
          println(s"\n ----------- FINISHED PUSHING -------------- \n sid = $sid \n pid = $pid & subsid = $subsId")

          saveSessionArchiveToDisk(subsId, pid, sid, isQa = true)
          //returnAvro(sid, isQa = true)
          //println(returnAvro(sid, isQa = true))
          val response: String = tryUrlResponse(s"$cageInstanceIp/recording/v1/$subsIdForCss/$pidForCss/$sid")
          searchRecursivley(response)
          println("\n")
          println(pushArguments)
          printArray(badCssURL,badCssURL.length,"bad")
          printArray(goodCssURL,goodCssURL.length,"good")
          amazon.countNumberOfObjectsInsideBucket()


      }
    )

    //amazon.getTagName(amazon.client)
  }
  def push(desiredPushArguments: List[PusherParams]): ParSeq[(Int, Int, Long)] = {
    desiredPushArguments.par.flatMap(
      x => {
        x.sessionStreamsDelayFromAuth = 0
        sendSessionAndReturnDetails(x)
      })
  }

  def sendSessionAndReturnDetails(x: PusherParams): List[(Int, Int, Long)] ={
    val session = pushSessionParallely(x.recFile, x.authState, optionalPid = x.optionalPid,
      subsId = x.subsId, referrer = x.referrer, dnsName = x.dnsName)
    println(s"\n ***** Finished pushing: ${x.recFile} to: \n  " +
      s"pid = ${x.optionalPid} with sid = ${session.sid}\n")
    List((x.subsId ,x.optionalPid, session.sid.toLong))
  }


  def printArray(strings: ArrayBuffer[String],size: Int,cssStatus:String): Unit ={
    var i = 0
    println("-----------------------------------------------------------------------------------------------------------")
    println("List of " + cssStatus + " URL's: ")
//    if(cssStatus == "good"){
//      i=1

//    }
    while(i<size){
      println(strings(i))
      i+=1
    }

    println("Number of " + cssStatus +"  CSS files are: " + size)
    println("-----------------------------------------------------------------------------------------------------------")
  }
  def searchRecursivley(str: String): Unit = {

    val temp = str.split("https://s3.amazonaws.com/nv-p1-s3-assets-01")
    var i = 1
    var res=""

  while (i < temp.length) {
    val temp2 = temp(i).split("\"")(0).split(')')
    val str: String = temp2(0).substring(temp2(0).length - 1, temp2(0).length)

    if (str == "\\") {
      res = temp2(0).substring(0, (temp2(0).length - 1))
    } else {
      res = temp2(0).substring(0, (temp2(0).length))
    }
    var res1: String = paste(res)
    // tryUrlResponse(res1)

    searchRecursivley(tryUrlResponse(res1))
    i += 1

    }
  }

  def tryUrlResponse(url: String, numOfTries: Int = 10): String = {
    val tries = 10
    val sleep = 1000
    //println("url address is: " + url)
    print(".")
    if (numOfTries < tries) {
      Thread.sleep(sleep)
    }
    try {
      Thread.sleep(sleep)
      val httpGet = new HttpGet(url)
      // set the desired header values
      val client = HttpClientBuilder.create().build()
      val response = client.execute(httpGet)
      var responseCode = response.getStatusLine.getStatusCode.toString
      if(responseCode!="200"){
        badCssURL+=url
      }else if(responseCode=="200"){
        goodCssURL+=url
      }
      val result = String.join("",IOUtils.readLines(response.getEntity.getContent))
      client.close()
      result
    }
    catch {
      case e: Exception =>{ if(numOfTries > 1) tryUrlResponse(url, numOfTries - 1) else throw e}
      case x: Throwable => throw x
    }
  }



  def returnAvro(sid: Long,
                 isQa: Boolean = false): Recording = {

    val client =
      HttpClientBuilder.create().build()
    val response =
      client.execute(new HttpGet(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/events/v1/$sid"))
    println(s"http://internal-nv-q-elb-int-cage-01-284466208.us-east-1.elb.amazonaws.com/events/v1/$sid")
    //val recordingAvro = Http(s"http://internal-nv-q-elb-int-cage-01-284466208.us-east-1.elb.amazonaws.com/events/v1/$sid")//.asBytes.body
    val recordingAvro = Http(s"http://internal-nv-q-elb-int-cage-01-284466208.us-east-1.elb.amazonaws.com/events/v1/$sid")//.asBytes.body
    val decodedBytes = Base64.getDecoder.decode(recordingAvro.asBytes.body)

    val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
    response.getEntity.writeTo(baos)
    val bytes: Array[Byte] = baos.toByteArray
    //val bla = Base64.decodeString(bytes)

    val serializer: AvroRecordingSerializer = new AvroRecordingSerializer()
    val versionedAvro: VersionedRecording = AvroVersionExtractor.extractVersionRecordingPair(new ByteArrayInputStream(decodedBytes))
    val deserializedRecording: Recording = serializer.deserialize(versionedAvro.recordingBytes)
    deserializedRecording
  }

  def avroContent(recordingAvro:Array[Byte]): String = {
    val serializer: AvroRecordingSerializer = new AvroRecordingSerializer()
    val versionedAvro = AvroVersionExtractor.extractVersionRecordingPair(new ByteArrayInputStream(recordingAvro))

    val deserializedRecording = serializer.deserialize(versionedAvro.recordingBytes)
    val generatedXml = RecordingXmlWriter.serializeRecoding(deserializedRecording)

    lazy val printer = new scala.xml.PrettyPrinter(1000, 2)

    val xmlData = printer.format(generatedXml)
    xmlData

  }

  def saveSessionArchiveToDisk(subsId:Int,
                               pid: Int,
                               sid: Long,
                               isQa: Boolean = false): Unit = {
    if (isQa){
      Try(loadFromCage(subsId, pid, sid)) match {
        case Success(archivedContent) =>
          archivedContent.writeArchivedContentToDisk(s"$archivedFilesFolder\\$sid")
          println("bla" + "\n" +archivedContent.events + "\n" + archivedContent.rawData + "\n" + "recording time from Json" + RecordingJson(archivedContent.recording).clientRecordingDateTimeUtc)
        case Failure(e) =>
          println("\n ***** !!! ERROR - Failed Retrieving data from cage!!! ***** \n ***** !!! Info from the exception:\n" +
            e.getMessage)
      }
    }

  }
}

