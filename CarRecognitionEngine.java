package com.njit.cs643;

import java.util.List;

public class CarRecognitionEngine {
    public static final String QUEUE_NAME = "rt24-cs643-queue.fifo";
    public static final String GROUP_ID = "rt24-cs643-group";
    public S3Manager s3Manager;
    public CarImageDetector carImageDetector;
    public SqsHandler sqsHandler;


    public CarRecognitionEngine() {
        s3Manager = new S3Manager();
        carImageDetector = new CarImageDetector();
        sqsHandler = new SqsHandler(QUEUE_NAME);

    }
    public static void main(String[] args) {
        CarRecognitionEngine carRecognitionEngine = new CarRecognitionEngine();
        carRecognitionEngine.doWork();
    }

    private void doWork() {
        List<String> s3ObjectNames = s3Manager.retrieveObjectNamesFromS3Bucket();
        for(String s3ObjectName : s3ObjectNames) {
            boolean detected = carImageDetector.detectCar(s3Manager.getBucketName(), s3ObjectName);
            if (detected) {

                sqsHandler.pushMessage(s3ObjectName, GROUP_ID);
            }
        }
        sqsHandler.pushMessage("-1", GROUP_ID);
    }
}
