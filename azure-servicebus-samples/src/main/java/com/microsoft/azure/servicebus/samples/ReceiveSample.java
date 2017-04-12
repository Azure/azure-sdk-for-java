package com.microsoft.azure.servicebus.samples;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class ReceiveSample {

	private static final String ENVIRONMENT_VARIABLE_NAME = "azure-service-bus-java/connectionstring";
	private static QueueClient queueClient;

	public static void main(String[] args) throws Exception {
		System.out.println("Begining receive sample.");
		
		String envVar = System.getenv(ENVIRONMENT_VARIABLE_NAME);

		if (envVar.isEmpty()) {
			throw new Exception("Could not read environment variable: " + ENVIRONMENT_VARIABLE_NAME);
		}

		QueueClient queueClient = new QueueClient(envVar, ReceiveMode.PeekLock);
		receiveMessages();
		Thread.sleep(60 * 1000);
		queueClient.close();
		System.out.println("Receive sample completed.");
	}

	private static void receiveMessages() throws InterruptedException, ServiceBusException {		
		queueClient.registerMessageHandler(new IMessageHandler() {
			
			@Override
			public CompletableFuture<Void> onMessageAsync(IMessage message) {
				System.out.println(new String(message.getContent()));
				return CompletableFuture.completedFuture(null);
			}
			
			@Override
			public void notifyException(Throwable exception, ExceptionPhase phase) {
				System.out.println(phase + " encountered exception:" + exception.getMessage());				
			}
		}, new MessageHandlerOptions(1, true, Duration.ofMinutes(2)));
	}

}