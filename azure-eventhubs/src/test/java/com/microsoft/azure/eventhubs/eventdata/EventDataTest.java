package com.microsoft.azure.eventhubs.eventdata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.junit.Assert;

import com.microsoft.azure.eventhubs.EventData;

public class EventDataTest
{
	final String payload = "testmessage1"; // even number of chars
	
	@Test (expected = IllegalArgumentException.class)
	public void eventDataByteArrayNotNull()
	{
		byte[] byteArray = null;
		new EventData(byteArray);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void eventDataByteArrayNotNullBuffer()
	{
		final ByteBuffer buffer = null;
	 	new EventData(buffer);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void eventDataByteArrayNotNullConstructor2()
	{
		new EventData(null, 0, 0);
	}
	
	@Test
	public void eventDataSerializationTest() throws IOException, ClassNotFoundException
	{	 	
	 	
	 	final EventData withSimpleByteArray = new EventData(payload.getBytes());
		EventData deSerializedEvent = serializeAndDeserialize(withSimpleByteArray);
	 	Assert.assertTrue(payload.equals(new String(deSerializedEvent.getBody())));
	}
	
	@Test
	public void eventDataSerializationTestConstWithOffsetAndLength() throws IOException, ClassNotFoundException
	{
		final ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
	 	payloadStream.write(payload.getBytes());
	 	payloadStream.write(payload.getBytes());
	 	payloadStream.close();
	 	
	 	final EventData withByteArrayAndOffset = new EventData(payloadStream.toByteArray(), payloadStream.size()/2, payloadStream.size()/2);
	 	final EventData deSerializedEvent = serializeAndDeserialize(withByteArrayAndOffset);
	 	Assert.assertTrue(payload.equals(new String(deSerializedEvent.getBody())));
	}
	
	@Test
	public void eventDataSerializationTestConstWithByteBuffer() throws IOException, ClassNotFoundException
	{
		final ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
	 	payloadStream.write(payload.getBytes());
	 	payloadStream.write(payload.getBytes());
	 	payloadStream.close();
	 	
	 	final EventData withByteBuffer = new EventData(ByteBuffer.wrap(payloadStream.toByteArray(),payloadStream.size()/2, payloadStream.size()/2));
	 	final EventData deSerializedEvent = serializeAndDeserialize(withByteBuffer);
	 	Assert.assertTrue(payload.equals(new String(deSerializedEvent.getBody())));
	}
	
	@Test
	public void sendingEventsSysPropsShouldBeNull()
	{
		Assert.assertTrue(new EventData("Test".getBytes()).getSystemProperties() == null);
	}

	private EventData serializeAndDeserialize(final EventData input) throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(input);
		oos.close();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
	 	final EventData deSerializedEvent = (EventData) ois.readObject();
	 	ois.close();
	 	
	 	return deSerializedEvent;
	}
}
