package com.njit.cs643;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class S3Manager {
    private String bucketName;
    public S3Manager() {
        AmazonS3URI amazonS3URI = new AmazonS3URI("https://njit-cs-643.s3.us-east-1.amazonaws.com");
        bucketName = amazonS3URI.getBucket();
    }
    private AmazonS3 s3Client;
    private static final Regions clientRegion = Regions.US_EAST_1;

    public List<String> retrieveObjectNamesFromS3Bucket() {
        List<String> result = new ArrayList<String>();
        try {
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new ProfileCredentialsProvider())
                    .build();

            ListObjectsV2Request req = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withMaxKeys(10);

            ListObjectsV2Result s3ListObjectResult;

            do {
                s3ListObjectResult = s3Client.listObjectsV2(req);

                for (S3ObjectSummary objectSummary : s3ListObjectResult.getObjectSummaries()) {
//                    System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
                    result.add(objectSummary.getKey());
                }
                String token = s3ListObjectResult.getNextContinuationToken();
//                System.out.println("Next Continuation Token: " + token);
                req.setContinuationToken(token);
            } while (s3ListObjectResult.isTruncated());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }

    return result;
    }

    public static void main(String[] args) throws IOException {

//        String bucketName = "rt24-cs643";
        S3Manager s3Manager = new S3Manager();
//        s3Manager.retrieveObjectNamesFromS3Bucket(bucketName);

        List<String> fileNames = s3Manager.retrieveObjectNamesFromS3Bucket();
        fileNames.forEach(System.out::println);
    }

//    private static void displayTextInputStream(InputStream input) throws IOException {
//        // Read the text input stream one line at a time and display each line.
//        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//        String line = null;
//        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
//        }
//        System.out.println();
//    }

    public String getBucketName() {
        return bucketName;
    }
}
