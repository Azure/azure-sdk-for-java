package com.microsoft.azure.eventhubs.lib;

import java.util.*;

import com.microsoft.azure.eventhubs.EventHubClient;

public class TestEventHubInfo {

	private String name;
	private String namespaceName;
	private List<String> consumerGroups;
	private Map<String, String> sasRules;
	
	public TestEventHubInfo(final String name, final String namespaceName, final List<String> consumerGroups, 
			final Map<String, String> sasRules) {
		this.name = name;
		this.namespaceName = namespaceName;
		
		this.consumerGroups = consumerGroups;
		this.sasRules = sasRules;
	}

	public String getName() {
		return this.name;
	}
	
	public String getNamespaceName() {
		return this.namespaceName;
	}
	
	public String getRandomConsumerGroup() {
		if (this.consumerGroups == null || this.consumerGroups.size() == 0)
		{
			return EventHubClient.DefaultConsumerGroupName;
		}
		
		int randomIndex = new Random().nextInt(this.consumerGroups.size());
		return this.consumerGroups.get(randomIndex);
	}
	
	/**
	 * @return Key-Value pair of SasKeyName and SasKey 
	 */
	public Map.Entry<String, String> getSasRule()
	{
		return new AbstractMap.SimpleEntry<String, String>(TestBase.SasRuleName, this.sasRules.get(TestBase.SasRuleName));
	}
}
