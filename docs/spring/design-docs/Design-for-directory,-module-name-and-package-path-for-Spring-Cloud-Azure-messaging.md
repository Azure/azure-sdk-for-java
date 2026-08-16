This article is to design the repository directory for Spring Cloud Azure project.

## Repository Directory
To horizontally extend the curent Spring Azure projects in azure-sdk-for-java repository, we plan to use multiple secondary directories instead of putting all modules under **sdk/spring/**. The detailed secondary directories are listed as follows:

1. spring: it's designed to contain core libraries providing common frameworks for all Azure Spring libraries and Spring Cloud starters for the supported Azure services.
2. spring-messaging: to provide support for Azure Event Hubs, Azure Service Bus and Azure Storage Queue on the basis of spring-messaging project
3. spring-integration: to provide support for Spring Integration which includes Spring Integration support for Event Hubs, Service Bus and Storage Queue
4. spring-cloud-stream: to provide support for Spring Cloud Stream which includes Spring Cloud Stream support for Event Hubs and Service Bus


## Overview
The overview of the above design is shown here:

<img width="601" alt="spring-directory-4 0" src="https://user-images.githubusercontent.com/4465723/137453509-893fe507-b74f-4e79-aa49-7b9fee2f2f3b.png">



