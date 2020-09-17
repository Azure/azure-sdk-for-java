// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DataDisk;
import com.azure.resourcemanager.compute.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.models.VirtualHardDisk;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The implementation for {@link DataDisk} and its create and update interfaces. */
class UnmanagedDataDiskImpl extends ChildResourceImpl<DataDisk, VirtualMachineImpl, VirtualMachine>
    implements VirtualMachineUnmanagedDataDisk,
        VirtualMachineUnmanagedDataDisk.DefinitionWithExistingVhd<VirtualMachine.DefinitionStages.WithUnmanagedCreate>,
        VirtualMachineUnmanagedDataDisk.DefinitionWithNewVhd<VirtualMachine.DefinitionStages.WithUnmanagedCreate>,
        VirtualMachineUnmanagedDataDisk.DefinitionWithImage<VirtualMachine.DefinitionStages.WithUnmanagedCreate>,
        VirtualMachineUnmanagedDataDisk.UpdateDefinitionWithExistingVhd<VirtualMachine.Update>,
        VirtualMachineUnmanagedDataDisk.UpdateDefinitionWithNewVhd<VirtualMachine.Update>,
        VirtualMachineUnmanagedDataDisk.Update {

    protected UnmanagedDataDiskImpl(DataDisk inner, VirtualMachineImpl parent) {
        super(inner, parent);
    }

    protected static UnmanagedDataDiskImpl prepareDataDisk(String name, VirtualMachineImpl parent) {
        DataDisk dataDiskInner = new DataDisk();
        dataDiskInner.withLun(-1).withName(name).withVhd(null);
        return new UnmanagedDataDiskImpl(dataDiskInner, parent);
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
    public UnmanagedDataDiskImpl withNewVhd(int sizeInGB) {
        this.inner().withCreateOption(DiskCreateOptionTypes.EMPTY).withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public UnmanagedDataDiskImpl withExistingVhd(String storageAccountName, String containerName, String vhdName) {
        this
            .inner()
            .withCreateOption(DiskCreateOptionTypes.ATTACH)
            .withVhd(new VirtualHardDisk().withUri(blobUrl(storageAccountName, containerName, vhdName)));
        return this;
    }

    @Override
    public UnmanagedDataDiskImpl fromImage(int imageLun) {
        this.inner().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE).withLun(imageLun);
        return this;
    }

    @Override
    public UnmanagedDataDiskImpl withSizeInGB(Integer sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public UnmanagedDataDiskImpl withLun(Integer lun) {
        this.inner().withLun(lun);
        return this;
    }

    @Override
    public UnmanagedDataDiskImpl withCaching(CachingTypes cachingType) {
        this.inner().withCaching(cachingType);
        return this;
    }

    @Override
    public UnmanagedDataDiskImpl storeAt(String storageAccountName, String containerName, String vhdName) {
        this.inner().withVhd(new VirtualHardDisk());
        // URL points to where the underlying vhd needs to be stored
        this.inner().vhd().withUri(blobUrl(storageAccountName, containerName, vhdName));
        return this;
    }

    @Override
    public VirtualMachineImpl attach() {
        return this.parent().withUnmanagedDataDisk(this);
    }

    protected static void setDataDisksDefaults(List<VirtualMachineUnmanagedDataDisk> dataDisks, String namePrefix) {
        List<Integer> usedLuns = new ArrayList<>();
        for (VirtualMachineUnmanagedDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() != -1) {
                usedLuns.add(dataDisk.lun());
            }
        }

        for (VirtualMachineUnmanagedDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() == -1) {
                Integer i = 0;
                while (usedLuns.contains(i)) {
                    i += 1;
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

    protected static void ensureDisksVhdUri(
        List<VirtualMachineUnmanagedDataDisk> dataDisks, StorageAccount storageAccount, String namePrefix) {
        for (VirtualMachineUnmanagedDataDisk dataDisk : dataDisks) {
            if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY
                || dataDisk.creationMethod() == DiskCreateOptionTypes.FROM_IMAGE) {
                // New empty and from image data disk requires Vhd Uri to be set
                if (dataDisk.inner().vhd() == null) {
                    dataDisk.inner().withVhd(new VirtualHardDisk());
                    dataDisk
                        .inner()
                        .vhd()
                        .withUri(
                            storageAccount.endPoints().primary().blob()
                                + "vhds/"
                                + namePrefix
                                + "-data-disk-"
                                + dataDisk.lun()
                                + "-"
                                + UUID.randomUUID().toString()
                                + ".vhd");
                }
            }
        }
    }

    protected static void ensureDisksVhdUri(List<VirtualMachineUnmanagedDataDisk> dataDisks, String namePrefix) {
        String containerUrl = null;
        for (VirtualMachineUnmanagedDataDisk dataDisk : dataDisks) {
            if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY && dataDisk.inner().vhd() != null) {
                int idx = dataDisk.inner().vhd().uri().lastIndexOf('/');
                containerUrl = dataDisk.inner().vhd().uri().substring(0, idx);
                break;
            }
        }
        if (containerUrl != null) {
            for (VirtualMachineUnmanagedDataDisk dataDisk : dataDisks) {
                if (dataDisk.creationMethod() == DiskCreateOptionTypes.EMPTY) {
                    // New data disk requires Vhd Uri to be set
                    if (dataDisk.inner().vhd() == null) {
                        dataDisk.inner().withVhd(new VirtualHardDisk());
                        dataDisk
                            .inner()
                            .vhd()
                            .withUri(
                                containerUrl
                                    + namePrefix
                                    + "-data-disk-"
                                    + dataDisk.lun()
                                    + "-"
                                    + UUID.randomUUID().toString()
                                    + ".vhd");
                    }
                }
            }
        }
    }

    private String blobUrl(String storageAccountName, String containerName, String blobName) {
        AzureEnvironment azureEnvironment = this.parent().environment();
        return "https://"
            + storageAccountName
            + ".blob"
            + azureEnvironment.getStorageEndpointSuffix()
            + "/"
            + containerName
            + "/"
            + blobName;
    }
}
