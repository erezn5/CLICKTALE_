package com.clicktale.pipeline.dataObjects

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.google.gson.Gson


class AuthResponse(  val ip: String,
                     val authorized: Boolean,
                     val sid: String,
                     val uid: String,
                     val ratio: Double,
                     val skipRecording: Boolean,
                     val rejectReason: String,
                     val userTrackingState: String,
                     val pid: Int = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt) {
  // If we write val before input vars it constract the same values

  //Override toString method
  override def toString: String = {
        "ip: " + ip + "\n" +
        "authorized: " + authorized + "\n" +
        "sid: " + sid + "\n" +
        "uid: " + uid + "\n" +
        "ratio: " + ratio + "\n" +
        "skipRecording: " + skipRecording + "\n" +
        "rejectReason: " + rejectReason + "\n" +
          "pid: " + pid + "\n" +
        "userTrackingState: " + userTrackingState
  }
}

object AuthResponse {

  def apply(ip: String, authorized: Boolean, sid: String, uid: String, ratio: Double, skipRecording: Boolean, rejectReason: String, userTrackingState: String): AuthResponse =
    new AuthResponse(ip, authorized, sid, uid, ratio, skipRecording, rejectReason, userTrackingState)

  def apply(responseAsJson: String): AuthResponse =  {
    new Gson().fromJson(responseAsJson, classOf[AuthResponse])

  }
}