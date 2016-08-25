package com.microsoft.azure.eventhubs.lib;

import org.junit.Assert;
import org.junit.Test;

public class EnvConfigurationTest
{
	@Test
	public void EnvironmentConfigurationTest() throws Throwable
	{
		Assert.assertTrue(
				String.format("Set the environment variables - %s & %s - to run EventHubs client CITs", TestContext.EVENT_HUB_CONNECTION_STRING_ENV_NAME, TestContext.PARTIION_COUNT_ENV_NAME),
				TestContext.isTestConfigurationSet());
	}
}
