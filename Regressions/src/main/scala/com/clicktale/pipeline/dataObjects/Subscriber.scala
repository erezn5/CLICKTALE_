package com.clicktale.pipeline.dataObjects

import com.clicktale.pipeline.framework.dal.GeneralFunctions

class Subscriber (var Id:String,var Name:String,var TotalQuota:Int,var Projects:List[Project]){

  def Update(attrName:String, attrVal:String, attrType:String): Unit ={
    GeneralFunctions.updateSubscriber(Id,attrName,attrVal,attrType)
  }

  def Delete(id:String): Unit ={
    GeneralFunctions.deleteSubscriber(id)
  }

}
