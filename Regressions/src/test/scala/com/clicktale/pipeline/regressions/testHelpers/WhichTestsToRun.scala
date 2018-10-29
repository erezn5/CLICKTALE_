package com.clicktale.pipeline.regressions.testHelpers

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
case class WhichTestsToRun(  final val shouldRunTest: Boolean = conf.getString("WebRecorder.TestRunningFlag.flag").toBoolean,
                             final val shouldRunWRTest: Boolean = conf.getString("WebRecorder.TestRunningFlag.WRflag").toBoolean,
                             final val shouldRunQuotaManagementBasicTests: Boolean = conf.getString("WebRecorder.TestRunningFlag.QuotaManagementBasicflag").toBoolean,
                             final val shouldRunBackOfficeSubscribersTests: Boolean = conf.getString("WebRecorder.TestRunningFlag.shouldRunBackOfficeSubscribersflag").toBoolean,
                             final val shouldRunIPBlockingTests: Boolean = conf.getString("WebRecorder.TestRunningFlag.shouldRunIPBlockingTests").toBoolean
                          ) {

}
