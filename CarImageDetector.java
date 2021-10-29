package com.njit.cs643;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;

import java.util.List;

public class CarImageDetector {
    private AmazonRekognition rekognitionClient;
    public CarImageDetector() {
        rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
    }

    public static void main(String[] args) {
        String photo = "10.jpg";
        String bucket = "rt24-cs643";

        CarImageDetector carImageDetector = new CarImageDetector();
        if (carImageDetector.detectCar(bucket, photo)) {
            System.out.println("CAR FOUND");
        } else {
            System.out.println("OOPS I DID IT AGAIN");
        }
    }

    public boolean detectCar(String buckeName, String objectName) {
        rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withS3Object(new S3Object().withName(objectName).withBucket(buckeName)))
                .withMaxLabels(10)
                .withMinConfidence(90F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();

            for (Label label : labels) {
//                System.out.println("Label: " + label.getName());
//                System.out.println("Confidence: " + label.getConfidence().toString() + "\n");
                if (label.getName().equalsIgnoreCase("CAR")) {
                    System.out.println("Car Detected in image: " + objectName);
                    return true;
                }
            }
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
