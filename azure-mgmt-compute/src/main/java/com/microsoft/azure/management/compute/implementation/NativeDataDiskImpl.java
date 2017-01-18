package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.VirtualHardDisk;
import com.microsoft.azure.management.compute.VirtualMachineNativeDataDisk;
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
class NativeDataDiskImpl
        extends ChildResourceImpl<DataDisk, VirtualMachineImpl, VirtualMachine>
        implements
        VirtualMachineNativeDataDisk,
        VirtualMachineNativeDataDisk.DefinitionStages.Blank<VirtualMachine.DefinitionStages.WithNativeCreate>,
        VirtualMachineNativeDataDisk.DefinitionStages.WithDiskSource<VirtualMachine.DefinitionStages.WithNativeCreate>,
        VirtualMachineNativeDataDisk.DefinitionStages.WithVhdAttachedDiskSettings<VirtualMachine.DefinitionStages.WithNativeCreate>,
        VirtualMachineNativeDataDisk.DefinitionStages.WithNewVhdDiskSettings<VirtualMachine.DefinitionStages.WithNativeCreate>,
        VirtualMachineNativeDataDisk.DefinitionStages.WithFromImageDiskSettings<VirtualMachine.DefinitionStages.WithNativeCreate>,
        VirtualMachineNativeDataDisk.UpdateDefinitionStages.Blank<VirtualMachine.Update>,
        VirtualMachineNativeDataDisk.UpdateDefinitionStages.WithDiskSource<VirtualMachine.Update>,
        VirtualMachineNativeDataDisk.UpdateDefinitionStages.WithVhdAttachedDiskSettings<VirtualMachine.Update>,
        VirtualMachineNativeDataDisk.UpdateDefinitionStages.WithNewVhdDiskSettings<VirtualMachine.Update>,
        VirtualMachineNativeDataDisk.Update {

    protected NativeDataDiskImpl(DataDisk inner, VirtualMachineImpl parent) {
        super(inner, parent);
    }

    protected static NativeDataDiskImpl prepareDataDisk(String name,
                                                        VirtualMachineImpl parent) {
        DataDisk dataDiskInner = new DataDisk();
        dataDiskInner.withLun(-1)
                .withName(name)
                .withVhd(null);
        return new NativeDataDiskImpl(dataDiskInner, parent);
    }

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

    @Override
    public NativeDataDiskImpl withNewVhd(int sizeInGB) {
        this.inner()
                .withCreateOption(DiskCreateOptionTypes.EMPTY)
                .withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public NativeDataDiskImpl withExistingVhd(String storageAccountName, String containerName, String vhdName) {
        this.inner()
                .withCreateOption(DiskCreateOptionTypes.ATTACH)
                .withVhd(new VirtualHardDisk()
                        .withUri(blobUrl(storageAccountName, containerName, vhdName)));
        return this;
    }

    @Override
    public NativeDataDiskImpl fromImage(int imageLun) {
        this.inner()
                .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                .withLun(imageLun);
        return this;
    }

    @Override
    public NativeDataDiskImpl withSizeInGB(Integer sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public NativeDataDiskImpl withLun(Integer lun) {
        this.inner().withLun(lun);
        return this;
    }

    @Override
    public NativeDataDiskImpl withCaching(CachingTypes cachingType) {
        this.inner().withCaching(cachingType);
        return this;
    }

    @Override
    public NativeDataDiskImpl storeAt(String storageAccountName, String containerName, String vhdName) {
        this.inner().withVhd(new VirtualHardDisk());
        // URL points to where the underlying vhd needs to be stored
        this.inner().vhd().withUri(blobUrl(storageAccountName, containerName, vhdName));
        return this;
    }

    @Override
    public VirtualMachineImpl attach() {
        return this.parent().withNativeDataDisk(this);
    }

    protected static void setDataDisksDefaults(List<VirtualMachineNativeDataDisk> dataDisks, String namePrefix) {
        List<Integer> usedLuns = new ArrayList<>();
        for (VirtualMachineNativeDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() != -1) {
                usedLuns.add(dataDisk.lun());
            }
        }

        for (VirtualMachineNativeDataDisk dataDisk : dataDisks) {
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

    protected static void ensureDisksVhdUri(List<VirtualMachineNativeDataDisk> dataDisks, StorageAccount storageAccount, String namePrefix) {
        for (VirtualMachineNativeDataDisk dataDisk : dataDisks) {
            if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY
                    || dataDisk.creationMethod() == DiskCreateOptionTypes.FROM_IMAGE) {
                //New empty and from image data disk requires Vhd Uri to be set
                if (dataDisk.inner().vhd() == null) {
                    dataDisk.inner().withVhd(new VirtualHardDisk());
                    dataDisk.inner().vhd().withUri(storageAccount.endPoints().primary().blob()
                            + "vhds/"
                            + namePrefix + "-data-disk-" + dataDisk.lun() + "-" + UUID.randomUUID().toString() + ".vhd");
                }
            }
        }
    }

    protected static void ensureDisksVhdUri(List<VirtualMachineNativeDataDisk> dataDisks, String namePrefix) {
        String containerUrl = null;
        for (VirtualMachineNativeDataDisk dataDisk : dataDisks) {
            if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY && dataDisk.inner().vhd() != null) {
                int idx = dataDisk.inner().vhd().uri().lastIndexOf('/');
                containerUrl = dataDisk.inner().vhd().uri().substring(0, idx);
                break;
            }
        }
        if (containerUrl != null) {
            for (VirtualMachineNativeDataDisk dataDisk : dataDisks) {
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