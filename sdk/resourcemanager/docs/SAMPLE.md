# Code snippets and samples

**If you are looking for general documentation on how to use the management libraries, please [visit here](https://aka.ms/azsdk/java/mgmt)**

### Azure Authentication

The `AzureResourceManager` class is the simplest entry point for creating and interacting with Azure resources.

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
AzureResourceManager azure = AzureResourceManager
    .authenticate(credential, profile)
    .withDefaultSubscription();
```

To learn more about authentication in the Azure Management Libraries for Java, see [https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md).

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
<li><a href="https://github.com/Azure-Samples/compute-java-manage-vm">Manage virtual machines</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-vm-async">Manage virtual machines asynchronously</a></li>
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
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machine-with-unmanaged-disks">Manage virtual machine with unmanaged disks</li>
<li><a href="https://github.com/Azure-Samples/aad-java-manage-resources-from-vm-with-msi">Manage Azure resources from a virtual machine with system assigned managed service identity (MSI)</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-vm-from-vm-with-msi-credentials">Manage Azure resources from a virtual machine with managed service identity (MSI) credentials</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-user-assigned-msi-enabled-virtual-machine">Manage Azure resources from a virtual machine with system assigned managed service identity (MSI)</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-vms-in-availability-zones">Manage virtual machines in availability zones</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-vmss-in-availability-zones">Manage virtual machine scale sets in availability zones</a></li>

<li><a href="https://github.com/Azure-Samples/compute-java-list-compute-skus">List compute SKUs</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-managed-disks">Manage virtual machine with managed disks</li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machine-with-disks">Manage virtual machine with disks</li>

</ul></td>
  </tr>
  <tr>
    <td>Virtual Machines - parallel execution</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machines-in-parallel">Create multiple virtual machines in parallel</li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-virtual-machines-with-network-in-parallel">Create multiple virtual machines with network in parallel</li>
<li><a href="https://github.com/Azure-Samples/compute-java-create-virtual-machines-across-regions-in-parallel">Create multiple virtual machines across regions in parallel</li>
<li><a href="https://github.com/Azure-Samples/compute-java-create-vms-async-tracking-related-resources">Create multiple virtual machines in parallel and track related resources</li>
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
</table>

### Networking

#### Create a virtual network

You can create a virtual network by using a `define() … create()` method chain.

```java
Network network = azure.networks().define("mynetwork")
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

<li><a href="https://github.com/Azure-Samples/network-java-manage-virtual-network">Manage virtual network</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-virtual-network-async">Manage virtual network asynchronously</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-network-interface">Manage network interface</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-network-security-group">Manage network security group</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-ip-address">Manage IP address</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-internet-facing-load-balancers">Manage Internet facing load balancers</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-internal-load-balancers">Manage internal load balancers</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-create-simple-internet-facing-load-balancer">Create simple Internet facing load balancer</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-use-new-watcher">Use net watcher</a>
<li><a href="https://github.com/Azure-Samples/network-java-manage-network-peering">Manage network peering between two virtual networks</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-use-network-watcher-to-check-connectivity">Use network watcher to check connectivity between virtual machines in peered networks</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-virtual-network-with-site-to-site-vpn-connection">Manage virtual network with site-to-site VPN connection</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-virtual-network-to-virtual-network-vpn-connection">Manage virtual network to virtual network VPN connection</a></li>
<li><a href="https://github.com/Azure-Samples/network-java-manage-vpn-client-connection">Manage client to virtual network VPN connection</a></li>
</ul>
</td>
  </tr>

  <tr>
    <td>DNS</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/dns-java-host-and-manage-your-domains">Host and manage domains</a></li>
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
    <td>Express Route</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/network-java-manage-express-route">Create and configure Express Route</a></li>
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
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps">Manage Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps-with-custom-domains">Manage Web apps with custom domains</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-web-apps">Configure deployment sources for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-web-apps-async">Configure deployment sources for Web apps asynchronously</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-staging-and-production-slots-for-web-apps">Manage staging and production slots for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-scale-web-apps">Scale Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-storage-connections-for-web-apps">Manage storage connections for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-data-connections-for-web-apps">Manage data connections (such as SQL database and Redis cache) for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-authentication-for-web-apps">Manage authentication for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-access-key-vault-by-msi-for-web-apps">Safegaurd Web app secrets in Key Vault</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-access-key-vault-convenience-for-web-apps">Safegaurd Web app secrets in Key Vault using convenience API</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-logs-for-web-apps">Get logs for Web apps</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Web Apps on <b>Linux</b></td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps-on-linux">Manage Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-deploy-image-from-acr-to-linux">Deploy a container image from Azure Container Registry to Linux containers</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-web-apps-on-linux-with-custom-domains">Manage Web apps with custom domains</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-web-apps-on-linux">Configure deployment sources for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-scale-web-apps-on-linux">Scale Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-storage-connections-for-web-apps-on-linux">Manage storage connections for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-data-connections-for-web-apps-on-linux">Manage data connections (such as SQL database and Redis cache) for Web apps</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-access-key-vault-convenience-for-web-apps-on-linux">Safegaurd Web app secrets in Key Vault on Linux</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Functions</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-functions">Manage functions</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-functions-with-custom-domains">Manage functions with custom domains</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-configure-deployment-sources-for-functions">Configure deployment sources for functions</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-authentication-for-functions">Manage authentication for functions</a></li>
<li><a href="https://github.com/Azure-Samples/app-service-java-manage-logs-for-function-apps">Get function logs</a></li>
</ul></td>
  </tr>
  
  <tr>
    <td>Traffic Manager</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/traffic-manager-java-manage-profiles">Manage traffic manager profiles</a></li>
<li><a href="https://github.com/Azure-Samples/traffic-manager-java-manage-simple-profiles">Manage simple traffic manager profiles</a></li>
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
SqlDatabase database = azure.sqlServers().databases().define("myNewDatabase")
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
<li><a href="https://github.com/Azure-Samples/storage-java-manage-storage-accounts">Manage storage accounts</a></li>
<li><a href="https://github.com/Azure-Samples/storage-java-manage-storage-accounts-async">Manage storage accounts asynchronously</a></li>
<li><a href="https://github.com/Azure-Samples/storage-java-manage-storage-account-network-rules">Manage network rules of a storage account</a></li>
</ul></td>
  </tr>

  <tr>
    <td>SQL Database</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-db">Manage SQL databases</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-sql-dbs-in-elastic-pool">Manage SQL databases in elastic pools</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-firewalls-for-sql-databases">Manage firewalls for SQL databases</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-sql-databases-across-regions">Manage SQL databases across regions</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-import-export-db">Import and export SQL databases</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-recover-restore-db">Restore and recover SQL databases</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-get-sql-metrics">Get SQL Database metrics</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-failover-groups">Manage SQL Database Failover Groups</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-sql-server-dns-aliases">Manage SQL Server DNS aliases</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-sql-secrets-in-key-vault">Manage SQL secrets (Server Keys) in Azure Key Vault</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-virtual-network-rules">Manage SQL Virtual Network Rules</a></li>
<li><a href="https://github.com/Azure-Samples/sql-database-java-manage-sql-server-security-alert-policy">Manage SQL Server Security Alert Policy</a></li>
</ul></td>
  </tr>

  <tr>
    <td>Cosmos DB</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-configure-for-high-availability">Create a CosmosDB and configure it for high availability</a></li>
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-configure-for-eventual-consistency">Create a CosmosDB and configure it with eventual consistency</a></li>
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-configure-firewall">Create a CosmosDB, configure it for high availability and create a firewall to limit access from an approved set of IP addresses</li>
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-and-get-mongodb-connection-string">Create a CosmosDB and get MongoDB connection string</li>
<li><a href="https://github.com/Azure-Samples/cosmosdb-java-create-cosmosdb-table-with-virtual-network-rule">Create a CosmosDB Table with a virtual network rule</li>
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
<li><a href="https://github.com/Azure-Samples/aad-java-manage-service-principal-credentials">Manage credentials for service principals using Java</a></li>
<li><a href="https://github.com/Azure-Samples/aad-java-manage-users-groups-and-roles">Manage users and groups and manage their roles</a></li>
<li><a href="https://github.com/Azure-Samples/compute-java-manage-resources-from-vm-with-msi-in-aad-group">Manage Azure resources from a managed service identity (MSI) enabled virtual machine that belongs to an Azure Active Directory (AAD) security group</a></li>
</ul></td>
  </tr>

<tr>
    <td>Container Service<br>Container Registry and <br>Container Instances</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/acr-java-manage-azure-container-registry">Manage container registry</a></li>
<li><a href="https://github.com/Azure-Samples/acr-java-manage-azure-container-registry-with-webhooks">Manage container registry with Web hooks</a></li>
<li><a href="https://github.com/Azure-Samples/aks-java-manage-kubernetes-cluster">Manage Kubernetes cluster (AKS)</a></li>
<li><a href="https://github.com/Azure-Samples/aks-java-deploy-image-from-acr-to-kubernetes">Deploy an image from container registry to Kubernetes cluster (AKS)</a></li>
<li><a href="https://github.com/Azure-Samples/aks-java-manage-kubernetes-cluster-with-advanced-networking">Manage Kubernetes clusters with advanced networking</a></li>

<li><a href="https://github.com/Azure-Samples/aci-java-manage-container-instances-1">Manage Azure Container Instances with new Azure File Share</li>
<li><a href="https://github.com/Azure-Samples/aci-java-manage-container-instances-2">Manage Azure Container Instances with an existing Azure File Share</li>
<li><a href="https://github.com/Azure-Samples/aci-java-create-container-groups">Create Container Group with multiple instances and container images</li>
<li><a href="https://github.com/Azure-Samples/aci-java-scale-up-containers-using-acs">Create Container Group and scale up containers using Kubernetes in ACS</li>
</ul></td>
  </tr>
  <tr>
    <td>Resource Groups</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/resources-java-manage-resource-group">Manage resource groups</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-manage-resource">Manage resources</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-using-arm-template">Deploy resources using ARM templates</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-using-arm-template-async">Deploy resources using ARM templates asynchronously</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-using-arm-template-with-deployment-operations">Deploy resources using ARM templates with deployment operations</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-using-arm-template-with-progress">Deploy resources using ARM templates with progress</a></li>
<li><a href="https://github.com/Azure-Samples/resources-java-deploy-using-arm-template-with-tags">Deploy resources using ARM templates with tags</a></li>
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
    <td>Monitor</td>
    <td><ul style="list-style-type:circle">
<li><a href="https://github.com/Azure-Samples/monitor-java-query-metrics-activitylogs">Get metrics and activity logs for a resource</a></li>

<li><a href="https://github.com/Azure-Samples/monitor-java-activitylog-alerts-on-security-breach-or-risk">Configuring activity log alerts to be triggered on potential security breaches or risks.</a></li>
<li><a href="https://github.com/Azure-Samples/monitor-java-metric-alerts-on-critical-performance">Configuring metric alerts to be triggered on potential performance downgrade.</a></li>
<li><a href="https://github.com/Azure-Samples/monitor-java-autoscale-based-on-performance">Configuring autoscale settings to scale out based on webapp request count statistic.</a></li>
</ul></td>
  </tr>

  <tr>
    <td>App Platform</td>
    <td><ul style="list-style-type:circle">
    <li><a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager-samples/src/main/java/com/azure/resourcemanager/appplatform/samples/ManageSpringCloud.java">Manage Spring Cloud</a></li>
  </tr>

</table>
