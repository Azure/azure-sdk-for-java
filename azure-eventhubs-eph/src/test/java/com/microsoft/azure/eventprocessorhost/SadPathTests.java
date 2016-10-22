package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.microsoft.azure.servicebus.IllegalEntityException;

public class SadPathTests extends TestBase
{
	@Test
	public void noSuchEventHubTest() throws Exception
	{
		PerTestSettings settings = new PerTestSettings("NoSuchEventHub");
		settings.inEntityDoesNotExist = true;
		settings.inoutEPHConstructorArgs.setEHPath("thereisnoeventhubwiththisname", PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE_AND_REPLACE);
		try
		{
			settings = testSetup(settings);
			fail("No exception occurred");
		}
		catch (ExecutionException e)
		{
			Throwable inner = e.getCause();
			if ((inner != null) && (inner instanceof IllegalEntityException))
			{
				TestUtilities.log("Got expected IllegalEntityException\n");
			}
			else
			{
				throw e;
			}
		}
		finally
		{
			testFinish(settings, NO_CHECKS);
		}
	}
	
	@Test
	public void noSuchConsumerGroupTest() throws Exception
	{
		PerTestSettings settings = new PerTestSettings("NoSuchConsumerGroup");
		settings.inEntityDoesNotExist = true;
		settings.inoutEPHConstructorArgs.setConsumerGroupName("thereisnoconsumergroupwiththisname");
		try
		{
			settings = testSetup(settings);
			fail("No exception occurred");
		}
		catch (ExecutionException e)
		{
			Throwable inner = e.getCause();
			if ((inner != null) && (inner instanceof EPHConfigurationException))
			{
				if ((inner.getMessage() != null) && (inner.getMessage().compareTo("Consumer group does not exist") == 0))
				{
					TestUtilities.log("Got expected exception\n");
				}
				else
				{
					throw e;
				}
			}
			else
			{
				throw e;
			}
		}
		finally
		{
			testFinish(settings, NO_CHECKS);
		}
	}
	
	@Test
	public void secondRegisterFailsTest() throws Exception
	{
		PerTestSettings settings = new PerTestSettings("SecondRegisterFails");
		settings = testSetup(settings);
		
		try
		{
			settings.outHost.registerEventProcessorFactory(settings.outProcessorFactory, settings.inOptions).get();
			fail("No exception occurred");
		}
		catch (IllegalStateException e)
		{
			TestUtilities.log("Got expected exception\n");
		}
		finally
		{
			testFinish(settings, NO_CHECKS);
		}
	}
}
