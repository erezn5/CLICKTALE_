package helpers;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringUtils;
import com.pipe.pipeapp.Configuration;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class Amazon {
    private FileHandling fh;
    private String AWS_ACCESS_KEY = Configuration.prop.getProperty("suite.pusher.defaultParams.accessKey");
    private String AWS_SECRET_KEY = Configuration.prop.getProperty("suite.pusher.defaultParams.secretKey");
    //String bucketName = "nv-q-s3-assets-01";
    String m_bucketName;
    private S3ObjectSummary summary;
    private ObjectListing object_listing;
    boolean flag = true;
    AmazonS3 s3client;
    String amazonUrl = Configuration.prop.getProperty("suite.pusher.defaultParams.amazonUrl");
    String[] instancesArray = new String[3];

    public Amazon() {

        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY));
        s3client = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion(Regions.US_EAST_1).build();

    }

    //Constructor for a case that we want to pass bucketName outside this class
    public Amazon(String i_bucketName) {
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY));
        s3client = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion(Regions.US_EAST_1).build();
        m_bucketName = i_bucketName;
    }


    public void removeFromBucket() {

        object_listing = s3client.listObjects(m_bucketName);

        while (flag) {
            for (Iterator<?> iterator =
                 object_listing.getObjectSummaries().iterator();
                 iterator.hasNext(); ) {
                summary = (S3ObjectSummary) iterator.next();
                s3client.deleteObject(m_bucketName, summary.getKey());

            }

            if (object_listing.isTruncated()) {
                object_listing = s3client.listNextBatchOfObjects(object_listing);
            } else {
                break;
            }
        }


    }

    public String downloadAnObjectAndReturnFolderPath() throws IOException {
        String filePath = Configuration.prop.getProperty("suite.pusher.defaultParams.processorLinuxPath");
        String folderName="";

        try {
            ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(m_bucketName).withMaxKeys(2);
            ListObjectsV2Result result;
            int count = 0;
            do {
                result = s3client.listObjectsV2(req);
//                if(result.isTruncated() == true){
//                    System.out.println();
//                }
                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + " " + "(size = " + objectSummary.getSize());
                    String key = objectSummary.getKey();
                    System.out.println("Downloading an object (proclin products): ");
                    S3Object s3object = s3client.getObject(new GetObjectRequest(
                            m_bucketName, key));
                    System.out.println("Content-Type: " +
                            s3object.getObjectMetadata().getContentType());
                   folderName = filePath + "\\" + (s3object.getKey()).substring(0, (s3object.getKey().lastIndexOf(".")));
                   fh = new FileHandling(folderName);
                   fh.createFolder();


                    InputStream reader = new BufferedInputStream(s3object.getObjectContent());

                    OutputStream writer = new BufferedOutputStream(new FileOutputStream(filePath+"\\"+s3object.getKey()));

                    int read = -1;

                    while ((read = reader.read()) != -1) {
                        writer.write(read);
                    }

                    writer.flush();
                    writer.close();
                    reader.close();

                    fh.unzipFile(filePath+"\\"+s3object.getKey(), folderName);

                }

            } while (result.isTruncated() == true);

        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means" +
                    " the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());

        }

        return folderName;

    }


    public int countObjects() {
        int count=0;
        try {
            System.out.println("Listing objects: ");
            ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(m_bucketName).withMaxKeys(2);
            ListObjectsV2Result result;
            count=0;
            do {
                result = s3client.listObjectsV2(req);

                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + " " + "(size = " + objectSummary.getSize());
                    count++;
                }

                //System.out.println("Next continuation Token : " + result.getNextContinuationToken());
                req.setContinuationToken(result.getContinuationToken());
            } while (result.isTruncated() == true);

            System.out.println("count is: " + count);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return count;
    }


    public int countNumberOfObjectsInBucket(){

        object_listing = s3client.listObjects(this.m_bucketName);

        flag=true;

        int count = 0;

        while(flag){
            for(Iterator<?> iterator = object_listing.getObjectSummaries().iterator();
                    iterator.hasNext();){
                summary = (S3ObjectSummary) iterator.next();
                System.out.print(".");
                count++;
            }

            flag = false;
        }


        System.out.println("\nNumber of objects inside bucket are: " + count);
        return count;
    }

    public void deleteAllObjetsFromBucketS3() {

        System.out.println("Deleting S3 bucket: " + m_bucketName);
        try {
            System.out.println(" - removing objects from bucket");
            ObjectListing object_listing = s3client.listObjects(m_bucketName);
            System.out.print("Deleting");
            while (true) {
                for (Iterator<?> iterator =
                     object_listing.getObjectSummaries().iterator();
                     iterator.hasNext(); ) {
                    S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
                    s3client.deleteObject(m_bucketName, summary.getKey());
                    System.out.print(".");
                }

                // more object_listing to retrieve?
                if (object_listing.isTruncated()) {
                    object_listing = s3client.listNextBatchOfObjects(object_listing);
                } else {
                    break;
                }
            }
            ;

        }catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("\nDone!");

    }


    public void initInstancesArray(){
        for(int i=0; i<instancesArray.length;i++){
            instancesArray[i] = Configuration.prop.getProperty("suite.pusher.defaultParams.instanceAero0"+(i+1));

        }
    }

    public String getObjectCreation(String sid, String subId, String pid) throws NoSuchAlgorithmException {
        fh = new FileHandling();
        String md5 = fh.calcMD5AndReturnMD5Date(sid);
        String objectZipFileName = md5.concat("/"+subId+"_"+pid+"_"+sid+".zip");
        ObjectListing objectListing = s3client.listObjects(m_bucketName);
        String date = "";
        String key = "";
        do{
            for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()){
                date = StringUtils.fromDate(objectSummary.getLastModified());
                key = objectSummary.getKey();
                if(key.equals(objectZipFileName)) {
                    System.out.println(objectSummary.getKey() + "\t" + objectSummary.getSize() + "\t" + date);
                    return date;
                }
            }

            objectListing = s3client.listNextBatchOfObjects(objectListing);
        }while(objectListing.isTruncated());

        return date;
    }

    public String checkInstanceStatus(String instance_id){
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instance_id);
        DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstanceRequest);
        InstanceState state = describeInstancesResult.getReservations().get(0).getInstances().get(0).getState();

        return state.getName();
    }



    public String checkPipeInstanceStatus() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.pipeInstance");
        String res = checkInstanceStatus(instance_id);

        return res;

    }

    public void startAeroSpikes(){
       initInstancesArray();

        for(int i=0;i<instancesArray.length;i++){
            startInstance(instancesArray[i]);
        }


    }

    public void stopAeroSpikes(){
        initInstancesArray();

        for(int i=0;i<instancesArray.length;i++){
            stopInstance(instancesArray[i]);
        }

    }

    private void startInstance(String instance_id){
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
                () -> {
                    StartInstancesRequest request = new StartInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()) {
            System.out.printf(
                    "Failed dry run to start instance %s\n", instance_id);

            throw dry_response.getDryRunResponse();
        }

        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s\n", instance_id);

    }


    private void stopInstance(String instance_id){

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request = () -> {
            StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
            return request.getDryRunRequest();
        };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()){
            System.out.printf("Failed dry run to stop instance %s\n",instance_id );
            throw dry_response.getDryRunResponse();
        }

        StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
        ec2.stopInstances(request);

        System.out.printf("Successfully stop instace %s\n", instance_id);
     }

    public void stopPipe() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.pipeInstance");
        stopInstance(instance_id);
    }

    public void startPipe() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.pipeInstance");
        startInstance(instance_id);
    }


    public void checkAeroSpikesStatus() {
        initInstancesArray();
        for(int i=0;i<instancesArray.length;i++){
            checkInstanceStatus(instancesArray[i]);
        }
    }

    public void stopKafka() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.kafkaInstance");
        stopInstance(instance_id);
    }

    public String checkKafkaStatus() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.kafkaInstance");
        String res = checkInstanceStatus(instance_id);

        return res;
    }
    public String checkRabbitStatus() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitInstance");
        String res = checkInstanceStatus(instance_id);

        return res;
    }
    public void startKafka() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.kafkaInstance");
        startInstance(instance_id);
    }

    public void stopRabbit() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitInstance");
        stopInstance(instance_id);
    }

    public void startRabbit() {
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitInstance");
        startInstance(instance_id);
    }

    public void startProcessorDotNet(){
        String instance_id = Configuration.prop.getProperty("suite.pusher.defaultParams.processorDotNetInstance");
        startInstance(instance_id);
    }
}



