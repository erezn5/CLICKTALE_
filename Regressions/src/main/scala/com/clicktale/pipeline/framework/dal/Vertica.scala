package com.clicktale.pipeline.framework.dal
import java.sql.{Connection, DriverManager}
import scala.collection.mutable.ArrayBuffer


object Vertica {

  var conn:Connection = null

  def connect(host:String): Unit ={
    Class.forName("com.vertica.jdbc.Driver")
    conn = DriverManager.getConnection(s"jdbc:vertica://$host:5433/CTN", "read_only_user", "12345")
  }

  def quit: Unit = {
    conn.close()
  }

  def select(query:String): ArrayBuffer[String] = {
    try {
      // create the statement, and run the select query
      val statement = conn.createStatement()
      val resultSet = statement.executeQuery(query)
      val ret = ArrayBuffer[String]()
      while ( resultSet.next() ) {
        var str:String = ""
        for (a <- 1 to resultSet.getMetaData.getColumnCount) {
          val temp = resultSet.getString(a)
          if (temp.length>=14) {
            str += resultSet.getString(a)
            if (!(a == resultSet.getMetaData.getColumnCount))
              str += ","
          }
        }
        ret+=str
      }
      ret
    } catch {
      case e => e.printStackTrace; quit; new ArrayBuffer[String]
    }

  }

  def selectCount(query:String): String = {
    try {
      // create the statement, and run the select query
      val statement = conn.createStatement()
      val resultSet = statement.executeQuery(query)
      val ret = ArrayBuffer[String]()
      resultSet.next()
      return resultSet.getString(1)
    }
    catch {
      case e => e.printStackTrace; quit; new String
    }

  }

}