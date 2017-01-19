package com.microsoft.azure.management.compute;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.functions.Func3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;

public class VirtualMachineCustomImageOperationsTest extends ComputeManagementTestBase {
    private static ApplicationTokenCredentials credentials;
    private static RestClient restClient;
    private static Region region = Region.fromName("eastus2euap");   // Special regions for canary deployment 'eastus2euap' and 'centraluseuap'

    private final String rgHoldingVhdBasedImage = ResourceNamer.randomResourceName("rgimg", 15);
    private final String vhdBasedImageName = ResourceNamer.randomResourceName("img", 15);

    @BeforeClass
    public static void setup() throws Exception {
        File credFile = new File("C:\\my.azureauth");
        credentials = ApplicationTokenCredentials.fromFile(credFile);

        AzureEnvironment canary = new AzureEnvironment("https://login.microsoftonline.com/",
                "https://management.core.windows.net/",
                "https://brazilus.management.azure.com/",
                "https://graph.windows.net/");

        restClient = new RestClient.Builder()
                .withBaseUrl(canary, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .build();

        computeManager = ComputeManager
                .authenticate(restClient, credentials.defaultSubscriptionId());
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(credentials.defaultSubscriptionId());
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canCreateImageFromNativeVhd() throws IOException {
        final String rgName = "customimages6512ert";
        final String vmName = "multidiskvm";

        VirtualMachine linuxVM = computeManager
                .virtualMachines()
                .getByGroup(rgName, vmName);
        Assert.assertNotNull("Expected VM not found, create one  \n "
                + "'prepareGeneralizedVmWithImageBasedDataDisk(" + rgName + ", " + vmName + ", " + region + ", computeManager)' \n", linuxVM);
        //
        // prepareGeneralizedVmWithImageBasedDataDisk(rgName, vmName, region, computeManager);
        //
        VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOsDiskSettings
                creatableDisk = computeManager
                .virtualMachineCustomImages()
                .define(vhdBasedImageName)
                .withRegion(region)
                .withNewResourceGroup(rgHoldingVhdBasedImage)
                .withGeneralizedLinuxOsDiskImage()
                .fromVhd(linuxVM.osNativeDiskVhdUri())
                .withOsDiskCaching(linuxVM.osDiskCachingType());
        for (VirtualMachineNativeDataDisk disk : linuxVM.nativeDataDisks()) {
            creatableDisk.defineDataDiskImage(disk.lun())
                    .fromVhd(disk.vhdUri())
                    .withDiskCaching(disk.cachingType())
                    .withDiskSizeInGB(disk.size() + 10) // Resize each data disk image by +10GB
                    .attach();
        }
        VirtualMachineCustomImage customImage = creatableDisk.create();
        Assert.assertNotNull(customImage.id());
        Assert.assertEquals(customImage.name(), vhdBasedImageName);
        Assert.assertFalse(customImage.isCreatedFromVirtualMachine());
        Assert.assertNull(customImage.sourceVirtualMachineId());
        Assert.assertNotNull(customImage.osDiskImage());
        Assert.assertNotNull(customImage.osDiskImage().blobUri());
        Assert.assertEquals(customImage.osDiskImage().caching(), CachingTypes.READ_WRITE);
        Assert.assertEquals((long)customImage.osDiskImage().diskSizeGB(), (long)linuxVM.osDiskSize());
        Assert.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assert.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assert.assertNotNull(customImage.dataDiskImages());
        Assert.assertEquals(customImage.dataDiskImages().size(), linuxVM.nativeDataDisks().size());
        for (ImageDataDisk diskImage : customImage.dataDiskImages().values()) {
            VirtualMachineNativeDataDisk matchedDisk = null;
            for (VirtualMachineNativeDataDisk vmDisk : linuxVM.nativeDataDisks()) {
                if (vmDisk.lun() == diskImage.lun()) {
                    matchedDisk = vmDisk;
                    break;
                }
            }
            Assert.assertNotNull(matchedDisk);
            Assert.assertEquals(matchedDisk.cachingType(), diskImage.caching());
            Assert.assertEquals(matchedDisk.vhdUri(), diskImage.blobUri());
            Assert.assertEquals((long)matchedDisk.size() + 10, (long)diskImage.diskSizeGB());
        }
        VirtualMachineCustomImage image = computeManager
                .virtualMachineCustomImages()
                .getByGroup(rgHoldingVhdBasedImage, vhdBasedImageName);
        Assert.assertNotNull(image);
        PagedList<VirtualMachineCustomImage> images = computeManager
                .virtualMachineCustomImages()
                .listByGroup(rgHoldingVhdBasedImage);
        Assert.assertTrue(images.size() > 0);
    }

    /**
     * Note: This tests fails today with server error 'virtual machine based on custom images
     * can be created only with managed disks'
     * TODO: Confirm with CRP that whether they have any plan to support this.
     */
    @Test
    public void canCreateVmUsingImageFromNativeVhd() {
        VirtualMachineCustomImage image = computeManager
                .virtualMachineCustomImages()
                .getByGroup(rgHoldingVhdBasedImage, vhdBasedImageName);
        Assert.assertNotNull(image);
        checkVirtualMachineCreationFromImage(image, computeManager);
    }

    @Test
    public void canCreateImageByCapturingVM() {
        String vmRgName = "rgb4864716e2";
        String vmName = "vm670214";

        VirtualMachine vm = computeManager.virtualMachines().getByGroup(vmRgName, vmName);
        Assert.assertNotNull("Expected VM not found, create one  \n "
                + "'prepareGeneralizedVmWith2EmptyDataDisks(" + vmRgName + ", " + vmName + ", " + region + ", computeManager)' \n", vm);
        //
        // prepareGeneralizedVmWith2EmptyDataDisks(rgName, vmRgName, region, computeManager);
        //
        final String rgName = ResourceNamer.randomResourceName("rg", 15);
        final String imageName = ResourceNamer.randomResourceName("img", 15);
        VirtualMachineCustomImage customImage = computeManager.virtualMachineCustomImages()
                .define(imageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .fromVirtualMachine(vm.id())
                .create();

        Assert.assertTrue(customImage.name().equalsIgnoreCase(imageName));
        Assert.assertNotNull(customImage.osDiskImage());
        Assert.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assert.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assert.assertNotNull(customImage.dataDiskImages());
        Assert.assertEquals(customImage.dataDiskImages().size(), 2);
        Assert.assertNotNull(customImage.sourceVirtualMachineId());
        Assert.assertTrue(customImage.sourceVirtualMachineId().equalsIgnoreCase(vm.id()));

        for (VirtualMachineNativeDataDisk vmDisk : vm.nativeDataDisks()) {
            Assert.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Assert.assertEquals(diskImage.caching(), vmDisk.cachingType());
            Assert.assertEquals((long) diskImage.diskSizeGB(), vmDisk.size());
            Assert.assertNotNull(diskImage.blobUri());
            diskImage.blobUri().equalsIgnoreCase(vmDisk.vhdUri());
        }

        customImage = computeManager.virtualMachineCustomImages().getByGroup(rgName, imageName);
        Assert.assertNotNull(customImage);
        Assert.assertNotNull(customImage.inner());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
    }

    @Test
    public void canCreateImageFromManagedDisk() {
        resourceManager.resourceGroups().deleteByName("rgeaa84048c8");

        String vmRgName = "rgb4864716e2";
        String vmName = "vm670214";

        VirtualMachine vm = computeManager.virtualMachines().getByGroup(vmRgName, vmName);
        Assert.assertNotNull("Expected VM not found, create one  \n "
                + "'prepareGeneralizedVmWith2EmptyDataDisks(" + vmRgName + ", " + vmName + ", " + region + ", computeManager)' \n", vm);

        final String rgName = ResourceNamer.randomResourceName("rg", 15);
        final String osDiskName = ResourceNamer.randomResourceName("dsk", 15);

        // Create managed disk with Os from vm's Os disk
        //
        Disk managedOsDisk = computeManager.disks().define(osDiskName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withOs()
                .importedFromGeneralizedOsVhd(vm.osNativeDiskVhdUri(), OperatingSystemTypes.LINUX)
                .create();

        // Create managed disk with Data from vm's lun0 data disk
        //
        final String dataDiskName1 = ResourceNamer.randomResourceName("dsk", 15);
        VirtualMachineNativeDataDisk vmNativeDataDisk1 = vm.nativeDataDisks().get(0);
        Disk managedDataDisk1 = computeManager.disks().define(dataDiskName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withData()
                .importedFromDataVhd(vmNativeDataDisk1.vhdUri())
                .create();

        // Create managed disk with Data from vm's lun1 data disk
        //
        final String dataDiskName2 = ResourceNamer.randomResourceName("dsk", 15);
        VirtualMachineNativeDataDisk vmNativeDataDisk2 = vm.nativeDataDisks().get(1);
        Disk managedDataDisk2 = computeManager.disks().define(dataDiskName2)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withData()
                .importedFromDataVhd(vmNativeDataDisk2.vhdUri())
                .create();

        // Create an image from the above managed disks
        //
        final String imageName = ResourceNamer.randomResourceName("img", 15);
        VirtualMachineCustomImage customImage = computeManager.virtualMachineCustomImages().define(imageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withGeneralizedLinuxOsDiskImage()
                .fromManagedDisk(managedOsDisk)
                .defineDataDiskImage(vmNativeDataDisk1.lun())
                .fromManagedDisk(managedDataDisk1)
                .withDiskCaching(vmNativeDataDisk1.cachingType())
                .withDiskSizeInGB(vmNativeDataDisk1.size() + 10)
                .attach()
                .defineDataDiskImage(vmNativeDataDisk2.lun())
                .fromManagedDisk(managedDataDisk2)
                .withDiskSizeInGB(vmNativeDataDisk2.size() + 10)
                .attach()
                .create();

        Assert.assertNotNull(customImage);
        Assert.assertTrue(customImage.name().equalsIgnoreCase(imageName));
        Assert.assertNotNull(customImage.osDiskImage());
        Assert.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assert.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assert.assertNotNull(customImage.dataDiskImages());
        Assert.assertEquals(customImage.dataDiskImages().size(), 2);
        Assert.assertNull(customImage.sourceVirtualMachineId());

        Assert.assertTrue(customImage.dataDiskImages().containsKey(vmNativeDataDisk1.lun()));
        Assert.assertEquals(customImage.dataDiskImages().get(vmNativeDataDisk1.lun()).caching(), vmNativeDataDisk1.cachingType());
        Assert.assertTrue(customImage.dataDiskImages().containsKey(vmNativeDataDisk2.lun()));
        Assert.assertEquals(customImage.dataDiskImages().get(vmNativeDataDisk2.lun()).caching(), CachingTypes.NONE);

        for (VirtualMachineNativeDataDisk vmDisk : vm.nativeDataDisks()) {
            Assert.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Assert.assertEquals((long) diskImage.diskSizeGB(), vmDisk.size() + 10);
            Assert.assertNull(diskImage.blobUri());
            Assert.assertNotNull(diskImage.managedDisk());
            Assert.assertTrue(diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk1.id())
                    || diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk2.id()));
        }
        computeManager.disks().deleteById(managedOsDisk.id());
        computeManager.disks().deleteById(managedDataDisk1.id());
        computeManager.disks().deleteById(managedDataDisk2.id());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
        computeManager.resourceManager().resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateImageFromSnapshot() {
    }

    public void runCommandOverSSh(String publicIp, String uname, String password, String command) {
        Func3<Session, String, String, String> runCommand = new Func3<Session, String, String, String>() {
            @Override
            public String call(com.jcraft.jsch.Session session, String command, String password) {
                StringBuilder outputBuffer = new StringBuilder();
                try {
                    ChannelExec channel = (ChannelExec) session.openChannel("exec");
                    channel.setCommand("sudo -S -p '' " + command);
                    InputStream commandOutput = channel.getInputStream();
                    channel.connect();

                    OutputStream out = channel.getOutputStream();
                    out.write((password +"\n y \n").getBytes());
                    out.flush();

                    byte[] buffer = new byte[1024];
                    while(true) {
                        while(commandOutput.available() > 0) {
                            int bytesRead = commandOutput.read(buffer, 0, 1024);
                            if (bytesRead < 0) {
                                break;
                            }
                            outputBuffer.append(new String(buffer, 0, bytesRead));
                        }
                        if(channel.isClosed()) {
                            if (commandOutput.available() > 0) {
                                continue;
                            }
                            System.out.println(String.format("Command '%s' exit status code %d:", command, channel.getExitStatus()));
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException ee){ }
                    }
                    channel.disconnect();
                } catch(IOException ioException) {
                    throw new RuntimeException(ioException.getMessage());
                } catch(JSchException jschException) {
                    throw new RuntimeException(jschException.getMessage());
                }
                return outputBuffer.toString();
            }
        };


        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession(uname, publicIp, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            String output = runCommand.call(session, command, password);
            System.out.println(output);
        } catch (Exception e) {
            Assert.fail("SSH connection failed" + e.getMessage());
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private void checkVirtualMachineCreationFromImage(VirtualMachineCustomImage image, ComputeManager computeManager) {
        final String rgName = ResourceNamer.randomResourceName("custvm-", 20);
        final String publicIpDnsLabel = ResourceNamer.randomResourceName("pip", 20);
        VirtualMachine.DefinitionStages.WithManagedCreate creatableVm = computeManager.virtualMachines()
                .define("custimgvm1")
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withNewPrimaryPublicIpAddress(publicIpDnsLabel)
                .withLinuxCustomImage(image.id())
                .withRootUsername("javauser")
                .withRootPassword("12NewPA$$w0rd!");

        // since creating from custom image does not require data disk corresponds to data disk image,
        // below code is not required.
        //
        // HashSet<Integer> diskImageLuns = new HashSet<>();
        // for (ImageDataDisk diskImage : image.dataDiskImages().values()) {
        //     diskImageLuns.add(diskImage.lun());
        //     creatableVm.withNewDataDisk(diskImage.sizeInGb(), diskImage.lun(), diskImage.cachingType())
        // }

        creatableVm
        .withSize(VirtualMachineSizeTypes.STANDARD_DS2)
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 17))
                .withOsDiskCaching(CachingTypes.READ_WRITE);
        creatableVm.create();
    }

    /**
     * Creates a generalized virtual machine that has data disks based on disk image(s) and
     * two blank vhds.
     *
     * @param rgName resource group name
     * @param vmName virtual machine name
     * @param region the resource region
     * @param computeManager the client
     */
    private void prepareGeneralizedVmWithImageBasedDataDisk(String rgName,
                                                            String vmName,
                                                            Region region,
                                                            ComputeManager computeManager) {
        final String uname = "javauser";
        final String password = "12NewPA$$w0rd!";

        VirtualMachineImage linuxVmImage = null;
        PagedList<VirtualMachineImage> vmImages = computeManager
                .virtualMachineImages()
                .listByRegion(region);
        for (VirtualMachineImage vmImage : vmImages) {
            if (vmImage.osDiskImage().operatingSystem() == OperatingSystemTypes.LINUX) {
                if (vmImage.dataDiskImages().size() > 0) {
                    linuxVmImage = vmImage;
                    break;
                }
            }
        }
//        linuxVmImage = computeManager.virtualMachineImages().getImage(region,
//                "alienvault",
//                "unified-security-management-anywhere",
//                "unified-security-management-anywhere",
//                "3.2.0");

        Assert.assertNotNull("A linux image with multi disk not found",
                linuxVmImage);
        final String publicIpDnsLabel = ResourceNamer.randomResourceName("pip", 20);
        VirtualMachine.DefinitionStages.WithNativeCreate creatableVm = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withNewPrimaryPublicIpAddress(publicIpDnsLabel)
                .withSpecificLinuxImageVersion(linuxVmImage.imageReference())
                .withRootUsername(uname)
                .withRootPassword(password)
                .withNativeDisks();
        if (linuxVmImage.plan() != null) {
            creatableVm.withPlan(linuxVmImage.plan());
        }
        // Specifies the data disk based on disk image
        //
        HashSet<Integer> diskImageLuns = new HashSet<>();
        for (DataDiskImage diskImage : linuxVmImage.dataDiskImages().values()) {
            diskImageLuns.add(diskImage.lun());
            creatableVm.defineNativeDataDisk("data-disk-" + diskImage.lun())
                    .fromImage(diskImage.lun())
                    .attach();
        }
        // Add two new empty data disks
        //
        int nextLun = Collections.max(diskImageLuns) + 1;
        creatableVm.defineNativeDataDisk("data-disk-" + nextLun)
                .withNewVhd(30)
                .withLun(nextLun)
                .withCaching(CachingTypes.READ_WRITE)
                .attach();
        nextLun++;
        creatableVm.defineNativeDataDisk("data-disk-" + nextLun)
                .withNewVhd(30)
                .withLun(nextLun)
                .withCaching(CachingTypes.READ_ONLY)
                .attach();

        creatableVm.withSize(VirtualMachineSizeTypes.STANDARD_DS2)
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 17))
                .withOsDiskCaching(CachingTypes.READ_WRITE);

        // Create the virtual Machine
        VirtualMachine linuxVM = creatableVm.create();
        //
        System.out.println("SSH into the VM [" + uname + "@" + linuxVM.getPrimaryPublicIpAddress().fqdn() + "]");
        System.out.println("with password: " + password);
        System.out.println("and run 'sudo waagent -deprovision+user' to prepare it for capturing");
        System.out.println("after that press 'Enter' to continue.");
        try {
            System.in.read();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
        linuxVM.deallocate();
        linuxVM.generalize();
    }

    private void prepareGeneralizedVmWith2EmptyDataDisks(String rgName,
                                                         String vmName,
                                                         Region region,
                                                         ComputeManager computeManager) {
        final String uname = "javauser";
        final String password = "12NewPA$$w0rd!";

        ImageReference imageReference = new ImageReference();
        imageReference.withPublisher("Canonical")
                .withOffer("UbuntuServer")
                .withSku("14.04.2-LTS")
                .withVersion("14.04.201507060");
        final String publicIpDnsLabel = ResourceNamer.randomResourceName("pip", 20);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withNewPrimaryPublicIpAddress(publicIpDnsLabel)
                .withSpecificLinuxImageVersion(imageReference)
                .withRootUsername(uname)
                .withRootPassword(password)
                .withNativeDisks()
                .defineNativeDataDisk("disk-1")
                    .withNewVhd(30)
                    .withCaching(CachingTypes.READ_WRITE)
                    .attach()
                .defineNativeDataDisk("disk-2")
                    .withNewVhd(60)
                    .withCaching(CachingTypes.READ_ONLY)
                    .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 17))
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .create();
        //
        System.out.println("SSH into the VM [" + uname + "@" + virtualMachine.getPrimaryPublicIpAddress().fqdn() + "]");
        System.out.println("with password: " + password);
        System.out.println("and run 'sudo waagent -deprovision+user' to prepare it for capturing");
        System.out.println("after that press 'Enter' to continue.");
        try {
            System.in.read();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
         virtualMachine.deallocate();
         virtualMachine.generalize();
    }
}