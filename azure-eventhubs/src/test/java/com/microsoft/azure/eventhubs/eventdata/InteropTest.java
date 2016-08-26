package com.microsoft.azure.eventhubs.eventdata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.MessageReceiver;
import com.microsoft.azure.servicebus.MessageSender;
import com.microsoft.azure.servicebus.MessagingFactory;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.microsoft.azure.servicebus.amqp.AmqpConstants;

public class InteropTest extends ApiTestBase
{
	static EventHubClient ehClient;
	static MessagingFactory msgFactory;
	static PartitionReceiver receiver;
	static MessageReceiver msgReceiver;
	static MessageSender partitionMsgSender;
	static PartitionSender partitionEventSender;

	static final String partitionId = "0";
	
	static final Message originalMessage = Proton.message();
	static final String applicationProperty = "firstProp";
	static final String msgAnnotation = "message-annotation-1";
	static final String payload = "testmsg";
	
	static EventData receivedEvent;
	static EventData reSentAndReceivedEvent;
	static Message reSendAndReceivedMessage;
	
	final Consumer<EventData> validateAmqpPropertiesInEventData = new Consumer<EventData>()
	{
		@Override
		public void accept(EventData eData)
		{
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_MESSAGE_ID)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_MESSAGE_ID).equals(originalMessage.getMessageId()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_USER_ID)
					&& new String((byte[]) eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_USER_ID)).equals(new String(originalMessage.getUserId())));		
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_TO)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_TO).equals(originalMessage.getAddress()));		
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CONTENT_TYPE).equals(originalMessage.getContentType()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CONTENT_ENCODING).equals(originalMessage.getContentEncoding()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CORRELATION_ID)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CORRELATION_ID).equals(originalMessage.getCorrelationId()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_CREATION_TIME)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_CREATION_TIME).equals(originalMessage.getCreationTime()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_SUBJECT)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_SUBJECT).equals(originalMessage.getSubject()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_GROUP_ID)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_GROUP_ID).equals(originalMessage.getGroupId()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_REPLY_TO_GROUP_ID).equals(originalMessage.getReplyToGroupId()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_REPLY_TO)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_REPLY_TO).equals(originalMessage.getReplyTo()));
			Assert.assertTrue(eData.getSystemProperties().containsKey(AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_time)
					&& eData.getSystemProperties().get(AmqpConstants.AMQP_PROPERTY_ABSOLUTE_EXPRITY_time).equals(originalMessage.getExpiryTime()));
			
			Assert.assertTrue(eData.getSystemProperties().containsKey(msgAnnotation)
					&& eData.getSystemProperties().get(msgAnnotation).equals(originalMessage.getMessageAnnotations().getValue().get(Symbol.getSymbol(msgAnnotation))));
			
			Assert.assertTrue(eData.getProperties().containsKey(applicationProperty)
					&& eData.getProperties().get(applicationProperty).equals(originalMessage.getApplicationProperties().getValue().get(applicationProperty)));
			
			Assert.assertTrue(eData.getProperties().size() == 1);
			
			Assert.assertTrue(new String(eData.getBody(), eData.getBodyOffset(), eData.getBodyLength()).equals(payload));	
		}};
		
	@BeforeClass
	public static void initialize() throws ServiceBusException, IOException, InterruptedException, ExecutionException
	{
		final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
		final String connectionString = connStrBuilder.toString();

		ehClient = EventHubClient.createFromConnectionStringSync(connectionString);
		msgFactory = MessagingFactory.createFromConnectionString(connectionString).get();
		receiver = ehClient.createReceiverSync(TestContext.getConsumerGroupName(), partitionId, Instant.now());
		partitionMsgSender = MessageSender.create(msgFactory, "link1", connStrBuilder.getEntityPath() + "/partitions/" + partitionId).get();
		partitionEventSender = ehClient.createPartitionSenderSync(partitionId);
		
		final HashMap<String, String> appProperties = new HashMap<String, String>();
		appProperties.put(applicationProperty, "value1");
		final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
		originalMessage.setApplicationProperties(applicationProperties);
		
		originalMessage.setMessageId("id1");
		originalMessage.setUserId("user1".getBytes());
		originalMessage.setAddress("eventhub1");
		originalMessage.setSubject("sub");
		originalMessage.setReplyTo("replyingTo");
		originalMessage.setExpiryTime(456L);
		originalMessage.setGroupSequence(5555L);
		originalMessage.setContentType("events");
		originalMessage.setContentEncoding("UTF-8");
		originalMessage.setCorrelationId("corid1");
		originalMessage.setCreationTime(345L);
		originalMessage.setGroupId("gid");
		originalMessage.setReplyToGroupId("replyToGroupId");
		
		originalMessage.setMessageAnnotations(new MessageAnnotations(new HashMap<Symbol, Object>()));
		originalMessage.getMessageAnnotations().getValue().put(Symbol.getSymbol(msgAnnotation), "messageAnnotationValue");
		
		originalMessage.setBody(new Data(Binary.create(ByteBuffer.wrap(payload.getBytes()))));
		
		partitionMsgSender.send(originalMessage).get();
		receivedEvent = receiver.receiveSync(10).iterator().next();
		
		partitionEventSender.sendSync(receivedEvent);
		reSentAndReceivedEvent = receiver.receiveSync(10).iterator().next();
		
		partitionEventSender.sendSync(reSentAndReceivedEvent);
		msgReceiver = MessageReceiver.create(
				msgFactory, 
				"receiver1", 
				connStrBuilder.getEntityPath() + "/ConsumerGroups/" + TestContext.getConsumerGroupName() + "/Partitions/" + partitionId,
				reSentAndReceivedEvent.getSystemProperties().getOffset(), false, null, 100, 0L, false).get();
		reSendAndReceivedMessage = msgReceiver.receive(10).get().iterator().next();
	}

	@Test
	public void interopWithDirectProtonAmqpMessage()
	{
		validateAmqpPropertiesInEventData.accept(receivedEvent);
	}
	
	@Test
	public void interopWithDirectProtonEventDataReSend()
	{		
		validateAmqpPropertiesInEventData.accept(reSentAndReceivedEvent);
	}
	
	@Test
	public void resentAmqpMessageShouldRetainAllOriginalProps()
	{
		Assert.assertTrue(reSendAndReceivedMessage.getMessageId().equals(originalMessage.getMessageId()));
		Assert.assertTrue(reSendAndReceivedMessage.getAddress().equals(originalMessage.getAddress()));
		Assert.assertTrue(reSendAndReceivedMessage.getContentEncoding().equals(originalMessage.getContentEncoding()));
		Assert.assertTrue(reSendAndReceivedMessage.getContentType().equals(originalMessage.getContentType()));
		Assert.assertTrue(new String(reSendAndReceivedMessage.getUserId()).equals(new String(originalMessage.getUserId())));
		Assert.assertTrue(reSendAndReceivedMessage.getCorrelationId().equals(originalMessage.getCorrelationId()));
		Assert.assertTrue(reSendAndReceivedMessage.getGroupId().equals(originalMessage.getGroupId()));
		Assert.assertTrue(reSendAndReceivedMessage.getReplyTo().equals(originalMessage.getReplyTo()));
		Assert.assertTrue(reSendAndReceivedMessage.getReplyToGroupId().equals(originalMessage.getReplyToGroupId()));
		Assert.assertTrue(reSendAndReceivedMessage.getSubject().equals(originalMessage.getSubject()));
		Assert.assertTrue(reSendAndReceivedMessage.getExpiryTime() == originalMessage.getExpiryTime());
		Assert.assertTrue(reSendAndReceivedMessage.getCreationTime() == originalMessage.getCreationTime());
		Assert.assertTrue(reSendAndReceivedMessage.getExpiryTime() == originalMessage.getExpiryTime());
		Assert.assertTrue(reSendAndReceivedMessage.getGroupSequence() == originalMessage.getGroupSequence());
		
		Assert.assertTrue(reSendAndReceivedMessage.getApplicationProperties().getValue().get(applicationProperty)
				.equals(originalMessage.getApplicationProperties().getValue().get(applicationProperty)));
		
		Assert.assertTrue(reSendAndReceivedMessage.getMessageAnnotations().getValue().get(Symbol.getSymbol(msgAnnotation))
				.equals(originalMessage.getMessageAnnotations().getValue().get(Symbol.getSymbol(msgAnnotation))));
		
		Binary payloadBytes = ((Data) reSendAndReceivedMessage.getBody()).getValue();
		Assert.assertTrue(new String(payloadBytes.getArray(), payloadBytes.getArrayOffset(), payloadBytes.getLength()).equals(payload));
	}
	
	@AfterClass
	public static void cleanup() throws ServiceBusException
	{
		if (msgReceiver != null)
			msgReceiver.closeSync();

		if (partitionEventSender != null)
			partitionEventSender.closeSync();

		if (partitionMsgSender != null)
			partitionMsgSender.closeSync();
		
		if (receiver != null)
			receiver.closeSync();
		
		if (ehClient != null)
			ehClient.closeSync();
		
		if (msgFactory != null)
			msgFactory.closeSync();
	}
}
