# Prepare for Azure Management Libraries for Java 1.0.0-beta5#

Steps to migrate code that uses Azure Management Libraries for Java from beta 4 to beta 5 …

> If this note missed any breaking changes, please open a pull request.

# `Create()` defaults to Managed Disks#

In `VirtualMachine, VirtualMachineScaleSet` and `VirtualMachineScaleSetVM` the OS and data disks getters and withers **default** to managed disks.

The withers and getters for storage account based (unmanaged) OS and data disks are **renamed** to include the term `unmanaged`.

## `Create()` creates unmanaged disks on explicit requests##
Starting in 1.0.0-beta5, if you like to continue to use the storage account based (unmanaged) operating system and data disks, you may use `withUnmanagedDisks()` in the `define() ... create()` method chain. 

The following sample statement creates a virtual machine with an unmanaged operating system disk:
    
    azure.virtualMachines().define("myLinuxVM")
       .withRegion(Region.US_EAST)
       .withNewResourceGroup(rgName)
       .withNewPrimaryNetwork("10.0.0.0/28")
       .withPrimaryPrivateIpAddressDynamic()
       .withNewPrimaryPublicIpAddress("mylinuxvmdns")
       .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
       .withRootUsername("tirekicker")
       .withSsh(sshKey)
       // Unmanaged disks - uses Storage Account
       .withUnmanagedDisks()
       .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
       .create();

For additional sample code, please see <a href="https://github.com/azure-samples/compute-java-manage-virtual-machine-with-unmanaged-disks">Manage Virtual Machine With Unmanaged Disks</a> ready-to-run sample. 

## Converting virtual machines with storage account based disks to use managed disks
 ##
You can convert a virtual machine with unmanaged disks (Storage Account based) to managed disks with a single reboot.

    PagedList<VirtualMachine> virtualMachines = azure.virtualMachines().list();
    for (VirtualMachine virtualMachine : virtualMachines) {
        if (!virtualMachine.isManagedDiskEnabled()) {
            virtualMachine.deallocate();
            virtualMachine.convertToManaged();
        }
    }


# Change Method Names #

<table>
  <tr>
    <th>From</th>
    <th>To</th>
    <th>Ref</th>
  </tr>
    <tr>
    <td><code>VirtualMachineScaleSetVM.isOsBasedOnPlatformImage()</code></td>
    <td><code>VirtualMachineScaleSetVM.isOSBasedOnPlatformImage()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSetVM.getPlatformImage()</code></td>
    <td><code>VirtualMachineScaleSetVM.getOSPlatformImage()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSetVM.customImageVhdUri()</code></td>
    <td><code>VirtualMachineScaleSetVM.storedImageUnmanagedVhdUri()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSetVM.osDiskVhdUri()</code></td>
    <td><code>VirtualMachineScaleSetVM.osUnmanagedDiskVhdUri()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDisk()</code></td>
    <td><code>VirtualMachine.withSpecializedOsUnmanagedDisk()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskCaching()</code></td>
    <td><code>VirtualMachine.withOSDiskCaching()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withOsDiskSizeInGb()</code></td>
    <td><code>VirtualMachine.withOSDiskSizeInGB()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.osDiskVhdUri()</code></td>
    <td><code>VirtualMachine.osUnmanagedDiskVhdUri()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
    <tr>
    <td><code>VirtualMachine.getPlatformImage()</code></td>
    <td><code>VirtualMachine.getOSPlatformImage()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.defineNewDataDisk()</code></td>
    <td><code>VirtualMachine.defineUnmanagedDataDisk()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withNewDataDisk()</code></td>
    <td><code>VirtualMachine.withNewUnmanagedDataDisk()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withExistingDataDisk()</code></td>
    <td><code>VirtualMachine.withExistingUnmanagedDataDisk()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withoutDataDisk()</code></td>
    <td><code>VirtualMachine.withoutUnmanagedDataDisk()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.updateDataDisk()</code></td>
    <td><code>VirtualMachine.updateUnmanagedDataDisk()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.dataDisks()</code></td>
    <td><code>VirtualMachine.unmanagedDataDisks()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1409">#1409</a></td>
  </tr>
  <tr>
    <td><code>Subscriptions.getByName()</code></td>
    <td><code>Subscriptions..getById()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1396">#1396</a></td>
  </tr>
</table>



# Change Receiving Variable Type #

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
    <td><code> List&lt;DataDiskImage&gt;</code></td>
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
</table>

# Change visibility #

<table>
  <tr>
    <th>Name</th>
    <th>Visibility</th>
    <th>Note</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td><code>setInner(T)</code></td>
    <td><code>internal</code></td>
    <td>we already documented setInner as "internal use only", making it really internal</td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1381">#1381</a></td>
  </tr>
</table>