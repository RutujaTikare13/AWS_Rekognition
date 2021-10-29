package com.njit.cs643;

import com.amazonaws.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TextRecognitionEngine {
    public static final String QUEUE_NAME = "rt24-cs643-queue.fifo";
    public static final String OUTPUT_FILE_NAME = "output";
    public static final String NEW_LINE = "\n";
    public S3Manager s3Manager;
    public TextDetector textDetector;
    public SqsHandler sqsHandler;
    public BufferedWriter writer;

    public TextRecognitionEngine() {
        s3Manager = new S3Manager();
        textDetector = new TextDetector();
        sqsHandler = new SqsHandler(QUEUE_NAME);

        try {
            writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_NAME +
                    System.currentTimeMillis() +
                    ".txt"));
        } catch (IOException e) {
            System.out.println("Exception creating a file writer " + e.getMessage());
        }

    }

    public static void main(String[] args) {
        TextRecognitionEngine textRecognitionEngine = new TextRecognitionEngine();
        try {
            textRecognitionEngine.doWorkText();
        } finally {
            try {
                textRecognitionEngine.writer.close();
            } catch (IOException e) {
                System.out.println("Error closing file");
            }
        }
    }

    private void doWorkText() {
        boolean isEndOfProcessing = false;
        while (true) {
            List<String> fileNames = sqsHandler.pollMessages();
            for (String fileName : fileNames) {
                if (fileName.equalsIgnoreCase( "-1")) {
                    isEndOfProcessing = true;
                    continue;
                }
                String textFromString = textDetector.extractTextFromImageInS3(s3Manager.getBucketName(), fileName);
                if (StringUtils.isNullOrEmpty(textFromString)) {
                    continue;
                }
                try {
                    writer.write(textFromString);
                    writer.write(NEW_LINE);
                } catch (IOException e) {
                    System.out.println("Failure to write " + e.getMessage());
                }
            }

            if(isEndOfProcessing) {
                break;
            }
        }
    }
}
