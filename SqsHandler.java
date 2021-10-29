package com.njit.cs643;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqsHandler {
    private final AmazonSQS sqs;
    String queueUrl;

    public SqsHandler(String queueName) {
        sqs = AmazonSQSClientBuilder.defaultClient();
        try {
            Map<String, String> attributes = new HashMap<>();
            attributes.put(QueueAttributeName.FifoQueue.toString(), Boolean.TRUE.toString());
            CreateQueueRequest request = new CreateQueueRequest()
                    .withQueueName(queueName)
                    .withAttributes(attributes); // Set FIFO_QUEUE attribute to true

            sqs.createQueue(request);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw e;
            }
        }
        queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
    }

    public void pushMessage(String message, String groupId) {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageGroupId(groupId)
                .withMessageDeduplicationId(groupId + ":" +
                        message.replace(".", ":") +
                        System.currentTimeMillis())
                .withMessageBody(message);
        sqs.sendMessage(send_msg_request);
    }

    public List<String> pollMessages() {
        ReceiveMessageRequest receiveMessageRequest =
                new ReceiveMessageRequest()
                        .withQueueUrl(queueUrl);

        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        List<String> fileNames = new ArrayList<String>();
        for (Message message : messages) {
            fileNames.add(message.getBody());
        }
        try {
            for (Message message : messages) {
                DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withReceiptHandle(message.getReceiptHandle());
                sqs.deleteMessage(deleteMessageRequest);
            }
        } catch (AmazonSQSException e) {
            throw e;
        }
        return fileNames;
    }

    public static void main(String[] args) {
        boolean isTerminate = false;
        SqsHandler sqsHandler = new SqsHandler("rt24-cs643-queue.fifo");
        while(true) {
            List<String> filenames = sqsHandler.pollMessages();
            for (String fileName : filenames) {
                if(fileName.equalsIgnoreCase("-1")) {
                    isTerminate = true;
                    continue;
                }
                System.out.println(fileName);
            }
            if(isTerminate) {
                break;
            }
        }
    }
}
