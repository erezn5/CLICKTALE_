package com.clicktale.pipeline.regressions.tests.functionalTests.crossPipelineFunctionalTests

import com.clicktale.pipeline.framework.queue
import org.scalatest.{BeforeAndAfterAll, Tag, WordSpecLike}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun

class AssetsManagerTests extends WordSpecLike with BeforeAndAfterAll {



  "Check Recording" in {
    if (!WhichTestsToRun().shouldRunTest) pending
    val kafka = new queue.KafkaClient()
    val messages = kafka.consume(Traversable("proc_1001_26952"))
    assert(messages.nonEmpty)
  }
}
