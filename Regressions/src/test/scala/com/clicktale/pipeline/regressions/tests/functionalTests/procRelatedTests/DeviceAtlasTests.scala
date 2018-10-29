package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests
import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, RecordingJson}
import com.clicktale.pipeline.dataObjects.RecordingXmlMobile
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.scalatest.WordSpecLike


class DeviceAtlasTests extends WordSpecLike {

  val testsPid: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt

  val testsSubsId: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt

  val states = Map(
    "XperiaZ3" ->  deviceFeatures(deviceName = conf.getString("WebRecorder.Devices.Xperia.DeviceName"), ua = conf.getString("WebRecorder.Devices.Xperia.UA"), deviceWeight = conf.getString("WebRecorder.Devices.Xperia.DisplayWidth").toInt, deviceHeight = conf.getString("WebRecorder.Devices.Xperia.DisplayHeight").toInt),
    "iPhone6" ->  deviceFeatures(deviceName = conf.getString("WebRecorder.Devices.Iphone.DeviceName"), ua = conf.getString("WebRecorder.Devices.Iphone.UA"), deviceWeight = conf.getString("WebRecorder.Devices.Iphone.DisplayWidth").toInt, deviceHeight = conf.getString("WebRecorder.Devices.Iphone.DisplayHeight").toInt),
    "SamsungGalaxyS4" ->  deviceFeatures(deviceName = conf.getString("WebRecorder.Devices.Galaxy.DeviceName"), ua = conf.getString("WebRecorder.Devices.Galaxy.UA"), deviceWeight = conf.getString("WebRecorder.Devices.Galaxy.DisplayWidth").toInt, deviceHeight = conf.getString("WebRecorder.Devices.Galaxy.DisplayHeight").toInt),
    "iPad" -> deviceFeatures(deviceName = conf.getString("WebRecorder.Devices.Ipad.DeviceName"), ua = conf.getString("WebRecorder.Devices.Ipad.UA"), deviceWeight = conf.getString("WebRecorder.Devices.Ipad.DisplayWidth").toInt, deviceHeight = conf.getString("WebRecorder.Devices.Ipad.DisplayHeight").toInt))

  states.foreach((x: (String, deviceFeatures)) => runDdrTestSuite(x))

  def dDrXmlTestSuite(archivedPackage: CageArchivePackage,
                      desiredDeviceName: String,
                      dispW: Int,
                      dispH: Int): Unit ={

    val storageXml = archivedPackage.events
    val recordingXml = RecordingXmlMobile(storageXml)

    assert(recordingXml.device == desiredDeviceName, "Device type is not D6603 Xperia Z3")
    assert(recordingXml.isRecorderMobile == "true", "Not recording mobile")
    assert(recordingXml.physicalDisplayWidth.toInt == dispW, "DisplayWidths are not equal")
    assert(recordingXml.physicalDisplayHeight.toInt == dispH, "Displayheights are not equal")
  }

  def dDrJsonTestSuite(archivedPackage: CageArchivePackage,
                      dispW: Int,
                      dispH: Int): Unit = {

    val storageJson = archivedPackage.recording

    val recordingJson = RecordingJson(storageJson)

    assert(recordingJson.recorderIsMobile, "Recorder is mobile is false")
    assert(recordingJson.physicalDisplayHeight.get == dispH, "DisplayHeights are not equal")
    assert(recordingJson.physicalDisplayWidth.get == dispW, "DisplayWidths are not equal")

  }

  def runDdrTestSuite(x: (String, deviceFeatures)): Unit ={
      x._1+"tests" should {
        if (x._2.isPending) pending

        val authResponse: AuthResponse =
          //pushSessionParallely("/recFiles/rec" + x._1 + ".json", conf.getString("WebRecorder.Session.States.Recording"), optionalPid = testsPid, ua = x._2.ua, optionalMobile = true)
        pushSessionParallely("/recFiles/rec" + x._1 + ".json", conf.getString("WebRecorder.Session.States.Recording"),subsId =testsSubsId, optionalPid = testsPid, ua = x._2.ua, optionalMobile = true)
        Thread.sleep(2000)

        val archivedPackage: CageArchivePackage =
          loadFromCage(testsSubsId, testsPid,authResponse.sid.toLong)

        x._1+" Json Test" in {
          dDrJsonTestSuite(archivedPackage, x._2.deviceWeight, x._2.deviceHeight)
        }

        x._1+" XmlTest" in {
          dDrXmlTestSuite(archivedPackage, x._2.deviceName, x._2.deviceWeight, x._2.deviceHeight)
        }
      }
  }
}
case class deviceFeatures(deviceName: String,
                          ua: String,
                          deviceWeight: Int,
                          deviceHeight: Int,
                          isPending: Boolean = false)

