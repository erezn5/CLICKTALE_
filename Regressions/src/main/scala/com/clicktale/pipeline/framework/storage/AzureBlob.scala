package com.clicktale.pipeline.framework.storage

import java.io.File
import java.text.SimpleDateFormat
import java.util
import java.util.Calendar

import System.IO.File
import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.google.gson.Gson
import com.microsoft.azure.storage._
import com.microsoft.azure.storage.blob._
import com.microsoft.azure.storage.file.FileInputStream

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.{Failure, Success, Try}

object AzureBlob {
  var storageConnectionString = s"DefaultEndpointsProtocol=${conf.getString("WebRecorder.Azure.Blob.DefaultEndpointsProtocol")};AccountName=${conf.getString("WebRecorder.Azure.Blob.AccountName")};AccountKey=${conf.getString("WebRecorder.Azure.Blob.AccountKey")}"
  var account = CloudStorageAccount.parse(storageConnectionString)
  var serviceClient = account.createCloudBlobClient()

  def CreateContainer(name: String): Unit = {
    var container = serviceClient.getContainerReference(name)
    container.createIfNotExists()
  }

  def Download(containerName: String, destFileName: String,fileBlobPath: String): Unit = {
    var container = serviceClient.getContainerReference(containerName)
    var blob = container.getBlockBlobReference(fileBlobPath)
    var destinationFile = new File(destFileName)
    blob.downloadToFile(destinationFile.getAbsolutePath())
  }

  def DeleteContainer(name: String): Unit = {
    var container = serviceClient.getContainerReference(name)
    container.deleteIfExists()
  }

  def ListBlobs(containerName: String): util.Iterator[ListBlobItem] = {
    var l = List[String]()
    var container = serviceClient.getContainerReference(containerName)
    container.listBlobs().iterator()
  }

  def DeleteBlob(containerName: String, blobName: String): Unit =
  {
    var container = serviceClient.getContainerReference(containerName)
    var blobsIterator = container.getDirectoryReference(blobName).listBlobs().iterator()
    while(blobsIterator.hasNext()){
      val str = blobsIterator.next().getUri().getPath()
      DeleteBlob(containerName,str.replace(s"/$containerName/",""))
      val blob = container.getBlockBlobReference(str.replace(s"/$containerName/",""))
      blob.deleteIfExists()
    }
  }
}