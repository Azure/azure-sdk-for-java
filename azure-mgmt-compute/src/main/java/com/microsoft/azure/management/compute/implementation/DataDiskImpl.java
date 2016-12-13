package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.VirtualHardDisk;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The implementation for {@link DataDisk} and its create and update interfaces.
 */
@LangDefinition
class DataDiskImpl
    extends ChildResourceImpl<DataDisk, VirtualMachineImpl, VirtualMachine>
    implements
        VirtualMachineDataDisk,
        VirtualMachineDataDisk.Definition<VirtualMachine.DefinitionStages.WithCreate>,
        VirtualMachineDataDisk.UpdateDefinition<VirtualMachine.Update>,
        VirtualMachineDataDisk.Update {

    protected DataDiskImpl(DataDisk inner, VirtualMachineImpl parent) {
        super(inner, parent);
    }

    protected static DataDiskImpl prepareDataDisk(String name, DiskCreateOptionTypes createOption, VirtualMachineImpl parent) {
        DataDisk dataDiskInner = new DataDisk();
        dataDiskInner.withLun(-1);
        dataDiskInner.withName(name);
        dataDiskInner.withCreateOption(createOption);
        dataDiskInner.withVhd(null);
        return new DataDiskImpl(dataDiskInner, parent);
    }

    protected static DataDiskImpl createNewDataDisk(int sizeInGB, VirtualMachineImpl parent) {
        DataDiskImpl dataDiskImpl = prepareDataDisk(null, DiskCreateOptionTypes.EMPTY, parent);
        dataDiskImpl.inner().withDiskSizeGB(sizeInGB);
        return dataDiskImpl;
    }

    protected static DataDiskImpl createFromExistingDisk(String storageAccountName,
                                                         String containerName,
                                                         String vhdName,
                                                         VirtualMachineImpl parent) {
        DataDiskImpl dataDiskImpl = prepareDataDisk(null, DiskCreateOptionTypes.ATTACH, parent);
        VirtualHardDisk diskVhd = new VirtualHardDisk();
        diskVhd.withUri(blobUrl(storageAccountName, containerName, vhdName));
        dataDiskImpl.inner().withVhd(diskVhd);
        return dataDiskImpl;
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public int size() {
        return Utils.toPrimitiveInt(this.inner().diskSizeGB());
    }

    @Override
    public int lun() {
        return Utils.toPrimitiveInt(this.inner().lun());
    }

    @Override
    public String vhdUri() {
        return this.inner().vhd().uri();
    }

    @Override
    public CachingTypes cachingType() {
        return this.inner().caching();
    }

    @Override
    public String sourceImageUri() {
        if (this.inner().image() != null) {
            return this.inner().image().uri();
        }
        return null;
    }

    @Override
    public DiskCreateOptionTypes creationMethod() {
        return this.inner().createOption();
    }

    // Fluent setters

    @Override
    public DataDiskImpl from(String storageAccountName, String containerName, String vhdName) {
        this.inner().withVhd(new VirtualHardDisk());
        //URL points to an existing data disk to be attached
        this.inner().vhd().withUri(blobUrl(storageAccountName, containerName, vhdName));
        return this;
    }

    @Override
    public DataDiskImpl withSizeInGB(Integer sizeInGB) {
        // Note: Size can be specified only while attaching new blank disk.
        // Size cannot be specified while attaching an existing disk.
        // Once attached both type of data disk can be resized via VM update.
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public DataDiskImpl storeAt(String storageAccountName, String containerName, String vhdName) {
        this.inner().withVhd(new VirtualHardDisk());
        // URL points to where the new data disk needs to be stored
        this.inner().vhd().withUri(blobUrl(storageAccountName, containerName, vhdName));
        return this;
    }

    @Override
    public DataDiskImpl withLun(Integer lun) {
        this.inner().withLun(lun);
        return this;
    }

    @Override
    public DataDiskImpl withCaching(CachingTypes cachingType) {
        this.inner().withCaching(cachingType);
        return this;
    }

    // Verbs

    @Override
    public VirtualMachineImpl attach() {
        return this.parent().withDataDisk(this);
    }

    protected static void setDataDisksDefaults(List<VirtualMachineDataDisk> dataDisks, String namePrefix) {
        List<Integer> usedLuns = new ArrayList<>();
        for (VirtualMachineDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() != -1) {
                usedLuns.add(dataDisk.lun());
            }
        }

        for (VirtualMachineDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() == -1) {
                Integer i = 0;
                while (usedLuns.contains(i)) {
                    i++;
                }
                dataDisk.inner().withLun(i);
                usedLuns.add(i);
            }

            if (dataDisk.name() == null) {
                dataDisk.inner().withName(namePrefix + "-data-disk-" + dataDisk.lun());
            }

            if (dataDisk.inner().caching() == null) {
                dataDisk.inner().withCaching(CachingTypes.READ_WRITE);
            }
        }
    }

    protected static void ensureDisksVhdUri(List<VirtualMachineDataDisk> dataDisks, StorageAccount storageAccount, String namePrefix) {
        for (VirtualMachineDataDisk dataDisk : dataDisks) {
            if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY) {
                //New data disk requires Vhd Uri to be set
                if (dataDisk.inner().vhd() == null) {
                    dataDisk.inner().withVhd(new VirtualHardDisk());
                    dataDisk.inner().vhd().withUri(storageAccount.endPoints().primary().blob()
                            + "vhds/"
                            + namePrefix + "-data-disk-" + dataDisk.lun() + "-" + UUID.randomUUID().toString() + ".vhd");
                }
            }
        }
    }

    protected static void ensureDisksVhdUri(List<VirtualMachineDataDisk> dataDisks, String namePrefix) {
        String containerUrl = null;
        for (VirtualMachineDataDisk dataDisk : dataDisks) {
            if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY && dataDisk.inner().vhd() != null) {
                int idx = dataDisk.inner().vhd().uri().lastIndexOf('/');
                containerUrl = dataDisk.inner().vhd().uri().substring(0, idx);
                break;
            }
        }
        if (containerUrl != null) {
            for (VirtualMachineDataDisk dataDisk : dataDisks) {
                if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY) {
                    //New data disk requires Vhd Uri to be set
                    if (dataDisk.inner().vhd() == null) {
                        dataDisk.inner().withVhd(new VirtualHardDisk());
                        dataDisk.inner().vhd().withUri(containerUrl
                                + namePrefix + "-data-disk-" + dataDisk.lun() + "-" + UUID.randomUUID().toString() + ".vhd");
                    }
                }
            }
        }
    }

    private static String blobUrl(String storageAccountName, String containerName, String blobName) {
        // Future: Get the storage domain from the environment
        return  "https://" + storageAccountName + ".blob.core.windows.net" + "/" + containerName + "/" + blobName;
    }
}
