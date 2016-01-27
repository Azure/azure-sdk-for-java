Windows Azure EventHubs Java SDK
=============================================

Windows Azure EventHubs is a high-throughput durable event injestion pipeline. Visit our [documentation](https://azure.microsoft.com/en-us/services/event-hubs/) to Learn more about EventHubs.

[Refer developer.md](developer.md) to find out how to contribute to EventHubs Java SDK.


To open the Windows Azure EventHubs Java SDK in Eclipse - please follow these standard "Maven to Eclipse" (for other IDE's bing.com should suggest) instructions:
-----------------------------------------------------------------------------------------------------------------------------------------------------------------
1. Minimum jdk required : 1.7
2. Maven is expected to be installed and Configured - version > 3.3.9
3. After git-clone'ing to the project - open shell and navigate to the location where pom.xml is present
4. Run these commands to prepare this maven project to be opened in Eclipse:
	i.  mvn -Declipse.workspace=f:\Eclipse5 eclipse:configure-workspace
	ii. mvn eclipse:eclipse
5. Open Eclipse and use "Import Existing Maven projects" to open the project.
