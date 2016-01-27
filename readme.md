#Microsoft Azure Event Hubs Client for Java

Azure Event Hubs is a highly scalable publish-subscribe service that can ingest millions of events per second and stream them into multiple applications. 
This lets you process and analyze the massive amounts of data produced by your connected devices and applications. Once Event Hubs has collected the data, 
transform and store it by using any real-time analytics provider or with batching/storage adapters. 

Refer to the [documentation](https://azure.microsoft.com/services/event-hubs/) to learn more about Event Hubs in general.



##Instructions to open the Windows Azure EventHubs Java SDK in Eclipse:

1. Minimum jdk version required : 1.7
2. Maven is expected to be installed and Configured - version > 3.3.9
3. After git-clone'ing to the project - open shell and navigate to the location where pom.xml is present
4. Run these commands to prepare this maven project to be opened in Eclipse:
  - mvn -Declipse.workspace=<path_to_workspace> eclipse:configure-workspace
  - mvn eclipse:eclipse
5. Open Eclipse and use "Import Existing Maven projects" to open the project.


##Contributing
[Refer to the developer.md](developer.md) to find out how to contribute to Event Hubs Java client.
