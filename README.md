[![Build Status](https://travis-ci.org/Azure/azure-sdk-for-java.svg?style=flat-square&label=build)](https://travis-ci.org/Azure/azure-sdk-for-java)

#Azure Management Libraries for Java

This README is based on the latest released preview version (1.0.0-beta2). If you are looking for other releases, see [More Information](#more-information)

The Azure Management Libraries for Java is a higher-level, object-oriented API for managing Azure resources.


> **1.0.0-beta2** is a developer preview that supports major parts of Azure Compute, Storage, Networking and Resource Manager. The next preview version of the Azure Management Libraries for Java is a work in-progress. We will be adding support for more Azure services and tweaking the API over the next few months.

**Azure Authentication**

The `Azure` class is the simplest entry point for creating and interacting with Azure resources.

`Azure azure = Azure.authenticate(credFile).withDefaultSubscription();` 

**Create a Virtual Machine**

You can create a virtual machine instance by using the `define() … create()` method chain.

```java
System.out.println("Creating a Linux VM");

VirtualMachine linuxVM = azure.virtualMachines().define("myLinuxVM")
	.withRegion(Region.US_EAST)
	.withNewResourceGroup("myResourceGroup")
	.withNewPrimaryNetwork("10.0.0.0/28")
	.withPrimaryPrivateIpAddressDynamic()
	.withNewPrimaryPublicIpAddress("mylinuxvmdns")
	.withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
	.withRootUserName("tirekicker")
	.withSsh(sshKey)
	.withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
	.create();
```

System.out.println("Created a Linux VM: " + linuxVM.id());


**Update a Virtual Machine**

You can update a virtual machine instance by using the `update() … apply()` method chain.

```java
linuxVM.update()
    .defineNewDataDisk(dataDiskName)
    .withSizeInGB(20)
    .withCaching(CachingTypes.READ_WRITE)
    .attach()
    .apply();
```

**Create a Network Security Group**

You can create a network security group instance by using the `define() … create()` method chain.

```java
NetworkSecurityGroup frontEndNSG = azure.networkSecurityGroups().define(frontEndNSGName)
    .withRegion(Region.US_EAST)
    .withNewResourceGroup(rgName)
    .defineRule("ALLOW-SSH")
        .allowInbound()
        .fromAnyAddress()
        .fromAnyPort()
        .toAnyAddress()
        .toPort(22)
        .withProtocol(NetworkSecurityRule.Protocol.TCP)
        .withPriority(100)
        .withDescription("Allow SSH")
        .attach()
    .defineRule("ALLOW-HTTP")
        .allowInbound()
        .fromAnyAddress()
        .fromAnyPort()
        .toAnyAddress()
        .toPort(80)
        .withProtocol(NetworkSecurityRule.Protocol.TCP)
        .withPriority(101)
        .withDescription("Allow HTTP")
        .attach()
    .create();
```


#Sample Code

You can find plenty of sample code that illustrates management scenarios in Azure Compute, Storage, Network and Resource Manager … 


> [Asir's note to editors] - will hyperlink as soon as these samples are visible

- Manage virtual machine
- Manage availability set
- List virtual machine images
- Manage storage accounts
- Manage virtual network
- Manage network interface
- Manage network security group
- Manage IP address
- Manage resource groups
- Manage resources
- Deploy resources with ARM templates
- Deploy resources with ARM templates (with progress)

# Download


**1.0.0-beta2**

If you are using released builds from 1.0.0-beta2, add the following to your POM file:

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure</artifactId>
    <version>1.0.0-beta2</version>
</dependency>
```

or Gradle:

    compile group: 'com.microsoft.azure', name: 'azure', version: '1.0.0-beta2'

**Snapshots builds for this repo**

If you are using snapshots builds for this repo, add the following repository and dependency to your POM file:

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

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

or Gradle:
```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
    ....
}
```


    compile group: 'com.microsoft.azure', name: 'azure', version: '1.0.0-SNAPSHOTS'

#Pre-requisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven
- Azure Service Principal - see [how to create authentication info](./AUTH.md).


## Help
If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

#Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

#More Information
* [Javadoc](http://azure.github.io/azure-sdk-for-java)
* [http://azure.com/java](http://azure.com/java)
* If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

**Previous Releases and Corresponding Repo Branches**

**[INSERT TABLE]**

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.