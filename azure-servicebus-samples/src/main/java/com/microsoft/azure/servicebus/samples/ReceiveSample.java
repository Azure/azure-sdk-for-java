package com.microsoft.azure.servicebus.samples;

import java.time.Duration;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class ReceiveSample {

	private static final String ENVIRONMENT_VARIABLE_NAME = "azure-service-bus-java/connectionstring";
	private static IMessageReceiver receiver;

	public static void main(String[] args) throws Exception {
		System.out.println("Begining receive sample.");
		
		String envVar = System.getenv(ENVIRONMENT_VARIABLE_NAME);

		if (envVar.isEmpty()) {
			throw new Exception("Could not read environment variable: " + ENVIRONMENT_VARIABLE_NAME);
		}

		ConnectionStringBuilder csb = new ConnectionStringBuilder(envVar);
		receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(csb, ReceiveMode.PeekLock);

		receiveMessages();
		System.out.println("Receive sample completed.");
	}

	private static void receiveMessages() throws InterruptedException, ServiceBusException {
		while (true) {
			IMessage receivedMessage = receiver.receive(Duration.ofMinutes(1));
			System.out.println(new String(receivedMessage.getContent()));
			receiver.complete(receivedMessage.getLockToken());
		}
	}

}