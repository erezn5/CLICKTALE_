package com.clicktale.pipeline.regressions.tests.quotaManagementApiTests

import com.clicktale.pipeline.framework.dal.GeneralFunctions
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.{FlatSpec, MustMatchers, Tag}

import scala.io.Source
import scalaj.http.{Http, _}

class QuotaManagementApiTests extends FlatSpec with MustMatchers {

  "QM API" should "Return 200/500 to the right Scenario" taggedAs Tag("NotInTeamCity") in { pending
    val fileContents: String =
      Source.fromInputStream(getClass.getResourceAsStream("test.json")).
        getLines.mkString

    val recStreams: JValue =
      parse(fileContents)

    val multiRuleJsonBad1 =
      s"""
          [{
            "ruleId": 12344,
            "subscriberId": 233150,
            "projectId": 39001,
            "url": "www.asiaApreciateAviv.com",
            "isIgnoreQueryString": true,
            "ruleType": 2,
            "ratio": 0.222
          },
          {
             "ruleId": 0,
             "subscriberId": 233150,
             "projectId": 39001,
             "url": "www.AvivAp123reciatesAsia.com",
             "isIgnoreQueryString": true,
             "ruleType": 2,
             "ratio": 0.7
          }]
      """

    val multiRulenewVersion =
      s"""
          [{
            "ruleId": 12344,
            "subscriberId": 233150,
            "projectId": 39001,
            "url": "www.asiaApreciateAviv.com",
            "isIgnoreQueryString": true,
            "ruleType": 2,
            "ratio": 0.222
          },
          {
             "subscriberId": 233150,
             "projectId": 39001,
             "url": "www.AvivAp123reciatesAsia.com",
             "isIgnoreQueryString": true,
             "ruleType": 2,
             "ratio": 0.7
          }]
      """
    val multiRuleJsonBad2 =
      s"""
          [{
            "ruleId": 616,
            "subscriberId": 233150,
            "projectId": 39001,
            "url": "www.asiaApreciateAviv.com",
            "isIgnoreQueryString": true,
            "ruleType": 2,
            "ratio": 0.8
          },
          {
             "ruleId": 0,
             "subscriberId": 233150,
             "projectId": 38964,
             "url": "www.Av123ivApreciatesAsia.com",
             "isIgnoreQueryString": true,
             "ruleType": 2,
             "ratio": 0.7
          }]
      """
    val multiRuleJson =
      s"""
          [{
            "ruleId": 617,
            "subscriberId": 233150,
            "projectId": 39001,
            "url": "www.asiaApreciateAviv.com",
            "isIgnoreQueryString": true,
            "ruleType": 2,
            "ratio": 0.55
          },
          {
             "ruleId": 0,
             "subscriberId": 233150,
             "projectId": 39001,
             "url": "www.AvivApr123eciatesAsia22.com",
             "isIgnoreQueryString": true,
             "ruleType": 2,
             "ratio": 0.7
          }]
      """

    val oneIsBad =
      s"""
          [{
            "ruleId": 51554,
            "subscriberId": 233150,
            "projectId": 39001,
            "url": "www.asiaApreciateAviv.com",
            "isIgnoreQueryString": "true",
            "ruleType": 2,
            "ratio": 0.222
          },
          {
             "ruleId": 0,
             "subscriberId": 233150,
             "projectId": 39001,
             "url": "www.AviIdanIdanvApr123eciatesAsia22.com",
             "isIgnoreQueryString": true,
             "ruleType": 2,
             "ratio": 55
          }]
      """

    val wrongUrlString =
      s"""
          [{
             "ruleId": 0,
             "subscriberId": 233150,
             "projectId": 39001,
             "url": "www.AvivAp1AvivAp123reciatesAsiaAvivAp123recivivAp1AvivAp123reciatesAsiaAvivAp123recivivAp1AvivAp123reciatesAsiaAvivAp123recivivAp1AvivAp123reciatesAsiaAvivAp123recivivAp1AvivAp123reciatesAsiaAvivAp123recivivAp1AvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsia23reciaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiaAvivAp123reciatesAsiatesAsia.com",
             "isIgnoreQueryString": "true",
             "ruleType": 2,
             "ratio": 0.7
          }]
      """

    val newVersion =
      s"""
          [{
             "ruleId": 0,
             "subscriberId": 232926,
             "projectId": 111,
             "url": "www.abalsld.com",
             "isIgnoreQueryString": "true",
             "ruleType": 2,
             "ratio": 0.7
          }]
      """

    val response1: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/fetch-all/232926/111").
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    println(response1.body)
    /*
//Added new SubscriberID's: 233150, 232622, 232926, 232959
    val responseFalse1: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/edit").postData(multiRuleJsonBad1).
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    val responseFalse2: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/edit").postData(multiRuleJsonBad2).
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    val badRequestInSet: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/edit").postData(oneIsBad).
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    val longURL: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/edit").postData(wrongUrlString).
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    val response: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/edit").postData(multiRuleJson).
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    val response222: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/edit").postData(newVersion).
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    val response1: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/fetch-all/232926/111").
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    println(response1.body)
    val response2: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/delete/233150/39001/616").
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString
    val response3: HttpResponse[String] =
      Http("http://172.22.5.221:8080/api/delete/233150/39001/2212212").
        header("Authentication", "Microsoft=QpQJ,bNwlqeAl/PA@").asString

    println(response222.body)
    ///println(response2.body)
    assert(response2.code == 500)
    assert(response3.code == 500)
    assert(response1.code == 200)
    assert(responseFalse2.code == 500)
    assert(responseFalse1.code == 500)
    assert(response.code == 200)
    assert(badRequestInSet.code == 500)
    assert(longURL.code == 500)
    println(response.body)
    println(response2.body)
    assert(response1.toString.count(x => x=='{') == GeneralFunctions.getRulesCount(39001,233150,1))
  }
*/
}}
