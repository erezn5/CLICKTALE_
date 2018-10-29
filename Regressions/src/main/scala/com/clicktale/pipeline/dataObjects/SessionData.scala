package com.clicktale.pipeline.dataObjects

class SessionData(val testName: String,
                  val recFileName: String,
                  val authDetail: AuthResponse,
                  val projectId: Int,
                  val subscriberId: Int) {

  //Override toString method
  override def toString: String = {
        "Test name:" + testName +
        "Recording file name:" + recFileName +
        authDetail.toString +
        "Pid + Subsid:" + projectId + subscriberId}
}

object SessionData {
  def apply(testName: String, recFileName: String, authDetail: AuthResponse, projectId: Int, subscriberId: Int): SessionData =
    new SessionData(testName, recFileName, authDetail, projectId, subscriberId)
}