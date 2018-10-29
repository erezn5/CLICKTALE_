package com.clicktale.pipeline.framework.dal

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.ec2.{AmazonEC2, AmazonEC2ClientBuilder}
import com.amazonaws.services.ec2.model._
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

object AwsManager {

  implicit val conf = ConfigFactory.load
  val awsCredentials = new BasicAWSCredentials(conf.getString(s"WebRecorder.Aws.${conf.getString("WebRecorder.Current.Environment")}.AccessKeyId"), conf.getString(s"WebRecorder.Aws.${conf.getString("WebRecorder.Current.Environment")}.SecretAccessKey"))
  implicit val awsEc2Client = AmazonEC2ClientBuilder.standard.withRegion(conf.getString(s"WebRecorder.Aws.${conf.getString("WebRecorder.Current.Environment")}.Region"))
    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build
  def getRunningInstanceByTag(tagKey: String, tagValue: String) = getRunningInstanceByTagInner(tagKey, tagValue)
  import scala.collection.JavaConversions._
  /**
    * Gets the running instances which matches the key value given tag.
    *
    * @param tagKey       The tag's key.
    * @param tagValue     - The matching tag's value.
    * @param awsEc2Client Implicit value, the client.
    * @return The first running instance which was found to comply.
    */
  def getRunningInstanceByTagInner(tagKey: String, tagValue: String)(implicit awsEc2Client: AmazonEC2): Instance = {
    val result = awsEc2Client.describeInstances(new DescribeInstancesRequest()
      .withFilters(new Filter("tag:" + tagKey, List(tagValue).asJava)))
    result.getReservations.toList
      .flatMap(_.getInstances)
      .filter(instance =>
        instance.getState.getName == conf.getString("WebRecorder.Aws.States.Running"))
      .head
  }


  /*
  /**
    * Deletes past gitlab snapshots(back one day and above)
    *
    * @param awsEc2Client Implicit value, the client.
    */
  def deletePastSnapshots(implicit awsEc2Client: AmazonEC2, conf: Config): Unit = {
    val currentDate = new SimpleDateFormat(conf.getString("WebRecorder.Aws.${conf.getString("WebRecorder.Current.Environment")}.DateFormat")).format(Calendar.getInstance().getTime)
    val pipelineVolumeId = conf.getString("Aws.VolumeId")   //no such value
    val snapshotResults = awsEc2Client.describeSnapshots()
    val snapShotsToDelete = snapshotResults.getSnapshots
      .filter(snapshot =>
        snapshot.getStartTime != currentDate && snapshot.getVolumeId == pipelineVolumeId)
      .map(_.getSnapshotId)
    snapShotsToDelete.foreach(snapshot =>
      awsEc2Client.deleteSnapshot(new DeleteSnapshotRequest(snapshot)))
  }
  */
}
