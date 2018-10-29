package com.clicktale.pipeline.framework.dal

import com.clicktale.pipeline.common.aerospike.AerospikeClientAdapter
import com.clicktale.pipeline.common.dal.AerospikeSessionsRepository
import ConfigParser.{conf, formats}
import com.aerospike.client.{AerospikeClient, Key, policy}
import com.aerospike.client.policy.WritePolicy
import com.clicktale.pipeline.common.dal.SessionsRepositoryDefs.SessionDetails
import com.clicktale.pipeline.framework.dal.AerospikeManager.aerospikeClient

import scala.concurrent.Future
import scala.util.matching.Regex

object AerospikeManager {

  val aerospikeNodes: String = conf.getString(s"WebRecorder.Aerospike.${conf.getString("WebRecorder.Current.Environment")}.Nodes")
  val aerospikePort: String = conf.getString(s"WebRecorder.Aerospike.${conf.getString("WebRecorder.Current.Environment")}.Port")
  val nodeRegex: Regex = conf.getString(s"WebRecorder.Aerospike.${conf.getString("WebRecorder.Current.Environment")}.NodeRegex").r
  val nodeRegex(node1, node2, node3) = aerospikeNodes
  val nodeList = List(node1, node2, node3)
  val aerospikeClient = AerospikeClientAdapter(nodeList.map(node => s"$node:$aerospikePort").mkString(";"))
  val sessionsRepo = AerospikeSessionsRepository(aerospikeClient, 2000, 1, 10, 1)

  /**
    * Receives the session ID and gets the SessionDetails(future)
    * @param sessionId - The SID
    * @return - SessionDetails object wrapped in a future
    */


  def getSessionDetails(sessionId : Long): Future[SessionDetails] = {
    sessionsRepo.getSessionDetails(sessionId)
  }

  def getAuthDetails(sessionId : Long): Future[SessionDetails] = {
    sessionsRepo.getSessionDetails(sessionId)
  }

  def getUserTrackingState(userId: Long) = {
    val aerospikeClient = new AerospikeClient(nodeList.map(node => s"$node:$aerospikePort").mkString(";"), aerospikePort.toInt)
    aerospikeClient.get(new WritePolicy(), new Key("pipeline", "UserTracking", userId))
  }

  def getMessagesFromAerospike(sessionId: Long) = {
    val aerospikeClient = new AerospikeClient(nodeList(0), aerospikePort.toInt)
    aerospikeClient.get(new WritePolicy(), new Key("pipeline", "rawsessions", sessionId))
  }

}
