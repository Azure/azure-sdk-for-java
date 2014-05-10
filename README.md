#Microsoft Azure SDK for Java

This project provides a client library in Java that makes it easy to consume Microsoft Azure services. For documentation please see the [Microsoft Azure Java Developer Center](http://azure.microsoft.com/en-us/develop/java/).

#Features


* Service Bus
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
    * Compute Management
    * Web Site Management
    * Network Management
    * Storage Management
    * Sql Database Management
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
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-management</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-management-compute</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-management-network</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-management-resources</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-management-sql</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-management-storage</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-management-websites</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-media</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-servicebus</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-serviceruntime</artifactId>
  <version>0.5.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-azure-api-tracing-util</artifactId>
  <version>0.5.0</version>
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

