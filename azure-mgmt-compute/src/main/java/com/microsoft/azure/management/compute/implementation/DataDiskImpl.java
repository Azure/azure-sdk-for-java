package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.CachingTypes;
import com.microsoft.azure.management.compute.implementation.api.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.implementation.api.VirtualHardDisk;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents data disk of a virtual machine.
 */
class DataDiskImpl
    extends ChildResourceImpl<com.microsoft.azure.management.compute.implementation.api.DataDisk, VirtualMachine>
    implements DataDisk,
    DataDisk.Definitions {
    // flag indicating whether data disk in create or update mode
    private final boolean isInCreateMode; // Future: will be used when we support update

    protected DataDiskImpl(String name,
                           com.microsoft.azure.management.compute.implementation.api.DataDisk dataDiskInner,
                           VirtualMachineImpl virtualMachine,
                           final boolean isInCreateMode) {
        super(name, dataDiskInner, virtualMachine);
        this.isInCreateMode = isInCreateMode;
    }

    protected static DataDiskImpl prepareDataDisk(String name, DiskCreateOptionTypes createOption, VirtualMachineImpl parent) {
        com.microsoft.azure.management.compute.implementation.api.DataDisk dataDiskInner
                = new com.microsoft.azure.management.compute.implementation.api.DataDisk();
        dataDiskInner.setLun(-1);
        dataDiskInner.setName(name);
        dataDiskInner.setCreateOption(createOption);
        dataDiskInner.setVhd(null);
        parent.inner().storageProfile().dataDisks().add(dataDiskInner);
        return new DataDiskImpl(name, dataDiskInner, parent, true);
    }

    protected static DataDiskImpl createNewDataDisk(int sizeInGB, VirtualMachineImpl parent) {
        DataDiskImpl dataDiskImpl = prepareDataDisk(null, DiskCreateOptionTypes.EMPTY, parent);
        dataDiskImpl.inner().setDiskSizeGB(sizeInGB);
        return dataDiskImpl;
    }

    protected static DataDiskImpl createFromExistingDisk(String storageAccountName,
                                                         String containerName,
                                                         String vhdName,
                                                         VirtualMachineImpl parent) {
        DataDiskImpl dataDiskImpl = prepareDataDisk(null, DiskCreateOptionTypes.ATTACH, parent);
        VirtualHardDisk diskVhd = new VirtualHardDisk();
        diskVhd.setUri(blobUrl(storageAccountName, containerName, vhdName));
        dataDiskImpl.inner().setVhd(diskVhd);
        return dataDiskImpl;
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public int size() {
        return this.inner().diskSizeGB();
    }

    @Override
    public int lun() {
        return this.inner().lun();
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
    public DiskCreateOptionTypes createOption() {
        return this.inner().createOption();
    }

    @Override
    public DataDiskImpl from(String storageAccountName, String containerName, String vhdName) {
        this.inner().setVhd(new VirtualHardDisk());
        //URL points to an existing data disk to be attached
        this.inner().vhd().setUri(blobUrl(storageAccountName, containerName, vhdName));
        return this;
    }

    @Override
    public DataDiskImpl withSizeInGB(Integer sizeInGB) {
        this.inner().setDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public DataDiskImpl storeAt(String storageAccountName, String containerName, String vhdName) {
        this.inner().setVhd(new VirtualHardDisk());
        // URL points to where the new data disk needs to be stored
        this.inner().vhd().setUri(blobUrl(storageAccountName, containerName, vhdName));
        return this;
    }

    @Override
    public DataDiskImpl withLun(Integer lun) {
        this.inner().setLun(lun);
        return this;
    }

    @Override
    public DataDiskImpl withCaching(CachingTypes cachingType) {
        this.inner().setCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachine attach() {
        return this.parent();
    }

    protected static void setDataDisksDefaults(List<DataDisk> dataDisks, String namePrefix) {
        List<Integer> usedLuns = new ArrayList<>();
        for (DataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() != -1) {
                usedLuns.add(dataDisk.lun());
            }
        }

        for (DataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() == -1) {
                Integer i = 0;
                while (usedLuns.contains(i)) {
                    i++;
                }
                dataDisk.inner().setLun(i);
                usedLuns.add(i);
            }

            if (dataDisk.name() == null) {
                dataDisk.inner().setName(namePrefix + "-data-disk-" + dataDisk.lun());
            }

            if (dataDisk.inner().caching() == null) {
                dataDisk.inner().setCaching(CachingTypes.READ_WRITE);
            }
        }
    }

    protected static void ensureDisksVhdUri(List<DataDisk> dataDisks, StorageAccount storageAccount, String namePrefix) {
        for (DataDisk dataDisk : dataDisks) {
            if (dataDisk.createOption() == DiskCreateOptionTypes.EMPTY) {
                //New data disk requires Vhd Uri to be set
                if (dataDisk.inner().vhd() == null) {
                    dataDisk.inner().setVhd(new VirtualHardDisk());
                    dataDisk.inner().vhd().setUri(storageAccount.endPoints().primary().blob()
                            + "/vhds/"
                            + namePrefix + "-data-disk-" + dataDisk.lun() + "-" + UUID.randomUUID().toString() + ".vhd");
                }
            }
        }
    }

    private static String blobUrl(String storageAccountName, String containerName, String blobName) {
        // Future: Get the storage domain from the environment
        return  "https://" + storageAccountName + ".blob.core.windows.net" + "/" + containerName + "/" + blobName;
    }
}
