package com.clicktale.pipeline.regressions.tests.end2EndTests

import java.io.{File, FileWriter}
import java.lang.{System, _}

import com.clicktale.pipeline.dataObjects.{Project, Subscriber, _}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.helpers.{CsvReader, StringManipulator}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3.loadFromCage
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.apache.commons.io.FileUtils
import org.scalatest.{Tag, WordSpecLike}

import scala.xml.{Elem, XML}

class IPBlockingE2ETests extends WordSpecLike with StringManipulator {

  "Basic IP Blocking" taggedAs Tag("NotInTeamCity") in {
    if (!WhichTestsToRun().shouldRunIPBlockingTests) pending
    GeneralFunctions.deleteIPBlockingRule(223,26955)
    GeneralFunctions.createIPBlockingRule(223,26955,"{\"version\":1,\"rules\":[{\"disabled\":false,\"mode\":\"single\",\"start\":{\"address\":\"207.232.17.25\",\"maskbits\":\"32\",\"mask\":\"255.255.255.255\"}}]}")
    Thread.sleep(60000)
    var session = getAuth(conf.getString(s"WebRecorder.Session.States.NotRecording"), optionalPid = 26955, subsId = 223, referrer = "http://www.test.com")//, optionalUrlSuffix = "&X-Forwarded-For=207.232.17.25")
    assert(session.authorized.equals(false))
    assert(session.rejectReason.equals("IpBlocked"))
    GeneralFunctions.deleteIPBlockingRule(223,26955)
  }

  "Basic IP Range Blocking" taggedAs Tag("NotInTeamCity") in {
    if (!WhichTestsToRun().shouldRunIPBlockingTests) pending
    GeneralFunctions.deleteIPBlockingRule(223,26955)
    GeneralFunctions.createIPBlockingRule(223,26955,"{\"version\":1,\"rules\":[{\"disabled\":false,\"mode\":\"range\",\"start\":{\"address\":\"207.232.17.0\",\"maskbits\":\"32\",\"mask\":\"255.255.255.255\"},\"end\":{\"address\":\"207.232.17.200\",\"maskbits\":\"32\",\"mask\":\"255.255.255.255\"}}]}")
    Thread.sleep(60000)
    var session = getAuth(conf.getString(s"WebRecorder.Session.States.NotRecording"), optionalPid = 26955, subsId = 223, referrer = "http://www.test.com")//, optionalUrlSuffix = "&X-Forwarded-For=207.232.17.25")
    assert(session.authorized.equals(false))
    assert(session.rejectReason.equals("IpBlocked"))
    GeneralFunctions.deleteIPBlockingRule(223,26955)
  }

  "Basic IP Subnet Blocking" taggedAs Tag("NotInTeamCity") in {
    if (!WhichTestsToRun().shouldRunIPBlockingTests) pending
    GeneralFunctions.deleteIPBlockingRule(223,26955)
    GeneralFunctions.createIPBlockingRule(223,26955,"{\"version\":1,\"rules\":[{\"disabled\":false,\"mode\":\"subnet\",\"start\":{\"address\":\"207.232.17.25\",\"maskbits\":\"29\",\"mask\":\"255.255.255.248\"}}]}")
    Thread.sleep(60000)
    var session = getAuth(conf.getString(s"WebRecorder.Session.States.NotRecording"), optionalPid = 26955, subsId = 223, referrer = "http://www.test.com")//, optionalUrlSuffix = "&X-Forwarded-For=207.232.17.25")
    assert(session.authorized.equals(false))
    assert(session.rejectReason.equals("IpBlocked"))
    GeneralFunctions.deleteIPBlockingRule(223,26955)
  }
}