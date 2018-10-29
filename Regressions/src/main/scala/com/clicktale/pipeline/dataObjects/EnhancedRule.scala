package com.clicktale.pipeline.dataObjects

case class EnhancedRule(
                    id:Int,
                    subsId:Int,
                    projectId:Long,
                    uri:String,
                    ignore:Boolean,
                    ratio:Double,
                    enabled:Boolean){
}
