package com.clicktale.pipeline.regressions.testHelpers

import com.clicktale.pipeline.dataObjects.PusherParams

case class TestData(var testName: String,
                    sessionParams: PusherParams,
                    expectedRecFilePaths: ExpectedRecFilePaths = ExpectedRecFilePaths("","","",""),
                    var expectedPopulationType: String = null,
                    var laggingWaitingTime:Int = 0) {
}

case class ExpectedRecFilePaths(var pathToDsr: String,
                                var pathToXml: String,
                                var pathToJson: String,
                                var pathToHtml: String){}