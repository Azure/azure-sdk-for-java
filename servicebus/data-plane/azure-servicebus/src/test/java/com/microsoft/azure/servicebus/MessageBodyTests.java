package com.microsoft.azure.servicebus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MessageBodyTests {

	@Test
	public void NullBinaryDataTest()
	{
		try
		{
			MessageBody.fromBinaryData(null);
			Assert.fail("MessageBody created with null binary data.");
		}
		catch(IllegalArgumentException e)
		{
			// passed
		}
	}
	
	@Test
	public void NullSequenceTest()
	{
		try
		{
			MessageBody.fromSequenceData(null);
			Assert.fail("MessageBody created with null sequence data.");
		}
		catch(IllegalArgumentException e)
		{
			// passed
		}
	}
	
	@Test
	public void NullValueDataTest()
	{
		try
		{
			MessageBody.fromValueData(null);
			Assert.fail("MessageBody created with null value data.");
		}
		catch(IllegalArgumentException e)
		{
			// passed
		}
	}
	
	@Test
	public void MultipleDataSectionsTest()
	{
		try
		{
			ArrayList<byte[]> dataList = new ArrayList<>();
			dataList.add(new byte[0]);
			dataList.add(new byte[0]);
			MessageBody.fromBinaryData(dataList);
			Assert.fail("MessageBody created with null binary data.");
		}
		catch(IllegalArgumentException e)
		{
			// passed
		}
	}
	
	@Test
	public void ZeroDataSectionsTest()
	{
		try
		{
			ArrayList<byte[]> dataList = new ArrayList<>();
			MessageBody.fromBinaryData(dataList);
			Assert.fail("MessageBody created with null binary data.");
		}
		catch(IllegalArgumentException e)
		{
			// passed
		}
	}
	
	@Test
	public void MultipleSequenceSectionsTest()
	{
		try
		{
			ArrayList<List<Object>> sequenceList = new ArrayList<>();
			ArrayList<Object> sequence1 = new ArrayList<>();
			sequence1.add("hello");
			ArrayList<Object> sequence2 = new ArrayList<>();
			sequence2.add("howdy");
			sequenceList.add(sequence1);
			sequenceList.add(sequence2);
			MessageBody.fromSequenceData(sequenceList);
			Assert.fail("MessageBody created with null binary data.");
		}
		catch(IllegalArgumentException e)
		{
			// passed
		}
	}
	
	@Test
	public void ZeroSequenceSectionsTest()
	{
		try
		{
			ArrayList<List<Object>> sequenceList = new ArrayList<>();
			MessageBody.fromSequenceData(sequenceList);
			Assert.fail("MessageBody created with null binary data.");
		}
		catch(IllegalArgumentException e)
		{
			// passed
		}
	}
	
	@Test
	public void ValueMessageBodyTest()
	{
		String value = "ValueBody";
		MessageBody body = MessageBody.fromValueData(value);
		Assert.assertEquals("Message body type didn't match.", MessageBodyType.VALUE, body.getBodyType());
		Assert.assertNull("MessageBody of value type has binary data.", body.getBinaryData());
		Assert.assertNull("MessageBody of value type has sequence data.", body.getSequenceData());
		Assert.assertEquals("Message body value didn't match", value, body.getValueData());
	}
	
	@Test
	public void SequenceMessageBodyTest()
	{
		String str1 = "hello";
		String str2 = "howdy";
		ArrayList<List<Object>> sequenceList = new ArrayList<>();
		ArrayList<Object> sequence1 = new ArrayList<>();
		sequence1.add(str1);
		sequence1.add(str2);
		sequenceList.add(sequence1);
		MessageBody body = MessageBody.fromSequenceData(sequenceList);
		Assert.assertEquals("Message body type didn't match.", MessageBodyType.SEQUENCE, body.getBodyType());
		Assert.assertNull("MessageBody of sequence type has binary data.", body.getBinaryData());
		Assert.assertNull("MessageBody of sequence type has value data.", body.getValueData());
		List<List<Object>> outputSequenceList = body.getSequenceData();
		Assert.assertEquals("Message body sequence didn't match", 1, outputSequenceList.size());
		List<Object> outputInnerSequence = outputSequenceList.get(0);
		Assert.assertEquals("Message body sequence didn't match", 2, outputInnerSequence.size());
		Assert.assertEquals("Message body sequence didn't match", str1, outputInnerSequence.get(0));
		Assert.assertEquals("Message body sequence didn't match", str2, outputInnerSequence.get(1));
	}
	
	@Test
	public void BinaryMessageBodyTest()
	{
		byte[] binaryData = new byte[1024];
		Arrays.fill(binaryData, (byte)32);
		ArrayList<byte[]> binaryDataList = new ArrayList<>();
		binaryDataList.add(binaryData);
		MessageBody body = MessageBody.fromBinaryData(binaryDataList);
		Assert.assertEquals("Message body type didn't match.", MessageBodyType.BINARY, body.getBodyType());
		Assert.assertNull("MessageBody of binary type has value data.", body.getValueData());
		Assert.assertNull("MessageBody of binary type has sequence data.", body.getSequenceData());
		List<byte[]> outputataList = body.getBinaryData();
		Assert.assertEquals("Message body binary data didn't match", 1, outputataList.size());
		byte[] outputBinaryData = outputataList.get(0);
		Assert.assertEquals("Message body sequence didn't match", binaryData, outputBinaryData);
	}
}
