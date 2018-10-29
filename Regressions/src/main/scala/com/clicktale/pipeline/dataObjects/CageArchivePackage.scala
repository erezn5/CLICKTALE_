package com.clicktale.pipeline.dataObjects

import java.io.File

import com.clicktale.pipeline.framework.helpers.UrlBuilder.writeToFile
import com.google.gson.Gson

class CageArchivePackage(val events: String,
                         val streams: String,
                         val webPage: String,
                         var recording: String,
                         val avro: String,
                         val errorException: String,
                         val rawData: String) {

  def writeArchivedContentToDisk(specificArchivedSessionFolder: String): Unit = {
    //webPage=webPage.replaceAll("nv-p1-s3-assets-01","nv-q-s3-assets-01")
    new File(s"$specificArchivedSessionFolder").mkdir()
    Array("json", "xml", "dsr", "webPage")
    .zip(Array(recording, events, streams,webPage))
    .foreach {
    case (extension, content) =>
    writeToFile(s"$specificArchivedSessionFolder\\${new Gson().fromJson(recording, classOf[RecordingJson]).SID}.${extension}", content)}
    println(s"Files can be found in: $specificArchivedSessionFolder\\")
  }
}

object CageArchivePackage{
  def apply(events: String,
            streams: String,
            html: String,
            avro: String,
            analytics: String,
            errorException: String,
            rawData: String): CageArchivePackage =
    new CageArchivePackage(events, streams, html, avro, analytics, errorException, rawData)

  def apply(responseAsJson: String): CageArchivePackage =  {
    new Gson().fromJson(responseAsJson, classOf[CageArchivePackage])
  }

}