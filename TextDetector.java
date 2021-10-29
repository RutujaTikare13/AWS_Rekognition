package com.njit.cs643;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;

import java.util.List;

public class TextDetector {
    public TextDetector() {
        rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
    }

    private AmazonRekognition rekognitionClient;



    public static void main(String[] args) throws Exception {
        String photo = "1.jpg";
        String bucket = "rt24-cs643";
        TextDetector subject = new TextDetector();
        subject.extractTextFromImageInS3(bucket, photo);

    }

    public String extractTextFromImageInS3(String bucket, String s3ObjectName) {
        StringBuilder stringBuilder = new StringBuilder();
        DetectTextRequest request = new DetectTextRequest()
                .withImage(new Image()
                        .withS3Object(new S3Object()
                                .withName(s3ObjectName)
                                .withBucket(bucket)));
        try {
            DetectTextResult result = rekognitionClient.detectText(request);
            List<TextDetection> textDetections = result.getTextDetections();
            if (textDetections == null || textDetections.size() == 0) {
                return null;
            }

            //System.out.println("Detected lines and words for " + s3ObjectName);
            stringBuilder.append("Texts in file: ").append(s3ObjectName).append(" are: ");
            for (TextDetection text : textDetections) {
                if (text.getType().equalsIgnoreCase("LINE")) {
                    stringBuilder.append(text.getDetectedText());
                }
            }
        } catch(AmazonRekognitionException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
