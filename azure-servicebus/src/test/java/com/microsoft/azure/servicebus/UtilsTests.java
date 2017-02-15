package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;

import com.microsoft.azure.servicebus.primitives.Util;

import org.junit.Assert;

public class UtilsTests {
	@Test
	public void testGuidConversionFromDotNetToJava()
	{
		String guidString = "b5dc4a70-ac5d-43b3-b132-ec8fcdac3a9d";
		// Java bytes are signed where as dotNet bytes are unsigned. No problem type casting larger than 127 unsigned bytes to java signed bytes
		// as we are interested only in the individual bits for UUID conversion.
		byte[] dotNetGuidBytes = {112, 74, (byte)220, (byte)181, 93, (byte)172, (byte)179, 67, (byte)177, 50, (byte)236, (byte)143, (byte)205, (byte)172, 58, (byte)157};		
		UUID convertedGuid = Util.convertDotNetBytesToUUID(dotNetGuidBytes);
		Assert.assertEquals("UUID conversion from DotNet to Java failed", guidString, convertedGuid.toString());
	}
	
	@Test
	public void testGuidConversionFromJavaToDotNet()
	{
		String guidString = "b5dc4a70-ac5d-43b3-b132-ec8fcdac3a9d";
		UUID javaGuid = UUID.fromString(guidString);		
		byte[] dotNetGuidBytes = {112, 74, (byte)220, (byte)181, 93, (byte)172, (byte)179, 67, (byte)177, 50, (byte)236, (byte)143, (byte)205, (byte)172, 58, (byte)157};		
		byte[] convertedBytes = Util.convertUUIDToDotNetBytes(javaGuid);		
		Assert.assertArrayEquals("UUID conversion from Java to DotNet failed", dotNetGuidBytes, convertedBytes);
	}
	
	@Test
	public void testDateTimeConversionFromDotNetToJava()
	{
		String dotNetDateTimeString = "2016-11-30T20:57:01.4638052Z";
		long dotNetTicks = 636161362214638052l;
		Instant convertedInstant = Util.convertDotNetTicksToInstant(dotNetTicks);
		Instant expectedInstant = Instant.parse(dotNetDateTimeString);
		Assert.assertEquals("DateTime conversion from DotNet to Java failed", expectedInstant, convertedInstant);
	}
	
	@Test
	public void testDateTimeConversionFromJavaToDotNet()
	{
		String dotNetDateTimeString = "2016-11-30T20:57:01.4638052Z";
		long dotNetTicks = 636161362214638052l;		
		Instant javaInstant = Instant.parse(dotNetDateTimeString);
		Assert.assertEquals("DateTime conversion from Java to DotNet failed", dotNetTicks, Util.convertInstantToDotNetTicks(javaInstant));
	}
}
