[![Build Status](https://travis-ci.org/Azure/azure-sdk-for-java.svg?style=flat-square&label=build)](https://travis-ci.org/Azure/azure-sdk-for-java)
#Azure Management Libraries for Java

This project provides a new, simplified Azure management libraries for Java. Our goal is to improve the developer experience by providing a higher-level, object-oriented API, optimized for readability and writability. 

For documentation please see the [Javadoc](http://azure.github.io/azure-sdk-for-java). 

For a list of libraries and how they are organized, please see the [Azure Management Libraries for Java Features Wiki page] (https://github.com/Azure/azure-sdk-for-java/wiki/Azure-SDK-for-Java-Features).

#[EDIT THIS] Download
**Notes:** If you are using snapshots builds from beta1 we recommend going to http://adxsnapshots.azurewebsites.net/ and find the exact version number. The latest beta1 snapshot versions are
- client-runtime: 1.0.0-20160513.000825-29
- azure-client-runtime: 1.0.0-20160513.000812-28
- azure-client-authentication: 1.0.0-20160513.000802-24

To compile either this repo, you need snapshot builds in sonatype snapshots repository.  Add the following to your pom:
```xml
  <repositories>
    <repository>
      <id>ossrh</id>
      <name>Sonatype Snapshots</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      <layout>default</layout>
      <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
      </snapshots>
    </repository>
  </repositories>
```
or Gradle:
```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
    ....
}
```

#Getting Started
You will need Java v1.7+. If you would like to develop on the SDK, you will also need maven.

## [EDIT THIS] Azure Resource Manager (ARM) Usage
```java
ResourceManagementClient client = new ResourceManagementClientImpl(
    new ApplicationTokenCredentials("client-id", "tenant-id", "secret", null) // see Authentication
);
client.setSubscriptionId(System.getenv("subscription-id"));
client.setLogLevel(HttpLoggingInterceptor.Level.BODY);

ResourceGroup group = new ResourceGroup();
group.setLocation("West US");
client.getResourceGroups().createOrUpdate("myresourcegroup", group);
```

### [EDIT THIS] Authentication
The first step to using the SDK is authentication and permissioning. For people unfamilar with Azure this may be one of the more difficult concepts. For a reference on setting up a service principal from the command line see [Authenticating a service principal with Azure Resource Manager](http://aka.ms/cli-service-principal) or [Unattended Authentication](http://aka.ms/auth-unattended). For a more robust explanation of authentication in Azure, see [Developer’s guide to auth with Azure Resource Manager API](http://aka.ms/arm-auth-dev-guide).

After creating the service principal, you should have three pieces of information, a client id (GUID), client secret (string) and tenant id (GUID) or domain name (string). By feeding them into the `ApplicationTokenCredentials` and initialize the ARM client with it, you should be ready to go.

## Need some help?
If you encounter any bugs with the SDK please file an issue via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

#Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

#Learn More
* [Javadoc](http://azure.github.io/azure-sdk-for-java)
* [http://azure.com/java](http://azure.com/java)
* If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.