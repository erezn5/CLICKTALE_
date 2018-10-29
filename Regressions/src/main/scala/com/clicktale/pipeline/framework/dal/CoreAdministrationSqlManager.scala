package com.clicktale.pipeline.framework.dal

import java.sql.{Connection, DriverManager, ResultSet}

import com.clicktale.pipeline.common.utils.SqlServerConnectionStringParser
import ConfigParser.conf
import org.scalatest.FunSuite

object CoreAdministrationSqlManager extends SqlServerConnectionStringParser {

  var driver = conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreAdministration.Driver")
  var url = s"jdbc:sqlserver://${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreAdministration.DataSource")}\\${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreAdministration.DataTable")}:${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreAdministration.Port")};user=${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreAdministration.UserId")};password=${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreAdministration.Password")}"

  Class.forName(driver)
  var connection = DriverManager.getConnection(url)

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
