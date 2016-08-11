package com.microsoft.azure.eventhubs.eventdata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.junit.Test;
import junit.framework.Assert;

import com.microsoft.azure.eventhubs.EventData;

public class EventDataTest
{

	@Test
	public void eventDataSerializationTest() throws IOException, ClassNotFoundException
	{
		// serialization assumes that the underlying byte-array in EventData is never null - so validating the same
	 	boolean thrown = false;
	 	try {
	 		final ByteBuffer buffer = null;
		 	new EventData(buffer);
	 	} catch (IllegalArgumentException ignore) {
	 		thrown = true;
	 	}
	 	Assert.assertTrue(thrown);
	 	
	 	final byte[] byteArray = null;
 		thrown = false;
	 	try {
	 		new EventData(byteArray);
	 	} catch (IllegalArgumentException ignore) {
	 		thrown = true;
	 	}
	 	Assert.assertTrue(thrown);
	 	
	 	thrown = false;
	 	try {
	 		new EventData(byteArray, 0, 0);
	 	} catch (IllegalArgumentException ignore) {
	 		thrown = true;
	 	}
	 	Assert.assertTrue(thrown);
	 	
	 	// validate byte-array serialization for all constructors of EventData
	 	final String payload = "testmessage1"; // even number of chars
		
	 	final EventData withSimpleByteArray = new EventData(payload.getBytes());
		EventData deSerializedEvent = serializeAndDeserialize(withSimpleByteArray);
	 	Assert.assertTrue(payload.equals(new String(deSerializedEvent.getBody())));
	 	
	 	final ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
	 	payloadStream.write(payload.getBytes());
	 	payloadStream.write(payload.getBytes());
	 	payloadStream.close();
	 	final EventData withByteArrayAndOffset = new EventData(payloadStream.toByteArray(), payloadStream.size()/2, payloadStream.size()/2);
	 	deSerializedEvent = serializeAndDeserialize(withByteArrayAndOffset);
	 	Assert.assertTrue(payload.equals(new String(deSerializedEvent.getBody())));
	 	
	 	final EventData withByteBuffer = new EventData(ByteBuffer.wrap(payloadStream.toByteArray(),payloadStream.size()/2, payloadStream.size()/2));
	 	deSerializedEvent = serializeAndDeserialize(withByteBuffer);
	 	Assert.assertTrue(payload.equals(new String(deSerializedEvent.getBody())));
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
