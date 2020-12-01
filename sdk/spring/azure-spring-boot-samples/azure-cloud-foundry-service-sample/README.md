# Cloud Foundry Sample for Azure Spring Boot client library for Java
## Key concepts
This sample project demonstrates how to consume azure services exposed through the Microsoft Azure Service Broker for
applications running in Cloud Foundry or by consuming the service configuration through a user provided service.
 

## Getting started

### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Pre-reqs:  Create required service instances in Cloud Foundry
Before you can create any service instances, you'll need to create a resource group on Azure. Then you'll reference that
resource group name below.  You'll also need access to a Cloud Foundry environment with the Azure Service Broker installed.
You can issue the following command to see if it is installed:

```
cf marketplace
```

If installed, you should see something like this:

```
service              plans                                                          description
azure-cosmosdb       standard*                                                      Azure CosmosDb Service
azure-mysqldb        basic50*, basic100*, standard100*, standard200*, standard400*,
                     standard800*                                                   Azure Database for MySQL Service
azure-postgresqldb   basic50*, basic100*, standard100*, standard200*, standard400*,
                     standard800*                                                   Azure Database for PostgreSQL Service
azure-rediscache     basic*, standard*, premium*                                    Azure Redis Cache Service
azure-servicebus     standard*                                                      Azure Service Bus Service
azure-sqldb          basic*, StandardS0*, StandardS1*, StandardS2*, StandardS3*,
                     PremiumP1*, PremiumP2*, PremiumP4*, PremiumP6*, PremiumP11*,
                     DataWarehouse100*, DataWarehouse1200*                          Azure SQL Database Service
azure-storage        standard*                                                      Azure Storage Service

* These service plans have an associated cost. Creating a service instance will incur this cost.

TIP:  Use 'cf marketplace -s SERVICE' to view descriptions of individual plans of a given service.
```


### Service Bus
To create the service instance, you'll need to create a JSON file (ex. azure-servicebus.json) 
with the configuration details for your service instance.
Specify your existing resource group name for RESOURCE_GROUP_NAME, then a new
value for NAMESPACE_NAME, and your Azure location (ex. "centralus") for LOCATION.

```
{
  "resource_group_name": "RESOURCE_GROUP_NAME",
  "namespace_name": "NAMESPACE_NAME",
  "location": "LOCATION",
  "type": "Messaging",
  "messaging_tier": "Standard"
}

```

Now you can create the new service

```
cf create-service azure-servicebus standard azure-servicebus-service -c ./azure-servicebus.json
```

The Service Bus Spring Starter assumes you've already created a queue and topic in Azure, so you'll need to manually create them before
running this sample application.  You can provide their names in the application.properties file, or as environment variables that Spring translates into properties.
See the application.properties file in this project for an example. 

### Storage
To create the service instance, you'll need to create a JSON file (ex. azure-storage.json) 
with the configuration details for your service instance.
Specify your existing resource group name for RESOURCE_GROUP_NAME, then a new
value for STORAGE_ACCOUNT_NAME, and your Azure location (ex. "centralus") for LOCATION

```
{
  "resource_group_name": "RESOURCE_GROUP_NAME",
  "storage_account_name": "STORAGE_ACCOUNT_NAME",
  "location": "LOCATION",
  "account_type": "Standard_LRS"
}

```

Now you can create the new service

```
cf create-service azure-storage standard azure-storage-service -c ./azure-storage.json
```

### Cosmos DB
To create the service instance, you'll need to create a JSON file (ex. azure-documentdb.json) 
with the configuration details for your service instance.
Specify your existing resource group name for RESOURCE_GROUP_NAME, then a new
value for DOCDB_ACCOUNT_NAME and DOCDB_NAME, and your Azure location (ex. "centralus") for LOCATION

```
{
  "resourceGroup": "RESOURCE_GROUP_NAME",
  "docDbAccountName": "DOCDB_ACCOUNT_NAME",
  "docDbName": "DOCDB_NAME",
  "location": "LOCATION"
}

```

Now you can create the new service

```
cf create-service azure-documentdb standard azure-documentdb-service -c ./azure-documentdb.json
```

### User Provided Service

It is also possible to use a user provided service to get the credentials used to connect to the service, and make use of the start to automatically bind just as if with an azure broker service.

This allows applications to share the same storage across pcf foundations, for example.

The Pre Reqs section don't really apply for user provided services, however, you may create a service using the Microsoft Azure Service Broker and take the credentials from that service to create other user provided services which will connect to the same storage (see example further ahead).

To create a user provided service compatible with the starter, you first need to create a json with the service credentials (ex. azure-service-credentials.json)
```
{
    "azure-service-broker-name":"azure-documentdb",
    "documentdb_host_endpoint": "https://docdb-account.documents.azure.com:443/",                                       
    "documentdb_master_key": "XXPSWDXXX==",
    "documentdb_database_id": "databasename",                                                                             
    "documentdb_database_link": "dbs/XXXXX==/"                                                                         
}
```
You basically create the credentials block the same way as the credentials block created by the Microsoft Azure Service Broker (see example further ahead), and add the attribute `azure-service-broker-name` with the name of the broker responsible for the service (in this case, "azure-documentdb").

You can then create the service

```
cf create-user-provided-service user-provided-azure-service -p ./azure-service-credentials.json
```

It is worth noticing that the Microsoft Azure Service Broker doesn't actually need to be available for this to work, but the broker names should be the same as the ones the Service Broker provide (as shown at the beginning).

## Examples
### Example Service Creation with a Microsoft Azure Service Broker service

Taking as an example the documentdb service created in the previous section, you may inspect the environment of the application to see which credentials the service has
```
System-Provided:
{
 "VCAP_SERVICES": {
  "azure-documentdb": [
   {
    "credentials": {
     "documentdb_database_id": "databasename",
     "documentdb_database_link": "dbs/XXXXX==/",
     "documentdb_host_endpoint": "https://docdb-account.documents.azure.com:443/",
     "documentdb_master_key": "XXPSWDXXX"
    },
    "label": "azure-documentdb",
    "name": "azure-documentdb-service",
    "plan": "standard",
    "provider": null,
    "syslog_drain_url": null,
    "tags": [],
    "volume_mounts": []
   }
  ]
 }
}
```
The credentials attribute can be used to create the json file for the user provided service.
```
{
    "azure-service-broker-name":"azure-documentdb",
    "documentdb_host_endpoint": "https://docdb-account.documents.azure.com:443/",                                       
    "documentdb_master_key": "XXPSWDXXX==",
    "documentdb_database_id": "databasename",                                                                             
    "documentdb_database_link": "dbs/XXXXX==/"                                                                         
}
```
Which can then be used to create user provided services in any space or foundation, and sharing the same storage.

### To push sample application to PCF
Login to your PCF environment and run "cf push" from the cloudfoundry/azure-cloud-foundry-service-sample folder.

The manifest.yml file in that folder specifies meta-data about the application, including the service bindings to the service instances.


```
---
applications:
- name: cf-app
  memory: 1G
  path: ./target/azure-cloud-foundry-service-sample-1.0.0.jar
  random-route: true
  services:
    - azure-documentdb-service
    - azure-servicebus-service

```

### Try the Cloud Foundry Demo
Get the URL from the output of the "cf-push" command, and append
the following endpoints:
* "/" - web page with links to the demos below
* "/sb" - this demo uses Azure Service Bus to put a message on a queue, retrieve it, and delete it.
* "/docdb" -  this demo uses Azure DocumentDB to store a new User object.

## Troubleshooting
## Next steps
## Contributing

[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
