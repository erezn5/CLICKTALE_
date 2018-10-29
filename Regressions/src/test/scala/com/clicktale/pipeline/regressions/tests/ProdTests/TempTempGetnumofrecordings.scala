package com.clicktale.pipeline.regressions.tests.ProdTests

import System.IO.File
import com.clicktale.pipeline.framework.dal.Vertica
import org.scalatest._

class TempTempGetnumofrecordings extends WordSpecLike with BeforeAndAfterAll {
  "GetFromVertica Prod US" taggedAs (Tag.apply("US")) in {
    val aaaaa = File.ReadAllText("c:\\aaa\\111.csv").split("\n")
    var pid: String = ""
    var subsId: String = ""
    aaaaa.foreach {
      ent =>
        //if (!(ent.split(',')(0).contains("SubsId"))) {
          try {
            subsId = ent.split(',')(0)
            pid = ent.split(',')(1)
            //val to = ent.split(',')(6)
            //val from = ent.split(',')(5)
            //if (!File.Exists(s"c:\\aaa\\results\\US\\new\\$subsId-$pid.csv")) {
            Vertica.connect("slvc1.clicktale.net")
            val count = Vertica.selectCount(s"select count(*) from CT1Analytics.Recordings where CreateDate>'3/3/2018 00:00' and CreateDate<'3/4/2018 00:00' and SubscriberID=$subsId and pid=$pid")
            //val sids2 = Vertica.selectCount(s"select count(*) from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SubscriberID=$subsId and pid=$pid")
            Vertica.quit
            var res = ""
            var i = 0
            if (count.toInt > 0) {
              File.AppendAllText("1111.csv", subsId + "," + pid + "\n")
              println(subsId + " " + pid + " do manual - " + count)
            }
          }
          catch {
            case ex: Exception => {
              println("exception in subsid " + subsId + " pid " + pid)
              println(ex.toString)
            }
          //}
        }

    }

  }

  "GetFromVertica Prod EU" taggedAs (Tag.apply("EU")) in {
    val aaaaa = File.ReadAllText("c:\\aaa\\results_per_day_EU.csv").split("\n")
    var pid: String = ""
    var subsId: String = ""
    aaaaa.foreach {
      ent =>
        if (!(ent.split(',')(0).contains("SubsId"))) {
          try {
            subsId = ent.split(',')(0)
            pid = ent.split(',')(1)
            val to = ent.split(',')(6)
            val from = ent.split(',')(5)
            if (!File.Exists(s"c:\\aaa\\results\\EU\\$subsId-$pid.csv")) {
              Vertica.connect("amvc1.clicktale.net")
              val sids = Vertica.select(s"select UID,SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SubscriberID=$subsId and pid=$pid")
              Vertica.quit
              var res = ""
              var i = 0
              if (sids.size < 20000) {
                var lala = ",".split(',')
                for (i <- 0 to sids.size - 1) {
                  if (sids(i).length > 0) {
                    lala = sids(i).split(',')
                    res += lala(0) + "," + lala(1) + "\n"
                  }
                }
                File.AppendAllText(s"c:\\aaa\\results\\EU\\$subsId-$pid.csv", res)
              }
              else
                println("run manually" + subsId + " " + pid)
            }
          }
          catch {
            case ex: Exception => {
              println("exception in subsid " + subsId + " pid " + pid)
              println(ex.toString)
            }
          }
        }
    }
  }
}
