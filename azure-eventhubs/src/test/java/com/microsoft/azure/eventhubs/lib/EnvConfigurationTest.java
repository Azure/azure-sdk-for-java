package com.microsoft.azure.eventhubs.lib;

import org.junit.Assert;
import org.junit.Test;

public class EnvConfigurationTest
{
	@Test
	public void EnvironmentConfigurationTest() throws Throwable
	{
		Assert.assertTrue(
				"Set the environment variables - EVENT_HUB_NAME, NAMESPACE_NAME, SAS_KEY & SAS_RULE_NAME - to run the CITs",
				TestBase.isTestConfigurationSet());
	}
}
