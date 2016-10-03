package com.microsoft.azure.eventhubs.lib;

import org.junit.Assume;
import org.junit.BeforeClass;

public class ApiTestBase extends TestBase
{
	@BeforeClass
	public static void skipIfNotConfigured()
	{
		Assume.assumeTrue(TestContext.isTestConfigurationSet());
	}
}
