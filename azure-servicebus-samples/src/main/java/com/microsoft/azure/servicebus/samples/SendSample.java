package com.microsoft.azure.servicebus.samples;

import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class SendSample {
	
	private static final String ENVIRONMENT_VARIABLE_NAME = "azure-service-bus-java/connectionstring";
	private static final int NUMBER_OF_MESSAGES = 1;
	private static QueueClient queueClient;
	
	public static void main(String[] args) throws Exception {
		System.out.println("Begining send sample.");
		
		String envVar = System.getenv(ENVIRONMENT_VARIABLE_NAME);
		
		if (envVar.isEmpty()) {
			throw new Exception("Could not read environment variable: " + ENVIRONMENT_VARIABLE_NAME);
		}		
		
		QueueClient queueClient = new QueueClient(envVar, ReceiveMode.PeekLock);
		
		sendMessages(NUMBER_OF_MESSAGES);
		queueClient.close();
		System.out.println("Send sample completed.");
	}
	
	private static void sendMessages(int numMessages) throws InterruptedException, ServiceBusException {
		for(int i = 0; i < numMessages; i ++) {
			String messageBody = "MessageNumber: " + i;
			Message message = new Message(messageBody.getBytes());
			queueClient.send(message);
			System.out.println("Sending message " + i);
		}
	}
}
