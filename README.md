#Microsoft Azure SDK for Java

This project provides a client library in Java that makes it easy to consume Microsoft Azure services. For documentation please see the [Microsoft Azure Java Developer Center](http://azure.microsoft.com/en-us/develop/java/).


#Features


* Service Bus
	* Storage
		* The Azure Storage SDK is maintained in a separate repository. You can find the Azure Storage Java SDK at [https://github.com/Azure/azure-storage-java](https://github.com/Azure/azure-storage-java). 
    * Queues
        * Create/Read/Update/Delete queues
        * Send/Receive/Unlock/Delete messages
        * Renew message lock
        * Message forwarding
    * Topics
        * Create/Read/Update/Delete topics
        * Create/Read/Update/Delete subscriptions
        * Create/Read/Update/Delete rules
        * Send/Receive/Unlock/Delete messages
        * Renew message lock
        * Message forwarding
* Media Services
    * Create/Read/Update/Delete access policies
    * Create/Read/Update/Delete asset files
    * Create/Read/Update/Delete assets
    * Create/Read/Update/Delete/Rebind content keys
    * Create/Read/Update/Cancel/Delete jobs
    * Add/Get job notifications
    * Create/Read/Update/Delete notification endpoints
* Service Management
    * Management
      * Create/Delete/Get/List/Update affinity group
      * List location
      * Create/Delete/Get/List management certificate
      * List role size
      * Get/List subscription
      * Register/Unregister resource
    * Compute Management
      * ChangeConfiguration/Delete/Reboot/Reimage/Swap/Update/Upgrade deployment
      * AddExtension/Delete/CheckNameAvailability/Create/Delete/Get/GetDetailed/GetExtension/List/ListAvailableExtension/Update hosted service operations
      * List/ListFamilies operating system
      * Create/Delete/Get/List service certificate
      * Create/Delete/Get/List/Update data disk or disk
      * List/ListVersions virtual machine extension
      * Capture/Create/Delete/Restart/Shutdown/Start/Update/Start/Update/UpdateLoadBalancedEndpointSet virtual machine
      * Create/Delete/Get/List/Update virtual machine OS image
      * Delete/List virtual machine image
    * Websites Management
      * Create/Delete/Get/List/Update server farm
      * Create/Delete/DeleteRepository/GeneratePassword/Get/GetConfiguration/GetHistoricalUsageMetrics/GetInstanceIds/GetPublishProfile/GetRepository/GetUsageMetrics/isHostnameAvailable/Restart/SwapSlots/SyncRepository/Update/UpdateConfiguration of Azure web sites
      * CreatePublishingUser/Get/List/ListGeoRegions/ListPublishingUser/ListWebSites of web space
    * Network Management
      * Create/Delete/Get/List client root certificate
      * Connect/Disconnect/Testing/Create/Delete/Failover/ResetSharedKey gateway operations
      * SetConfiguration/GetConfiguration/Get/List network 
      * Create/Delete/Get/List reserved IP operations
      * Check static IP operations
    * Storage Management
      * Create/CheckNameAvailability/Delete/Get/GetAsync/GetKeys/List/RegenerateKeys/Update Azure storage account
    * Sql Database Management
      * Export/GetStatus/Import database 
      * Get/List database operation's operation
      * Create/Delete/Get/GetAsync/GetEventLogs/List/Update database operations
      * Create/Delete/Get/List/Update firewall rule
      * ChangeAdministratorPassword/Create/Delete/List server operations
      * Get/List service objective
* Service Runtime
    * Retrieve information about the state of your Azure Compute instances


#Getting Started

##Download
###Option 1: Via Git

To get the source code of the SDK via git just type:

    git clone git://github.com/Azure/azure-sdk-for-java.git
    cd ./azure-sdk-for-java/
    mvn compile

###Option 2: Via Maven

To get the binaries of this library as distributed by Microsoft, ready for use
within your project you can also have them installed by the Java package manager Maven.

```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-management</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-management-compute</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-management-network</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-management-sql</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-management-storage</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-management-websites</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-media</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-servicebus</artifactId>
  <version>0.6.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-serviceruntime</artifactId>
  <version>0.6.0</version>
</dependency>
```

##Minimum Requirements

* Java 1.6
* (Optional) Maven


##Usage

To use this SDK to call Microsoft Azure services, you need to first create an
account.  To host your Java code in Microsoft Azure, you additionally need to download
the full Microsoft Azure SDK for Java - which includes packaging, emulation, and
deployment tools.


#Need Help?

Be sure to check out the Microsoft Azure [Developer Forums on Stack Overflow](http://go.microsoft.com/fwlink/?LinkId=234489) if you have trouble with the provided code.

#Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.com/guidelines.html).

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/Azure/azure-sdk-for-java/issues) section of the project.

#Learn More

* [Microsoft Azure Java Developer Center](http://azure.microsoft.com/en-us/develop/java/)
* [JavaDocs](http://dl.windowsazure.com/javadoc/)

