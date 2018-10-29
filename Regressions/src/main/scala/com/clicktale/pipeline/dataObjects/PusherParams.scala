package com.clicktale.pipeline.dataObjects

import com.clicktale.pipeline.framework.dal.ConfigParser.conf

case class PusherParams(var recFile:String = "/recFiles/recEvent.json",
                        var authState:String = conf.getString("WebRecorder.Session.States.Recording"),
                        var optionalPid : Int = 26955,
                        var optionalMobile : Boolean = false,
                        var subsId : Int = 223,
                        var domain: String = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"),
                        var retry:Boolean = true,
                        var referrer:String = "",
                        var dnsName: String = "https://wrlinqa-aws.clicktale.net/ctn_v2/",
                        var ua: String = "",
                        var isProtocolV16: Boolean = false,
                        var messageCounter:Int = 0,
                        var sessionStreamsDelayFromAuth: Int = 0,
                        var optionalSuffix: String = "") {

  def getRecFile(): String ={
    return recFile
  }
}

