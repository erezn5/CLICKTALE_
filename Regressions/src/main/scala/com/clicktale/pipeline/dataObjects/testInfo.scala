package com.clicktale.pipeline.dataObjects

class testInfo(val testName: String,
               val testResults: Boolean,
               val ErrorData: String) {

  //Override toString method
  override def toString: String = {
    "Test name: " + testName +
    "Test results: " + testResults +
      "Recording file name: " + ErrorData}
}

object testInfo {
  def apply(testName: String, testResults: Boolean = true, ErrorData:String = ""): testInfo =
    new testInfo(testName: String, testResults: Boolean, ErrorData:String)
}
