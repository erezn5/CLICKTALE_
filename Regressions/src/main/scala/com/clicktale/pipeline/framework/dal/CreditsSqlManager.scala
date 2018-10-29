package com.clicktale.pipeline.framework.dal

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.helpers.SqlServerConnectionStringParser
import org.joda.time.DateTime
import scalikejdbc._

object CreditsSqlManager extends SqlServerConnectionStringParser {

  /**
    * A wrapper for the SQL entry which was pulled from projectCredits DB
    *
    * @param subscriberId The subscriber id.
    * @param projectId The PID.
    * @param currentAmount Current amount of credits.
    * @param createDate The date this entry was created.
    */
  case class EntryProjectCredits(subscriberId: Int, projectId: Int, currentAmount: Long, createDate: DateTime)

  scalikejdbc.config.DBs.loadGlobalSettings()
  Class.forName(conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Driver"))

  /**
    * Makes the connection to the SQL server and adds a connection pool entry.
    */
  def connect(): Unit = {
    val connectionString = s"Data Source=${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.DataSource")}:${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Port")};Initial Catalog=CoreRecordings;user id=${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.UserId")};password=${conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Password")}"//ZookeeperManager.getValue("ProjectCreditsRepositoryConnectionString")
    val (jdbcUrl, usedId, pass) = parseSqlServerConnectionString(connectionString)
    ConnectionPool.add(conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Host"), jdbcUrl, usedId, pass)
  }

  /**
    * Pulls the credit amount of a specific PID using the EntryProjectCredits as a wrapper.
    *
    * @param specificPid The specific PID to search in the DB
    * @return The current amount of credits.
    */
  def getCreditsForSpecificPid(specificPid: Int): Int = {
    NamedDB(conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Host")) readOnly { implicit session =>
      object Entry extends SQLSyntaxSupport[EntryProjectCredits] {
        override val tableName = "ProjectCredits"

        def apply(rs: WrappedResultSet) = EntryProjectCredits(
          rs.int("SubscriberId"),
          rs.int("ProjectId"),
          rs.long("RandomCreditAmount"),
          rs.jodaDateTime("CreateDate"))
      }
      val entries: List[EntryProjectCredits] = sql"select * from ProjectCredits where ProjectId = $specificPid"
        .map(rs => Entry(rs)).list.apply()
      if (entries.isEmpty)
        0
      else
        entries.head.currentAmount.toInt
    }
  }

  def getCreditsForSpecificPidAndSubsId(specificPid: Int,subsId: Int): Long = {
    NamedDB(conf.getString(s"WebRecorder.Sql.${conf.getString("WebRecorder.Current.Environment")}.CoreRecordings.Host")) readOnly { implicit session =>
      object Entry extends SQLSyntaxSupport[EntryProjectCredits] {
        override val tableName = "ProjectCredits"

        def apply(rs2: WrappedResultSet) = EntryProjectCredits(
          rs2.int("SubscriberId"),
          rs2.int("ProjectId"),
          rs2.long("RandomCreditAmount"),
          rs2.jodaDateTime("CreateDate"))
      }

      val entries: List[EntryProjectCredits] = sql"select * from ProjectCredits where ProjectId = $specificPid and SubscriberId = $subsId"
        .map(rs2 => Entry(rs2)).list.apply()
      entries.head.currentAmount
    }

  }

}
