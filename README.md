[![Build Status](https://travis-ci.org/Azure/azure-sdk-for-java.svg?style=flat-square&label=build)](https://travis-ci.org/Azure/azure-sdk-for-java)

# Azure Management Libraries for Java

This README is based on the released stable version (1.1.0). If you are looking for other releases, see [More Information](#more-information).

The Azure Management Libraries for Java is a higher-level, object-oriented API for managing Azure resources. If you are looking for Java client libraries for Azure services, please see [Azure libraries for Java](https://docs.microsoft.com/en-us/java/azure/java-sdk-azure-install) - this page will be maintained regularly to ensure that it is up to date.

Note: This page will be maintained regularly to ensure that it is up to date.

## Feature Availability and Road Map as of Version 1.1.0 ##

<table>
  <tr>
    <th align="left">Service | feature</th>
    <th align="left">Available as GA</th>
    <th align="left">Available as Preview</th>
    <th align="left">Coming soon</th>
  </tr>
  <tr>
    <td>Compute</td>
    <td>Virtual machines and VM extensions<br>Virtual machine scale sets<br>Managed disks</td>
    <td valign="top">Azure container service and registry</td>
    <td valign="top"></td>
  </tr>
  <tr>
    <td>Storage</td>
    <td>Storage accounts</td>
    <td></td>
    <td>Encryption</td>
  </tr>
  <tr>
    <td>SQL Database</td>
    <td>Databases<br>Firewalls<br>Elastic pools</td>
    <td></td>
    <td valign="top">More features</td>
  </tr>
  <tr>
    <td>Networking</td>
    <td>Virtual networks<br>Network interfaces<br>IP addresses<br>Routing table<br>Network security groups<br>Application gateways<br>DNS<br>Traffic managers</td>
    <td valign="top">Load balancers</td>
    <td valign="top">VPN<br>Network watchers<br>More application gateway features</td>
  </tr>
  <tr>
    <td>More services</td>
    <td>Resource Manager<br>Key Vault<br>Redis<br>CDN<br>Batch</td>
    <td valign="top">Web apps<br>Function Apps<br>Service bus<br>Graph RBAC<br>ComsosDB</td>
    <td valign="top">Monitor<br>Scheduler<br>Functions management<br>Search<br>More Graph RBAC features</td>
  </tr>
  <tr>
    <td>Fundamentals</td>
    <td>Authentication - core<br>Async methods</td>
    <td></td>
    <td valign="top"></td>
  </tr>
</table>


> *Preview* features are marked with the @Beta annotation at the class or interface or method level in libraries. These features are subject to change. They can be modified in any way, or even removed, in the future.


#### Azure Authentication

The `Azure` class is the simplest entry point for creating and interacting with Azure resources.

`Azure azure = Azure.authenticate(credFile).withDefaultSubscription();` 

#### Create a Cosmos DB with CosmosDB Programming Model

You can create a Cosmos DB account by using a `define() … create()` method chain.

```java
CosmosAccount cosmosDBAccount = azure.cosmosDBs().define(cosmosDBName)
	.withRegion(Region.US_EAST)
	.withNewResourceGroup(rgName)
	.withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
	.withSessionConsistency()
	.withWriteReplication(Region.US_WEST)
	.withReadReplication(Region.US_CENTRAL)
	.create()
```

#### Create a Virtual Machine

You can create a virtual machine instance by using a `define() … create()` method chain.

```java
System.out.println("Creating a Linux VM");

VirtualMachine linuxVM = azure.virtualMachines().define("myLinuxVM")
	.withRegion(Region.US_EAST)
	.withNewResourceGroup(rgName)
	.withNewPrimaryNetwork("10.0.0.0/28")
	.withPrimaryPrivateIPAddressDynamic()
	.withNewPrimaryPublicIPAddress("mylinuxvmdns")
	.withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
	.withRootUsername("tirekicker")
	.withSsh(sshKey)
	.withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
	.create();
	
System.out.println("Created a Linux VM: " + linuxVM.id());
```

#### Update a Virtual Machine

You can update a virtual machine instance by using an `update() … apply()` method chain.

```java
linuxVM.update()
	.withNewDataDisk(20,  lun,  CachingTypes.READ_WRITE)
	.apply();
```
#### Create a Virtual Machine Scale Set

You can create a virtual machine scale set instance by using another `define() … create()` method chain.

```java
 VirtualMachineScaleSet virtualMachineScaleSet = azure.virtualMachineScaleSets()
     .define(vmssName)
     .withRegion(Region.US_EAST)
     .withExistingResourceGroup(rgName)
     .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
     .withExistingPrimaryNetworkSubnet(network, "Front-end")
     .withPrimaryInternetFacingLoadBalancer(loadBalancer1)
     .withPrimaryInternetFacingLoadBalancerBackends(backendPoolName1, backendPoolName2)
     .withPrimaryInternetFacingLoadBalancerInboundNatPools(natPool50XXto22, natPool60XXto23)
     .withoutPrimaryInternalLoadBalancer()
     .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
     .withRootUsername(userName)
     .withSsh(sshKey)
     .withNewDataDisk(100)
     .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
     .withNewDataDisk(100, 2, CachingTypes.READ_WRITE, StorageAccountTypes.STANDARD_LRS)
     .withCapacity(3)
     .create();
```

#### Create a Network Security Group

You can create a network security group instance by using another `define() … create()` method chain.

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
        .withProtocol(SecurityRuleProtocol.TCP)
        .withPriority(100)
        .withDescription("Allow SSH")
        .attach()
    .defineRule("ALLOW-HTTP")
        .allowInbound()
        .fromAnyAddress()
        .fromAnyPort()
        .toAnyAddress()
        .toPort(80)
        .withProtocol(SecurityRuleProtocol.TCP)
        .withPriority(101)
        .withDescription("Allow HTTP")
        .attach()
    .create();
```

#### Create an Application Gateway

You can create a application gateway instance by using another `define() … create()` method chain.

```java
ApplicationGateway applicationGateway = azure.applicationGateways().define("myFirstAppGateway")
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(resourceGroup)
    // Request routing rule for HTTP from public 80 to public 8080
    .defineRequestRoutingRule("HTTP-80-to-8080")
        .fromPublicFrontend()
        .fromFrontendHttpPort(80)
        .toBackendHttpPort(8080)
        .toBackendIpAddress("11.1.1.1")
        .toBackendIpAddress("11.1.1.2")
        .toBackendIpAddress("11.1.1.3")
        .toBackendIpAddress("11.1.1.4")
        .attach()
    .withExistingPublicIpAddress(publicIpAddress)
    .create();
```

#### Create a Web App

You can create a Web App instance by using another `define() … create()` method chain.

```java
WebApp webApp = azure.webApps()
    .define(appName)
    .withRegion(Region.US_WEST)
    .withNewResourceGroup(rgName)
    .withNewWindowsPlan(PricingTier.STANDARD_S1)
    .create();
```

#### Create a SQL Database

You can create a SQL server instance by using another `define() … create()` method chain.

```java
SqlServer sqlServer = azure.sqlServers().define(sqlServerName)
    .withRegion(Region.US_EAST)
    .withNewResourceGroup(rgName)
    .withAdministratorLogin("adminlogin123")
    .withAdministratorPassword("myS3cureP@ssword")
    .withNewFirewallRule("10.0.0.1")
    .withNewFirewallRule("10.2.0.1", "10.2.0.10")
    .create();
```

Then, you can create a SQL database instance by using another `define() … create()` method chain.

```java
SqlDatabase database = sqlServer.databases().define("myNewDatabase")
    .create();
```

# Sample Code

You can find plenty of sample code that illustrates management scenarios (80+ end-to-end scenarios) for Azure Virtual Machines, Virtual Machine Scale Sets, Managed Disks, Active Directory Azure Container Service and Registry, Storage, Networking, Resource Manager, SQL Database, Cosmos DB, App Service (Web Apps on Windows and Linux), Functions, Service Bus, Key Vault, Redis, CDN and Batch … 

<table>
  <tr>
    <th>Service</th>
    <th>Management Scenario</th>
  </tr>
  <tr>
    <td>Virtual Machines</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/compute-java-manage-vm">Manage virtual machines</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-vm-async">Manage virtual machines asynchronously </a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-availability-sets"> Manage availability set</li>
<li><a href="https://github.com/Azure-Samples/compute-java-list-vm-images">List virtual machine images</li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machine-using-vm-extensions">Manage virtual machines using VM extensions</li>
<li><a href="https://github.com/Azure-Samples/compute-java-list-vm-extension-images">List virtual machine extension images</li>
<li><a href="https://github.com/Azure-Samples/compute-java-create-virtual-machines-from-generalized-image-or-specialized-vhd">Create virtual machines from generalized image or specialized VHD</li>
<li><a href="https://github.com/Azure-Samples/managed-disk-java-create-virtual-machine-using-custom-image">Create virtual machine using custom image from virtual machine</li>
<li><a href="https://github.com/Azure-Samples/managed-disk-java-create-virtual-machine-using-custom-image-from-VHD">Create virtual machine using custom image from VHD</li>
<li><a href="https://github.com/Azure-Samples/managed-disk-java-create-virtual-machine-using-specialized-disk-from-VHD">Create virtual machine by importing a specialized operating system disk VHD</li>
<li><a href="https://github.com/Azure-Samples/managed-disk-java-create-virtual-machine-using-specialized-disk-from-snapshot">Create virtual machine using specialized VHD from snapshot</li>
<li><a href="https://github.com/Azure-Samples/managed-disk-java-convert-existing-virtual-machines-to-use-managed-disks">Convert virtual machines to use managed disks</li>
<li><a href="https://github.com/azure-samples/compute-java-manage-virtual-machine-with-unmanaged-disks">Manage virtual machine with unmanaged disks</li></ul></td>
  </tr>
  <tr>
    <td>Virtual Machines - parallel execution</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/azure-samples/compute-java-manage-virtual-machines-in-parallel">Create multiple virtual machines in parallel</li>
<li><a href="https://github.com/azure-samples/compute-java-manage-virtual-machines-with-network-in-parallel">Create multiple virtual machines with network in parallel</li>
<li><a href="https://github.com/azure-samples/compute-java-create-virtual-machines-across-regions-in-parallel">Create multiple virtual machines across regions in parallel</li>
</ul></td>
  </tr>
  <tr>
    <td>Virtual Machine Scale Sets</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machine-scale-sets">Manage virtual machine scale sets (behind an Internet facing load balancer)</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machine-scale-sets-async">Manage virtual machine scale sets (behind an Internet facing load balancer) asynchronously</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machine-scale-set-with-unmanaged-disks">Manage virtual machine scale sets with unmanaged disks</li>
</ul></td>
  </tr>
<tr>
    <td>Active Directory</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/aad-java-manage-service-principals">Manage service principals using Java</a></li>
<li><a href="https://github.com/Azure-Samples/aad-java-browse-graph-and-manage-roles">Browse graph (users, groups and members) and managing roles</a></li>
<li><a href="https://github.com/Azure-Samples/aad-java-manage-passwords">Manage passwords</li>
</ul></td>
  </tr>
<tr>
    <td>Container Service and Container Registry</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/acr-java-manage-azure-container-registry">Manage container registry</a></li>
<li><a href="https://github.com/Azure-Samples/acs-java-deploy-image-from-acr-to-kubernetes">Deploy an image from container registry to Kubernetes cluster</a></li>
<!--
<li><a href="https://github.com/Azure-Samples/acs-java-deploy-image-from-acr-to-swarm">Deploy an image from container registry to Swarm cluster</li>
<li><a href="https://github.com/Azure-Samples/acs-java-deploy-image-from-docker-hub-to-kubernetes">Deploy an image from Docker hub to Kubernetes cluster</a></li>
<li><a href="https://github.com/Azure-Samples/acs-java-deploy-image-from-docker-hub-to-swarm">Deploy an image from Docker hub to Swarm cluster</li>
-->
<li><a href="https://github.com/Azure-Samples/acs-java-manage-azure-container-service">Manage container service</li>
</ul></td>
  </tr>
  <tr>
    <td>Storage</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/storage-java-manage-storage-accounts">Manage storage accounts</a></li>
<li><a href="https://github.com/Azure-Samples/storage-java-manage-storage-accounts-async">Manage storage accounts asynchronously</a></li>
</ul></td>
  </tr>
  <tr>
    <td>Networking</td>
    <td><ul style="list-style-type:circle">

<li><a href="https://github.com/Azure-Samples/network-java-manage-virtual-network">Manage virtual network</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-virtual-network-async">Manage virtual network asynchronously</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-network-interface">Manage network interface</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-network-security-group">Manage network security group</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-ip-address">Manage IP address</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-internet-facing-load-balancers">Manage Internet facing load balancers</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-internal-load-balancers">Manage internal load balancers</a></li>
</ul>
</td>
  </tr>

  <tr>
    <td>Networking - DNS</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/dns-java-host-and-manage-your-domains">Host and manage domains</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Traffic Manager</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/traffic-manager-java-manage-profiles">Manage traffic manager profiles</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Application Gateway</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/application-gateway-java-manage-simple-application-gateways">Manage application gateways</a></li>
<li><a href="https://github.com/Azure-Samples/application-gateway-java-manage-application-gateways">Manage application gateways with backend pools</a></li>
</ul></td>
  </tr>

  <tr>
    <td>SQL Database</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-db">Manage SQL databases</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-sql-dbs-in-elastic-pool">Manage SQL databases in elastic pools</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-firewalls-for-sql-databases">Manage firewalls for SQL databases</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-sql-databases-across-regions">Manage SQL databases across regions</a></li>
</ul></td>
  </tr>


  <tr>
    <td>App Service - Web Apps on <b>Windows</b></td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps">Manage Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps-with-custom-domains">Manage Web apps with custom domains</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-web-apps">Configure deployment sources for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-web-apps-async">Configure deployment sources for Web apps asynchronously</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-staging-and-production-slots-for-web-apps">Manage staging and production slots for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-scale-web-apps">Scale Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-storage-connections-for-web-apps">Manage storage connections for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-data-connections-for-web-apps">Manage data connections (such as SQL database and Redis cache) for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-authentication-for-web-apps">Manage authentication for Web apps</a></li>
</ul></td>
  </tr>

  <tr>
    <td>App Service - Web Apps on <b>Linux</b></td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps-on-linux">Manage Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-deploy-image-from-acr-to-linux">Deploy a container image from Azure Container Registry to Linux containers</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps-on-linux-with-custom-domains">Manage Web apps with custom domains</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-web-apps-on-linux">Configure deployment sources for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-scale-web-apps-on-linux">Scale Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-storage-connections-for-web-apps-on-linux">Manage storage connections for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-data-connections-for-web-apps-on-linux">Manage data connections (such as SQL database and Redis cache) for Web apps</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Functions</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-functions">Manage functions</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-functions-with-custom-domains">Manage functions with custom domains</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-functions">Configure deployment sources for functions</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-authentication-for-functions">Manage authentication for functions</a></li>
</ul></td>
  </tr>

<tr>
    <td>Cosmos DB</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-configure-for-high-availability">Create a CosmosDB and configure it for high availability</a></li>
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-configure-for-eventual-consistency">Create a CosmosDB and configure it with eventual consistency</a></li>
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-configure-firewall">Create a CosmosDB, configure it for high availability and create a firewall to limit access from an approved set of IP addresses</li>
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-get-mongodb-connection-string">Create a CosmosDB and get MongoDB connection string</li>
</ul></td>
  </tr>


  <tr>
    <td>Service Bus</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/service-bus-java-manage-queue-with-basic-features">Manage queues with basic features</a></li>
<li><a href="https://github.com/Azure-Samples/service-bus-java-manage-publish-subscribe-with-basic-features">Manage publish-subscribe with basic features</a></li>
<li><a href="https://github.com/Azure-Samples/service-bus-java-manage-with-claims-based-authorization">Manage queues and publish-subcribe with cliams based authorization</a></li>
<li><a href="https://github.com/Azure-Samples/service-bus-java-manage-publish-subscribe-with-advanced-features">Manage publish-subscribe with advanced features - sessions, dead-lettering, de-duplication and auto-deletion of idle entries</a></li>
<li><a href="https://github.com/Azure-Samples/service-bus-java-manage-queue-with-advanced-features">Manage queues with advanced features - sessions, dead-lettering, de-duplication and auto-deletion of idle entries</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Resource Groups</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/resources-java-manage-resource-group">Manage resource groups</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-manage-resource">Manage resources</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-using-arm-template">Deploy resources with ARM templates</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-using-arm-template-with-progress">Deploy resources with ARM templates (with progress)</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-virtual-machine-with-managed-disks-using-arm-template">Deploy a virtual machine with managed disks using an ARM template</li></ul></td>
  </tr>

  <tr>
    <td>Redis Cache</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/redis-java-manage-cache">Manage Redis Cache</a></li>
</ul></td>
</tr>

  <tr>
    <td>Key Vault</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/key-vault-java-manage-key-vaults">Manage key vaults</a></li>
</ul></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/cdn-java-manage-cdn ">Manage CDNs</a></li>
</ul></td>
  </tr>
  <tr>
    <td>Batch</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/batch-java-manage-batch-accounts">Manage batch accounts</a></li>
</ul></td>
  </tr>
</table>

# Download


**1.1.0**

If you are using released builds from 1.1.0, add the following to your POM file:

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure</artifactId>
    <version>1.1.0</version>
</dependency>
```

# Pre-requisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven
- Azure Service Principal - see [how to create authentication info](./AUTH.md).


## Help

If you are migrating your code to 1.1.0, you can use these notes for [preparing your code for 1.1.0 from 1.0.0](./notes/prepare-for-1.1.0.md).

If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

# Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

# More Information
* [Javadoc](http://azure.github.io/azure-sdk-for-java)
* [http://azure.com/java](http://azure.com/java)
* If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

### Previous Releases and Corresponding Repo Branches

| Version           | SHA1                                                                                      | Remarks                                               |
|-------------------|-------------------------------------------------------------------------------------------|-------------------------------------------------------|
| 1.0.0       | [1.0.0](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0)               | Tagged release for 1.0.0 version of Azure management libraries |
| 1.0.0-beta5       | [1.0.0-beta5](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0-beta5)               | Tagged release for 1.0.0-beta5 version of Azure management libraries |
| 1.0.0-beta4.1       | [1.0.0-beta4.1](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0-beta4.1)               | Tagged release for 1.0.0-beta4.1 version of Azure management libraries |
| 1.0.0-beta3       | [1.0.0-beta3](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0-beta3)               | Tagged release for 1.0.0-beta3 version of Azure management libraries |
| 1.0.0-beta2       | [1.0.0-beta2](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0-beta2)               | Tagged release for 1.0.0-beta2 version of Azure management libraries |
| 1.0.0-beta1       | [1.0.0-beta1](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0-beta1)               | Maintenance branch for AutoRest generated raw clients |
| 1.0.0-beta1+fixes | [1.0.0-beta1+fixes](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0-beta1+fixes) | Stable build for AutoRest generated raw clients       |
| 0.9.x-SNAPSHOTS   | [0.9](https://github.com/Azure/azure-sdk-for-java/tree/0.9)                               | Maintenance branch for service management libraries   |
| 0.9.3             | [0.9.3](https://github.com/Azure/azure-sdk-for-java/tree/v0.9.3)                         | Latest release for service management libraries       |

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
