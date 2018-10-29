package com.clicktale.pipeline.regression.tools

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, FileOutputStream}
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Base64

import com.aerospike.client.policy.WritePolicy
import com.aerospike.client.{AerospikeClient, Key}
import com.clicktale.parsing.avro.{AvroRecordingSerializer, AvroVersionExtractor, VersionedRecording}
import com.clicktale.parsing.xml.RecordingXmlWriter
import com.clicktale.pipeline.dataObjects.{PusherParams, RecordingJson}
import com.clicktale.pipeline.framework.MD5PathStrategy
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.helpers.CsvReader
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import com.clicktale.pipeline.storagemaster.support.serialization.encryption.RijndaelEncryption
import com.clicktale.recordings.Recording
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder

import scala.collection.parallel.ParSeq
import scala.util.{Failure, Success, Try}
import scalaj.http.Http


object PushSession {


  //val src: Array[Array[String]] = CsvReader.GetAllLines("C:\\Git\\Regressions\\src\\main\\resources\\whatToPush.txt").drop(1).map(_.split("\t"))
  //src.foreach(line => println(line(0)))
  val optionalPid: Int = conf.getString("WebRecorder.Environments.AwsQaLin.Pid").toInt
  val subsId: Int = conf.getString("WebRecorder.Environments.AwsQaLin.SubsId").toInt
  val cageInstanceIp: String =
    conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")
  val pushArguments: Array[List[PusherParams]] =
    Array(
      List.fill(1)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = optionalPid, subsId = subsId))

      //List.fill(10)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 98, subsId = 13624, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //      List.fill(1)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 6, subsId = 69093, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //List.fill(10)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 243, subsId = 233338, dnsName = "https://ing-district.clicktale.net/ctn_v2/"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 223098, subsId = 223))
      //      List.fill(1)(PusherParams(recFile = "/recFiles/recFidelity.json",optionalPid = optionalPid, subsId = subsId))
      //        List.fill(1)(PusherParams(recFile = "/recFiles/recBigData.json",optionalPid = optionalPid, subsId = subsId))

      //  List.fill(100)(PusherParams(recFile = "/recFiles/recFidelity.json"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/recPerfect.json"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/discovery.json"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/Vitaly-Fidality.json"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/TDBANK.json"))
      //List.fill(300)(PusherParams(recFile = "/recFiles/recFidelity.json",optionalPid = 999123, subsId = 9991))
      //List.fill(1)(PusherParams(recFile = "/recFiles/recBiggerData.json"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/japan.js on",optionalPid = 45, subsId = 69527, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 40, subsId = 233210, dnsName = "https://ing-district.clicktale.net/ctn_v2/"))
      //List.fill(100)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 12, subsId = 233217, dnsName = "https://ir-ing-district.clicktale.net/ctn_v2/")),
      //List.fill(100)(PusherParams(recFile = "/recFiles/recShmulik.json",optionalPid = 12, subsId = 233217, dnsName = "https://ir-ing-district.clicktale.net/ctn_v2/")),
      //List.fill(1)(PusherParams(recFile = "/recFiles/japan.json",optionalPid = 12, subsId = 233217, dnsName = "https://ir-ing-district.clicktale.net/ctn_v2/"))
      //      List.fill(1)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //      List.fill(1)(PusherParams(recFile = "/recFiles/Escalations/recNikeJapan.json",optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //      List.fill(1)(PusherParams(recFile = "/recFiles/recAdidasEs1.json",optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //      List.fill(8)(PusherParams(recFile = "/recFiles/rec.json",optionalPid = 6, subsId = 69093, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //      List.fill(1)(PusherParams(recFile = "/recFiles/recPerfect.json",optionalPid = 1, subsId = 69093, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //
      //      List.fill(1)(PusherParams(recFile = "/recFiles/rec.json", optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //List.fill(1)(PusherParams(recFile = "/recFiles/escalation.json",optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
      //      List.fill(1)(PusherParams(recFile = "/recFiles/recBigData.json",optionalPid = 2, subsId = 69313, dnsName = "https://ing.clicktale.net/ctn_v2/"))
    )
  val archivedFilesFolder =
    """C:\temp\ArchivedFilesFromPusher"""

  if (!new File(archivedFilesFolder).exists) new File(archivedFilesFolder).mkdir()

  def main(args: Array[String]): Unit = {
//    while(true) {
      pushArguments.par.foreach(
        recFilesToPush => push(recFilesToPush).par.foreach {
          case (subsId, pid, sid) =>
            println(s"\n ----------- FINISHED PUSHING -------------- \n sid = $sid \n pid = $pid & subsid = $subsId")
            saveSessionArchiveToDisk(subsId, pid, sid, isQa = true)
        }
      )
//      println("Going to sleep for 30 seconds...")
//      Thread.sleep(30000)
//    }
  }

  def push(desiredPushArguments: List[PusherParams]): ParSeq[(Int, Int, Long)] = {

    desiredPushArguments.par.flatMap(
      x => {
        x.sessionStreamsDelayFromAuth = 0
        sendSessionAndReturnDetails(x)
      })
  }

  def sendSessionAndReturnDetails(x: PusherParams): List[(Int, Int, Long)] = {
    val session = pushSessionParallely(x.recFile, x.authState, optionalPid = x.optionalPid,
      subsId = x.subsId, referrer = x.referrer, dnsName = x.dnsName)
    println(s"\n ***** Finished pushing: ${x.recFile} to: \n  " +
      s"pid = ${x.optionalPid} with sid = ${session.sid}\n")
    List((x.subsId, x.optionalPid, session.sid.toLong))
  }

  def returnAvro(sid: Long,
                 isQa: Boolean = false): Recording = {

    val client =
      HttpClientBuilder.create().build()
    val response =
      client.execute(new HttpGet(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/events/v1/$sid"))
    println(s"http://internal-nv-q-elb-int-cage-01-284466208.us-east-1.elb.amazonaws.com/events/v1/$sid")
    //val recordingAvro = Http(s"http://internal-nv-q-elb-int-cage-01-284466208.us-east-1.elb.amazonaws.com/events/v1/$sid")//.asBytes.body
    val recordingAvro = Http(s"http://internal-nv-q-elb-int-cage-01-284466208.us-east-1.elb.amazonaws.com/events/v1/$sid")
    //.asBytes.body
    val decodedBytes = Base64.getDecoder.decode(recordingAvro.asBytes.body)


    val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
    response.getEntity.writeTo(baos)
    val bytes: Array[Byte] = baos.toByteArray
    //val bla = Base64.decodeString(bytes)


    val serializer: AvroRecordingSerializer = new AvroRecordingSerializer()
    val versionedAvro: VersionedRecording = AvroVersionExtractor.extractVersionRecordingPair(new ByteArrayInputStream(decodedBytes))
    //val versionedAvro: VersionedRecording = AvroVersionExtractor.extractVersionRecordingPair(new ByteArrayInputStream(bytes))
    //val versionedAvro: VersionedRecording = AvroVersionExtractor.extractVersionRecordingPair(new ByteArrayInputStream(bytes))


    val deserializedRecording: Recording = serializer.deserialize(versionedAvro.recordingBytes)

    deserializedRecording
  }

  /*
      val request: BufferedSource = Source.fromURL(
        s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/events/v1/$sid")

      if (isQa) {
        Try(loadAvroFromCage(sid)) match {
          case Success(avroContent) =>
            println(avroContent)
          case Failure(e) =>
            println("\n ***** !!! ERROR - Failed Retrieving Avro from cage!!! ***** \n ***** !!! Info from the exception:\n" +
              e.getMessage)
        }
      }
      }
      */


  def avroContent(recordingAvro: Array[Byte]): String = {
    val serializer: AvroRecordingSerializer = new AvroRecordingSerializer()
    val versionedAvro = AvroVersionExtractor.extractVersionRecordingPair(new ByteArrayInputStream(recordingAvro))

    val deserializedRecording = serializer.deserialize(versionedAvro.recordingBytes)
    val generatedXml = RecordingXmlWriter.serializeRecoding(deserializedRecording)

    lazy val printer = new scala.xml.PrettyPrinter(1000, 2)

    val xmlData = printer.format(generatedXml)
    xmlData

  }

  def saveSessionArchiveToDisk(subsId: Int,
                               pid: Int,
                               sid: Long,
                               isQa: Boolean = false): Unit = {
    if (isQa) {
      Thread.sleep(9000)
      Try(loadFromCage(subsId, pid, sid)) match {
        case Success(archivedContent) =>
          archivedContent.writeArchivedContentToDisk(s"$archivedFilesFolder\\$sid")
          println("bla" + "\n" + archivedContent.events + "\n" + archivedContent.rawData + "\n" + "recording time from Json" + RecordingJson(archivedContent.recording).clientRecordingDateTimeUtc)
        case Failure(e) =>
          println("\n ***** !!! ERROR - Failed Retrieving data from cage!!! ***** \n ***** !!! Info from the exception:\n" +
            e.getMessage)
      }
    }

  }
}

