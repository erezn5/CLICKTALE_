package com.clicktale.pipeline.dataObjects

import com.clicktale.pipeline.framework.dal.GeneralFunctions

class Project (var Id:String, var Ratio:Double, var SubscriberId:String, var Name:String, var Type:String, var QueueId:Int, var Domains:String, var LegacyProjectId:String, var TTL:Int, var Rules:List[Rule]){

  def Create(Id:String, Ratio:Double, SubscriberId:String, Name:String, Type:String, QueueId:Int, Domains:String, LegacyProjectId:String, TTL:Int): Unit =
  {
    GeneralFunctions.createProject(Id, Ratio, SubscriberId, Name, Type, QueueId, Domains, LegacyProjectId, TTL)
  }

  def Update(attrName:String, attrVal:String, attrType:String): Unit ={
    GeneralFunctions.updateProject(Id,attrName,attrVal,attrType)
  }

  def Delete(id:String): Unit ={
    GeneralFunctions.deleteProject(id)
  }
}

