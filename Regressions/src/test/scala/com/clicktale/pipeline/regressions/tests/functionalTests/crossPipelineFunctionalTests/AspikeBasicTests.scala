package com.clicktale.pipeline.regressions.tests.functionalTests.crossPipelineFunctionalTests

import java.io.DataInputStream
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.zip.ZipInputStream

import com.aerospike.client.{AerospikeClient, Key}
import com.aerospike.client.policy.WritePolicy
import com.clicktale.pipeline.common.dal.SessionsRepositoryDefs.SessionDetails
import com.clicktale.pipeline.dataObjects.{AuthResponse, SessionStream}
import com.clicktale.pipeline.framework.dal.AerospikeManager
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.senders.SendSession._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.WordSpecLike

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import io.protostuff.Tag
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.junit.Ignore
import org.scalatest.tagobjects._
class AspikeBasicTests extends WordSpecLike with StringManipulator {

  import com.clicktale.pipeline.framework.dal.ZookeeperManager

  val projectId : Int = 223098
  val authResponse: AuthResponse = pushSession("/recFiles/recMissingEndMessage.json", conf.getString("WebRecorder.Session.States.Recording"), optionalPid = projectId, subsId = 223)
  val sidFromResponse: Long = authResponse.sid.toLong
  val futureSessionDetails: Future[SessionDetails] = AerospikeManager.getSessionDetails(sidFromResponse)
  val sessionDetails: SessionDetails = Await.result[SessionDetails](futureSessionDetails, 10.seconds)

  "Check SID In AS" in {
    assert (sessionDetails.metadata.sessionId == sidFromResponse, "Session ID from AS not equal to Session ID in response")
  }

  "Check UID In AS" in {
    val uidFromResponse = authResponse.uid.toLong
    assert(sessionDetails.metadata.userId == uidFromResponse, "User ID from AS not equal to User ID in response")
  }

  "Check PID In AS" in {
    assert(sessionDetails.metadata.projectId == projectId, "Project ID from AS not equal to Project ID in config")
  }

  "Check User Agent In AS" in {
    assert(sessionDetails.metadata.userAgent contains conf.getString("WebRecorder.Scala.Http.PartialUserAgent"), "User Agent from AS not equal to Scala UA")
  }

  "Check QueryStrings And Body Of Streams In AS" in {
    val fileContentAsString =
      Source.fromInputStream(getClass.getResourceAsStream("/recFiles/recMissingEndMessage.json")).
        getLines.mkString
    val recStreams = parse(fileContentAsString)
    val expectedStringList = (recStreams \ "LiveSessionDataCollection")
      .extract[List[JObject]]
      .map(c => SessionStream(c).data)
    val asStringList = sessionDetails.messages.map(message =>
      if (!conf.getString("WebRecorder.Session.ExcludedDataFlagTypes").contains("," + message.dataFlags.toString + ","))
        extractDataString(message.queryString)
      else new String(Base64.getEncoder.encodeToString(message.body)))
    assert(expectedStringList.intersect(asStringList).size == Math.max(expectedStringList.size, asStringList.size), "Streams are not equal") // Equality checks inner elements for lists
  }

  "Check That Session Has Been Removed From AS" in {
    Try[Int](ZookeeperManager.getValue(conf.getString(s"WebRecorder.Aerospike.${conf.getString("WebRecorder.Current.Environment")}.Attributes.SessionExpiration")).toInt) match {
      case Success(expirationMinutes) => {
        println(s"Will wait ${expirationMinutes * 60000 / 1000 / 60} minutes until session is deleted from AS")
        Thread.sleep(expirationMinutes * 60000)
      }
      case Failure(f) => fail(f)
    }
    val futureSessionDetailsShouldFail = AerospikeManager.getSessionDetails(sidFromResponse)
    import com.aerospike.client.AerospikeException
    try {
      Await.result[SessionDetails](futureSessionDetailsShouldFail, 10.seconds)
      fail("Session was still found in AS after AeroSpikeSessionsExpirationMins expired")
    } catch {
      case e: AerospikeException => println("Session was removed correctly")
    }
  }

  import com.clicktale.pipeline.framework.storage.GetFromS3._

  "Check That Session Has Been Processed" in {
    assert(checkIfBucketExistsInS3(authResponse), "The session was not processed after AeroSpikeSessionsExpirationMins passed")
  }

  "Aspike Basic Tests" should {
    if (!WhichTestsToRun().shouldRunTest) pending

    "Check user tracking" in {

      //    val res = AerospikeManager.getUserTrackingState(1304290950742016L)
      val aerospikeClient = new AerospikeClient("172.22.3.202", 3000)
      val res = aerospikeClient.get(new WritePolicy(), new Key("pipeline", "UserTracking", 1304290950742016L))
      val expirationDate = LocalDateTime.of(2010, 1, 1, 0, 0, 0).plus(res.expiration, ChronoUnit.SECONDS)
      println(expirationDate)
    }

    "Check user tracking22" in {

      //    val res = AerospikeManager.getUserTrackingState(1304290950742016L)
      val aerospikeClient = new AerospikeClient("172.27.9.228", 3000)
      val res = aerospikeClient.get(new WritePolicy(), new Key("recordings", "events", 1304420854874125L))
      val expirationDate = LocalDateTime.of(2010, 1, 1, 0, 0, 0).plus(res.expiration, ChronoUnit.SECONDS)
      println(expirationDate)
    }
  }
}
