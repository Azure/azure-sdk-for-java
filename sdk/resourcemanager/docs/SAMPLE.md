# Code snippets and samples

### Azure Authentication

The `Azure` class is the simplest entry point for creating and interacting with Azure resources.

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.environment().getActiveDirectoryEndpoint())
    .build();
Azure azure = Azure
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

To learn more about authentication in the Azure Management Libraries for Java, see [AUTH.md](AUTH.md).

### Virtual Machines

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
	.withNewDataDisk(20, lun, CachingTypes.READ_WRITE)
	.apply();
```

#### Create a Virtual Machine Scale Set

You can create a virtual machine scale set instance by using a `define() … create()` method chain.

```java
 VirtualMachineScaleSet virtualMachineScaleSet = azure.virtualMachineScaleSets().define(vmssName)
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

#### Ready-to-run code samples for virtual machines

<table>
  <tr>
    <th>Service</th>
    <th>Management Scenario</th>
  </tr>
  <tr>
    <td>Virtual Machines</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachine.java">Manage virtual machines</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineAsync.java">Manage virtual machines asynchronously</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageAvailabilitySet.java"> Manage availability set</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ListVirtualMachineImages.java">List virtual machine images</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineExtension.java">Manage virtual machines using VM extensions</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ListVirtualMachineExtensionImages.java">List virtual machine extension images</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/CreateVirtualMachinesUsingCustomImageOrSpecializedVHD.java">Create virtual machines from generalized image or specialized VHD</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/CreateVirtualMachineUsingCustomImageFromVM.java">Create virtual machine using custom image from virtual machine</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/CreateVirtualMachineUsingCustomImageFromVHD.java">Create virtual machine using custom image from VHD</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/CreateVirtualMachineUsingSpecializedDiskFromVhd.java">Create virtual machine by importing a specialized operating system disk VHD</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/CreateVirtualMachineUsingSpecializedDiskFromSnapshot.java">Create virtual machine using specialized VHD from snapshot</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ConvertVirtualMachineToManagedDisks.java">Convert virtual machines to use managed disks</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineWithUnmanagedDisks.java">Manage virtual machine with unmanaged disks</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageStorageFromMSIEnabledVirtualMachine.java">Manage Azure resources from a virtual machine with system assigned managed service identity (MSI)</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineFromMSIEnabledVirtualMachine.java">Manage Azure resources from a virtual machine with managed service identity (MSI) credentials</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageUserAssignedMSIEnabledVirtualMachine.java">Manage Azure resources from a virtual machine with system assigned managed service identity (MSI)</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageZonalVirtualMachine.java">Manage virtual machines in availability zones</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageZonalVirtualMachineScaleSet.java">Manage virtual machine scale sets in availability zones</a></li>

<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ListComputeSkus.java">List compute SKUs</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageManagedDisks.java">Manage virtual machine with managed disks</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineWithDisk.java">Manage virtual machine with disks</li>

</ul></td>
  </tr>
  <tr>
    <td>Virtual Machines - parallel execution</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachinesInParallel.java">Create multiple virtual machines in parallel</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageVirtualMachinesInParallelWithNetwork.java">Create multiple virtual machines with network in parallel</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/CreateVirtualMachinesInParallel.java">Create multiple virtual machines across regions in parallel</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/CreateVirtualMachinesAsyncTrackingRelatedResources.java">Create multiple virtual machines in parallel and track related resources</li>
</ul></td>
  </tr>
  <tr>
    <td>Virtual Machine Scale Sets</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineScaleSet.java">Manage virtual machine scale sets (behind an Internet facing load balancer)</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineScaleSetAsync.java">Manage virtual machine scale sets (behind an Internet facing load balancer) asynchronously</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageVirtualMachineScaleSetWithUnmanagedDisks.java">Manage virtual machine scale sets with unmanaged disks</li>
</ul></td>
  </tr>
</table>

### Networking

#### Create a virtual network

You can create a virtual network by using a `define() … create()` method chain.

```java
Network network = networks.define("mynetwork")
	.withRegion(Region.US_EAST)
	.withNewResourceGroup()
	.withAddressSpace("10.0.0.0/28")
	.withSubnet("subnet1", "10.0.0.0/29")
	.withSubnet("subnet2", "10.0.0.8/29")
	.create();
```

#### Create a network security group

You can create a network security group instance by using a `define() … create()` method chain.

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

You can create a application gateway instance by using a `define() … create()` method chain.

```java
ApplicationGateway applicationGateway = azure.applicationGateways().define("myFirstAppGateway")
    .withRegion(Region.US_EAST)
    .withExistingResourceGroup(resourceGroup)
    // Request routing rule for HTTP from public 80 to public 8080
    .defineRequestRoutingRule("HTTP-80-to-8080")
        .fromPublicFrontend()
        .fromFrontendHttpPort(80)
        .toBackendHttpPort(8080)
        .toBackendIPAddress("11.1.1.1")
        .toBackendIPAddress("11.1.1.2")
        .toBackendIPAddress("11.1.1.3")
        .toBackendIPAddress("11.1.1.4")
        .attach()
    .withExistingPublicIPAddress(publicIpAddress)
    .create();
```

#### Ready-to-run code samples for networking

<table>
  <tr>
    <th>Service</th>
    <th>Management Scenario</th>
  </tr>
  <tr>
    <td>Networking</td>
    <td><ul style="list-style-type:circle">

<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageVirtualNetwork.java">Manage virtual network</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageVirtualNetworkAsync.java">Manage virtual network asynchronously</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageNetworkInterface.java">Manage network interface</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageNetworkSecurityGroup.java">Manage network security group</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageIPAddress.java">Manage IP address</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageInternetFacingLoadBalancer.java">Manage Internet facing load balancers</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageInternalLoadBalancer.java">Manage internal load balancers</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/CreateSimpleInternetFacingLoadBalancer.java">Create simple Internet facing load balancer</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageNetworkWatcher.java">Use net watcher</a>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageNetworkPeeringInSameSubscription.java">Manage network peering between two virtual networks</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/VerifyNetworkPeeringWithNetworkWatcher.java">Use network watcher to check connectivity between virtual machines in peered networks</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageVpnGatewaySite2SiteConnection.java">Manage virtual network with site-to-site VPN connection</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageVpnGatewayVNet2VNetConnection.java">Manage virtual network to virtual network VPN connection</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageVpnGatewayPoint2SiteConnection.java">Manage client to virtual network VPN connection</a></li>
</ul>
</td>
  </tr>

  <tr>
    <td>DNS</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/dns/samples/ManageDns.java">Host and manage domains</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Application Gateway</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageSimpleApplicationGateway.java">Manage application gateways</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageApplicationGateway.java">Manage application gateways with backend pools</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Express Route</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//network/samples/ManageExpressRoute.java">Create and configure Express Route</a></li>
</ul></td>
  </tr>

</table>


### Application Services

#### Create a Web App

You can create a Web App instance by using a `define() … create()` method chain.

```java
WebApp webApp = azure.webApps()
    .define(appName)
    .withRegion(Region.US_WEST)
    .withNewResourceGroup(rgName)
    .withNewWindowsPlan(PricingTier.STANDARD_S1)
    .create();
```

#### Ready-to-run code samples for Application Services

<table>
  <tr>
    <th>Service</th>
    <th>Management Scenario</th>
  </tr>

  <tr>
    <td>Web Apps on <b>Windows</b></td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppBasic.java">Manage Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppWithDomainSsl.java">Manage Web apps with custom domains</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppSourceControl.java">Configure deployment sources for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppSourceControlAsync.java">Configure deployment sources for Web apps asynchronously</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppSlots.java">Manage staging and production slots for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppWithTrafficManager.java">Scale Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppStorageAccountConnection.java">Manage storage connections for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppSqlConnection.java">Manage data connections (such as SQL database and Redis cache) for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppWithAuthentication.java">Manage authentication for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppCosmosDbThroughKeyVault.java">Safegaurd Web app secrets in Key Vault</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppCosmosDbByMsi.java">Safegaurd Web app secrets in Key Vault using convenience API</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageWebAppLogs.java">Get logs for Web apps</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Web Apps on <b>Linux</b></td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppBasic.java">Manage Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppWithContainerRegistry.java">Deploy a container image from Azure Container Registry to Linux containers</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppWithDomainSsl.java">Manage Web apps with custom domains</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppSourceControl.java">Configure deployment sources for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppWithTrafficManager.java">Scale Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppStorageAccountConnection.java">Manage storage connections for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppSqlConnection.java">Manage data connections (such as SQL database and Redis cache) for Web apps</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageLinuxWebAppCosmosDbByMsi.java">Safegaurd Web app secrets in Key Vault on Linux</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Functions</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageFunctionAppBasic.java">Manage functions</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageFunctionAppWithDomainSsl.java">Manage functions with custom domains</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageFunctionAppSourceControl.java">Configure deployment sources for functions</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageFunctionAppWithAuthentication.java">Manage authentication for functions</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appservice/samples/ManageFunctionAppLogs.java">Get function logs</a></li>
</ul></td>
  </tr>

</table>

### Databases and Storage

#### Create a Cosmos DB with CosmosDB Programming Model

You can create a Cosmos DB account by using a `define() … create()` method chain.

```java
CosmosAccount cosmosDBAccount = azure.cosmosDBAccounts().define(cosmosDBName)
	.withRegion(Region.US_EAST)
	.withNewResourceGroup(rgName)
	.withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
	.withSessionConsistency()
	.withWriteReplication(Region.US_WEST)
	.withReadReplication(Region.US_CENTRAL)
	.create()
```

#### Create a SQL Database

You can create a SQL server instance by using a `define() … create()` method chain.

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

Then, you can create a SQL database instance by using a `define() … create()` method chain.

```java
SqlDatabase database = sqlServer.databases().define("myNewDatabase")
	...
    .create();
```

#### Ready-to-run code samples for databases

<table>
  <tr>
    <th>Service</th>
    <th>Management Scenario</th>
  </tr>

  <tr>
    <td>Storage</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//storage/samples/ManageStorageAccount.java">Manage storage accounts</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//storage/samples/ManageStorageAccountAsync.java">Manage storage accounts asynchronously</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//storage/samples/ManageStorageAccountNetworkRules.java">Manage network rules of a storage account</a></li>
</ul></td>
  </tr>

  <tr>
    <td>SQL Database</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlDatabase.java">Manage SQL databases</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlDatabaseInElasticPool.java">Manage SQL databases in elastic pools</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlFirewallRules.java">Manage firewalls for SQL databases</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlDatabasesAcrossDifferentDataCenters.java">Manage SQL databases across regions</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlImportExportDatabase.java">Import and export SQL databases</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlWithRecoveredOrRestoredDatabase.java">Restore and recover SQL databases</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/GettingSqlServerMetrics.java">Get SQL Database metrics</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlFailoverGroups.java">Manage SQL Database Failover Groups</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlServerDnsAliases.java">Manage SQL Server DNS aliases</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlServerKeysWithAzureKeyVaultKey.java">Manage SQL secrets (Server Keys) in Azure Key Vault</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlVirtualNetworkRules.java">Manage SQL Virtual Network Rules</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//sql/samples/ManageSqlServerSecurityAlertPolicy.java">Manage SQL Server Security Alert Policy</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Cosmos DB</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//cosmos/samples/ManageHACosmosDB.java">Create a CosmosDB and configure it for high availability</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//cosmos/samples/CreateCosmosDBWithEventualConsistency.java">Create a CosmosDB and configure it with eventual consistency</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//cosmos/samples/CreateCosmosDBWithIPRange.java">Create a CosmosDB, configure it for high availability and create a firewall to limit access from an approved set of IP addresses</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//cosmos/samples/CreateCosmosDBWithKindMongoDB.java">Create a CosmosDB and get MongoDB connection string</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//cosmos/samples/CreateCosmosDBTableWithVirtualNetworkRule.java">Create a CosmosDB Table with a virtual network rule</li>
</ul></td>
  </tr>

</table>


### Other code samples

<table>
  <tr>
    <th>Service</th>
    <th>Management Scenario</th>
  </tr>
<tr>
    <td>Active Directory</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//authorization/samples/ManageServicePrincipal.java">Manage service principals using Java</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//authorization/samples/ManageServicePrincipalCredentials.java">Manage credentials for service principals using Java</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//authorization/samples/ManageUsersGroupsAndRoles.java">Manage users and groups and manage their roles</a></li>
<!--li><a href="https://github.com/Azure-Samples/aad-java-manage-passwords">Manage passwords</li-->
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//compute/samples/ManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup.java">Manage Azure resources from a managed service identity (MSI) enabled virtual machine that belongs to an Azure Active Directory (AAD) security group</a></li>
</ul></td>
  </tr>

<tr>
    <td>Container Service<br>Container Registry and <br>Container Instances</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//containerregistry/samples/ManageContainerRegistry.java">Manage container registry</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//containerregistry/samples/ManageContainerRegistryWithWebhooks.java">Manage container registry with Web hooks</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//kubernetescluster/samples/ManageKubernetesCluster.java">Manage Kubernetes cluster (AKS)</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//kubernetescluster/samples/DeployImageFromContainerRegistryToKubernetes.java">Deploy an image from container registry to Kubernetes cluster (AKS)</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//kubernetescluster/samples/ManagedKubernetesClusterWithAdvancedNetworking.java">Manage Kubernetes clusters with advanced networking</a></li>

<!--li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//containerinstance/samples/ManageContainerInstanceWithAzureFileShareMount.java">Manage Azure Container Instances with new Azure File Share</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//containerinstance/samples/ManageContainerInstanceWithManualAzureFileShareMountCreation.java">Manage Azure Container Instances with an existing Azure File Share</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//containerinstance/samples/ManageContainerInstanceWithMultipleContainerImages.java">Create Container Group with multiple instances and container images</li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//containerinstance/samples/ManageContainerInstanceZeroToOneAndOneToManyUsingContainerServiceOrchestrator.java">Create Container Group and scale up containers using Kubernetes in ACS</li-->
</ul></td>
  </tr>
  <tr>
    <td>Resource Groups</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/ManageResourceGroup.java">Manage resource groups</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/ManageResource.java">Manage resources</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/locks/mgmt-v2016_09_01/src/main/java/com/microsoft/azure/management/locks/v2016_09_01/ManagementLocks.java">Manage resource locks</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/WebServerWithDelegatedCredentials.java">Manage delegated credentials</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/DeployUsingARMTemplate.java">Deploy resources with ARM templates</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/DeployUsingARMTemplateAsync.java">Deploy resources with ARM templates asynchronously</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/DeployUsingARMTemplateWithDeploymentOperations.java">Deploy resources with ARM templates with deployment operations</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/DeployUsingARMTemplateWithProgress.java">Deploy resources with ARM templates (with progress)</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/resources/samples/DeployVirtualMachineUsingARMTemplate.java">Deploy a virtual machine with managed disks using an ARM template</li></ul></td>
  </tr>

  <!--tr>
    <td>Redis Cache</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-context/src/main/java/com/microsoft/azure/spring/cloud/context/core/impl/RedisCacheManager.java">Manage Redis Cache</a></li>
</ul></td>
</tr-->

  <tr>
    <td>Key Vault</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager//keyvault/samples/ManageKeyVault.java">Manage key vaults</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Monitor</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/monitor/samples/QueryMetricsAndActivityLogs.java">Get metrics and activity logs for a resource</a></li>

<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/monitor/samples/SecurityBreachOrRiskActivityLogAlerts.java">Configuring activity log alerts to be triggered on potential security breaches or risks.</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/monitor/samples/WebAppPerformanceMonitoringAlerts.java">Configuring metric alerts to be triggered on potential performance downgrade.</a></li>
<li><a href="https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/monitor/samples/AutoscaleSettingsBasedOnPerformanceOrSchedule.java">Configuring autoscale settings to scale out based on webapp request count statistic.</a></li>
</ul></td>
  </tr>

</table>
