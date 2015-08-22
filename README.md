#Microsoft Azure SDK for Java

This project provides a client library in Java that makes it easy to consume Microsoft Azure services. For documentation please see the [JavaDocs](http://azure.github.io/azure-sdk-for-java). For a list of libraries and how they are organized, please see the [Azure SDK for Java Features Wiki page] (https://github.com/Azure/azure-sdk-for-java/wiki/Azure-SDK-for-Java-Features).

#Getting Started
You will need Java v1.6+. If you would like to develop on the SDK, you will also need maven.

##Via Maven
Maven distributed jars are the recommended way of getting started with the Azure Java SDK. You can add these dependencies to many of the Java dependency managment tools (Maven, Gradle, Ivy...) and ensure that your project will contain all the Azure dependencies. A list of the artifacts are in the [Azure SDK for Java Features Wiki page] (https://github.com/Azure/azure-sdk-for-java/wiki/Azure-SDK-for-Java-Features).

##Via Git
If using package management is not your thing, then you can grab the sdk directly from source using git. To get the source code of the SDK via git just type:
```bash
git clone https://github.com/Azure/azure-sdk-for-java.git
cd ./azure-sdk-for-java/
```

## Azure Resource Manager Usage
### Authentication
The first step to using the SDK is authentication and permissioning. For people unfamilar with Azure this may be one of the more difficult concepts. For a reference on setting up a service principal from the command line see [Authenticating a service principal with Azure Resource Manager](http://aka.ms/cli-service-principal) or [Unattended Authentication](http://aka.ms/auth-unattended). For a more robust explanation of authentication in Azure, see [Developer’s guide to auth with Azure Resource Manager API](http://aka.ms/arm-auth-dev-guide).

After creating the service principal, you should have three pieces of information, a client id (GUID), client secret (string) and tenant id (GUID) or domain name (string).

### Getting Started Samples
We have a collection of getting started samples which will show you how to [deploy templates](https://github.com/Azure/azure-sdk-for-java/blob/master/azure-mgmt-samples/src/main/java/com/microsoft/azure/samples/templatedeployments/CreateTemplateDeploymentExample.java#L19-L95), [create virtual machines](https://github.com/Azure/azure-sdk-for-java/blob/master/azure-mgmt-samples/src/main/java/com/microsoft/azure/samples/compute/CreateVMExample.java#L37-L99) and [simply authenticate](https://github.com/Azure/azure-sdk-for-java/blob/master/azure-mgmt-samples/src/main/java/com/microsoft/azure/samples/authentication/ServicePrincipalExample.java#L104-L139). There are also a handful of utilities and helpers you will find useful in the azure-mgmt-utilities.

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
* [JavaDocs](http://azure.github.io/azure-sdk-for-java)

