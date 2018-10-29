package com.clicktale.pipeline.dataObjects

import com.clicktale.pipeline.framework.dal.GeneralFunctions

class Rule (var Id:String, var ProjectId:String,var Ratio:Double,var URI:String,var Type:Int,var Enabled:Int){

  def Update(attrName:String, attrVal:String, attrType:String): Unit ={
    GeneralFunctions.updateRule(Id,attrName,attrVal,attrType)
  }

  def Delete(id:String): Unit ={
    GeneralFunctions.deleteRule(id)
  }

}
