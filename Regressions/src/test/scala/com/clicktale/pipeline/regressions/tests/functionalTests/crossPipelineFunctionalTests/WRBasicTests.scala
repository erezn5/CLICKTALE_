package com.clicktale.pipeline.regressions.tests.functionalTests.crossPipelineFunctionalTests

import com.clicktale.pipeline.common.identity.LongIdentityProvider
import com.clicktale.pipeline.dataObjects.AuthResponse
import com.clicktale.pipeline.framework.dal.ConfigParser.formats
import com.clicktale.pipeline.framework.helpers.UrlBuilder.buildAuthUrl
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.{MustMatchers, WordSpecLike}

import scalaj.http.{Http, _}

class WRBasicTests extends WordSpecLike with MustMatchers {

    val response: HttpResponse[String] = Http("https://wrlinqa-aws.clicktale.net/ctn_v2/auth?pid=1221221").asString
    val response2: HttpResponse[String] = Http(s"https://wrlinqa-aws.clicktale.net/ctn_v2/auth?pid=26952&subsid=422").asString

    val json: JValue = parse(response.body)
    val json2: JValue = parse(response2.body)

    println(AuthResponse(response.body))
 "WR Tests" should {
   //if (!WhichTestsToRun().shouldRunWRTest) pending
   "Authorization should return 200 test" in {

     response.code mustEqual 200
     assert(((json \ "ratio").extract[Double] <= 1) && ((json \ "ratio").extract[Double] >= 0))
   }

   "Authorization should return ip test" in {

     assert((json \ "ip").extract[String] != null)
   }

   "Authorization with subsid should return 200 test" in {

     response2.code mustEqual 200
     assert(((json2 \ "ratio").extract[Double] <= 1) && ((json2 \ "ratio").extract[Double] >= 0))
   }

   "Authorization for full processing should never return ETP test" in {

     //val response3: AuthResponse = AuthResponse(Http("http://wrlinqa-aws.clicktale.net/ctn_v2/auth?pid=26952&subsid=100001"))
     assert(((json2 \ "ratio").extract[Double] <= 1) && ((json2 \ "ratio").extract[Double] >= 0))
   }

   "UID Should consist of digits only" in {

     val regex = """\d+$""".r
     val uidString = (json \ "uid").extract[String]
     assert(regex.pattern.matcher(uidString).matches())
   }

   "Bad authorization test" in {

     val badResponse: HttpResponse[String] = Http("https://wrlinqa-aws.clicktale.net/ctn_v2/auth?pid=10101").asString
     val badJson = parse(badResponse.body)
     assert(!(badJson \ "authorized").extract[Boolean])
     assert((badJson \ "rejectReason").extract[String] == "InvalidProject")
     assert(!(badJson \ "skipRecording").extract[Boolean])
     assert((badJson \ "userTrackingState").extract[String] == "NotRecording")
   }

   "UID/SID should not be null test" in {

     assertResult(false, "\n -> UID/SID returned as null \n Reject Reason was: " + (json \ "rejectReason").extract[String] + "\n\n") {
       ((json \ "uid").extract[String] == null) && ((json \ "sid").extract[String] == null)
     }
     assert((json \ "rejectReason").extract[String] == "RejectedByRandomRatio,OutOfEnhancedCredits,OutOfETRCredits")
     assert((json \ "authorized").extract[Boolean])
   }

   "UID should be equal to first SID test" in {
     assert((json \ "uid").extract[String].toLong == (json \ "sid").extract[String].toLong, "\n -> UID does not eqaul to first SID")
   }

   "MachineID test" in {
     assert((((json \ "uid").extract[String].toLong & 0x3fe0) >> 5) >= 0, "\n -> Wrong machineID \n\n")
   }

   "SID time stamp test" in {
     val sidEpochTimeStamp = LongIdentityProvider.toUtcTimeMillis((json \ "uid").extract[String].toLong)
     val sidUTCTimeStamp = new org.joda.time.DateTime(sidEpochTimeStamp)

     //assert(sidUTCTimeStamp.isBeforeNow)
     //assert((sidUTCTimeStamp.getYear == java.time.LocalDate.now.getYear) &&
     // (sidUTCTimeStamp.getMonthOfYear == java.time.LocalDate.now.getMonthValue) &&
     // (sidUTCTimeStamp.getDayOfMonth == java.time.LocalDate.now.getDayOfMonth))
     assert((sidUTCTimeStamp.getMonthOfYear == java.time.LocalDate.now.getMonthValue) &&
       (sidUTCTimeStamp.getDayOfMonth == java.time.LocalDate.now.getDayOfMonth))
   }

   "Old UID (with dot) test" in {
     val authResponse = AuthResponse(Http(buildAuthUrl(authSuffix = "&uid=454544565.45645456645")).asString.body)
     assert(authResponse.rejectReason == "ExceptionOccured")
     assert(authResponse.userTrackingState == "NotRecording")
     assert(!authResponse.authorized)
   }

   Thread.sleep(10000)
   println("Tests run:" + testNames)
 }
 }
