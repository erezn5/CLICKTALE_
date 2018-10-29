package com.clicktale.pipeline.framework.dal

import java.sql.{Connection, DriverManager, ResultSet}

import com.clicktale.pipeline.common.utils.SqlServerConnectionStringParser
import com.clicktale.pipeline.framework.dal.ConfigParser.conf

object CoreRecordingsSqlManager extends SqlServerConnectionStringParser {

  val driver: String = conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Driver")
  val url = s"jdbc:sqlserver://${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.DataSource")}\\${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.DataTable")}:${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Port")};user=${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.UserId")};password=${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Password")}"

  Class.forName(driver)
  val connection: Connection = DriverManager.getConnection(url)

  def Select(query : String) : ResultSet = {
    val statement = connection.createStatement()
    statement.executeQuery(query)
  }

  def Insert(query : String) : Unit = {
    val statement = connection.createStatement()
    statement.executeUpdate(query)
  }

  def Update(query : String) : Unit = {
    val statement = connection.createStatement()
    statement.executeUpdate(query)
  }

  def Delete(query : String) : Unit = {
    val statement = connection.createStatement()
    statement.executeQuery(query)
  }

}
