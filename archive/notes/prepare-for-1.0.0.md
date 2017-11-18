# Prepare for Azure Management Libraries for Java 1.0.0 #

Steps to migrate code that uses Azure Management Libraries for Java from beta 5 to 1.0.0 …

> If this note missed any breaking changes, please open a pull request.

## App service plan adds new required parameter: operating system

To create an `AppServicePlan` in beta5:

```java
appServiceManager.appServicePlans().define(APP_SERVICE_PLAN_NAME)
    .withRegion(Region.US_WEST)
    .withNewResourceGroup(RG_NAME)
    .withPricingTier(PricingTier.PREMIUM_P1)
    .create();
```

To create an `AppServicePlan` in 1.0.0:

```java
appServiceManager.appServicePlans().define(APP_SERVICE_PLAN_NAME)
    .withRegion(Region.US_WEST)
    .withNewResourceGroup(RG_NAME)
    .withPricingTier(PricingTier.PREMIUM_P1)
    .withOperatingSystem(OperatingSystem.WINDOWS)
    .create();
```

## Parameters for `WebApp` creation are re-ordered

In beta5, we create a `WebApp` with a new plan as following:

```java
azure.webApps().define(app1Name)
    .withNewResourceGroup(rg1Name)
    .withNewAppServicePlan(planName)
    .withRegion(Region.US_WEST)
    .withPricingTier(AppServicePricingTier.STANDARD_S1)
    .create();
```

or with an existing plan as following:

```java
azure.webApps().define(app2Name)
    .withExistingResourceGroup(rg1Name)
    .withExistingAppServicePlan(plan)
    .create();
```

In 1.0, there are a few breaking changes:

- region is the first required parameter for a new app service plan
- the app service plan is the first required parameter for an existing app service plan
- the app service plan parameter doesn't require a name (if its name is important, define an app service plan separately in its own `define()` flow)
- `withNewAppServicePlan()` is separated into `withNewWindowsPlan()` and `withNewLinuxPlan()` depending on the operating system of the plan. Same applies for `withExistingAppServicePlan()`.

To create one with a new app service plan

```java
WebApp app1 = azure.webApps()
    .define(app1Name)
    .withRegion(Region.US_WEST)
    .withNewResourceGroup(rg1Name)
    .withNewWindowsPlan(PricingTier.STANDARD_S1)
    .create();
```

To create one with an existing app service plan

```java
azure.webApps()
    .define(app2Name)
    .withExistingWindowsPlan(plan)
    .withExistingResourceGroup(rg1Name)
    .create();
```

## Change Method or Interface Names ##

<table>
  <tr>
    <th>From</th>
    <th>To</th>
    <th>Ref</th>
  </tr>
  <tr>
      <td><code>VirtualMachine.extensions()</code></td>
      <td><code>VirtualMachine.getExtensions()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1466">#1466</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskCaching()</code></td>
    <td><code>VirtualMachine.withOSDiskCaching()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskSizeInGB()</code></td>
    <td><code>VirtualMachine.withOSDiskSizeInGB()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withSpecializedOsUnmanagedDisk()</code></td>
    <td><code>VirtualMachine.withSpecializedOSUnmanagedDisk()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskName()</code></td>
    <td><code>VirtualMachine.withOSDiskName()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskEncryptionSettings()</code></td>
    <td><code>VirtualMachine.withOSDiskEncryptionSettings()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskStorageAccountType()</code></td>
    <td><code>VirtualMachine.withOSDiskStorageAccountType()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskVhdLocation()</code></td>
    <td><code>VirtualMachine.withOSDiskVhdLocation()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withoutVmAgent()</code></td>
    <td><code>VirtualMachine.withoutVMAgent()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1549">#1549</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withWinRm()</code></td>
    <td><code>VirtualMachine.withWinRM()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1549">#1549</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withWinRm()</code></td>
    <td><code>VirtualMachineScaleSet.withWinRM()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1549">#1549</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withoutVmAgent()</code></td>
    <td><code>VirtualMachineScaleSet.withoutVMAgent()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1549">#1549</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withVmAgent()</code></td>
    <td><code>VirtualMachineScaleSet.withVMAgent()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1549">#1549</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withOsDiskStorageAccountType()</code></td>
    <td><code>VirtualMachineScaleSet.withOSDiskStorageAccountType()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withOsDiskCaching()</code></td>
    <td><code>VirtualMachineScaleSet.withOSDiskCaching()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withOsDiskName()</code></td>
    <td><code>VirtualMachineScaleSet.withOSDiskName()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1598">#1598</a></td>
  </tr>
  <tr>
    <td>All <code>*ByGroup*</code> in names</td>
    <td>All <code>*ByResourceGroup*</code> in names</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1566">#1566</a></td>
  </tr>
  <tr>
    <td>Enumeration <code>CountryISOCode</code></td>
    <td>Enumeration <code>CountryIsoCode</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1558">#1558</a></td>
  </tr>
  <tr>
    <td>Interface <code>Wrapper&lt;T&gt;</code></td>
    <td>Interface <code>HasInner&lt;T&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1439">#1439</a></td>
  </tr>
  <tr>
    <td>Any types or methods <code>*Ip*</code> in names</td>
    <td>Any types or methods <code>*IP*</code> in names</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1439">#1439</a></td>
  </tr>
  <tr>
    <td>Any types or methods <code>*Mx*</code> in names</td>
    <td>Any types or methods <code>*MX*</code> in names</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1439">#1439</a></td>
  </tr>
  <tr>
    <td>Any types or methods <code>*Ns*</code> in names</td>
    <td>Any types or methods <code>*NS*</code> in names</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1439">#1439</a></td>
  </tr>
  <tr>
    <td>Any types or methods <code>*Vm*</code> in names</td>
    <td>Any types or methods <code>*VM*</code> in names</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1439">#1439</a></td>
  </tr>
</table>



## Change Receiving Variable Type ##

<table>
  <tr>
    <th>From</th>
    <th>To</th>
    <th>For Method</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td><code>Observable&lt;Void&gt;</code></td>
    <td><code>Completable</code></td>
    <td><code>SupportDeletingByName.deleteByNameAsync()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1388">#1388</a></td>
  </tr>
  <tr>
    <td><code>Observable&lt;Void&gt;</code></td>
    <td><code>Completable</code></td>
    <td><code>SupportDeletingById.deleteByIdAsync()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1388">#1388</a></td>
  </tr>
  <tr>
    <td><code>Observable&lt;Void&gt;</code></td>
    <td><code>Completable</code></td>
    <td><code>SupportDeletingByGroup.deleteByGroupAsync()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1388">#1388</a></td>
  </tr>
    <tr>
    <td><code>List&lt;DataDiskImage&gt;</code></td>
    <td><code>Map&lt;Integer, DataDiskImage&gt;</code></td>
    <td><code>VirtualMachineImage.dataDiskImages()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
    <tr>
    <td><code>List&lt;ResourceT&gt;</code></td>
    <td><code>Map&lt;String, ResourceT&gt;</code></td>
    <td><code>{ResourceCollection}.create(List&lt;Creatable&lt;ResourceT&gt;&gt;)</code><br/><br/>
    <code>e.g. 
        VirtualMachines.create(List&lt;Creatable&lt;VirtualMachine&gt;&gt;) </code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1381">#1381</a></td>
  </tr>

  <tr>
    <td><code>List&lt;String&gt;></code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><code>AvailabilitySet.virtualMachineIds()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1558">1558</a></td>
  </tr>
  <tr>
    <td><code>List&lt;String&gt;></code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><code>NetworkSecurityGroup.networkInterfaceIds()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1439">1439</a></td>
  </tr>

</table>


## Drop Method Usage or Use Alternate ##

<table>
  <tr>
    <th>Drop Method</th>
    <th>Use Alternate</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td><code>StorageAccount.withoutCustomDomain()</code></td>
    <td></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1600/commits/58eb1a5326e2491f90e04c27d8b4e4aaef1caa87">58eb1a5</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withDataDiskUpdated()</code></td>
    <td>Disks can not longer be updated as part of virtual machine update. They should be updated directly via Disks API.</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1579">1579</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withDataDiskUpdated()</code></td>
    <td>Disks can not longer be updated as part of virtual machine scale set update. They should be updated directly via Disks API.</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1579">1579</a></td>
  </tr>
</table>

