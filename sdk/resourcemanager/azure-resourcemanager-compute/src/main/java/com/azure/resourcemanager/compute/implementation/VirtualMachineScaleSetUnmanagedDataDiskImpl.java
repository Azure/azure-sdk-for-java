// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUnmanagedDataDisk;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.ArrayList;
import java.util.List;

/** The implementation for {@link VirtualMachineScaleSetUnmanagedDataDisk} and its create and update interfaces. */
class VirtualMachineScaleSetUnmanagedDataDiskImpl
    extends ChildResourceImpl<VirtualMachineScaleSetDataDisk, VirtualMachineScaleSetImpl, VirtualMachineScaleSet>
    implements VirtualMachineScaleSetUnmanagedDataDisk.DefinitionWithNewVhd<
            VirtualMachineScaleSet.DefinitionStages.WithUnmanagedCreate>,
        VirtualMachineScaleSetUnmanagedDataDisk.DefinitionWithImage<
            VirtualMachineScaleSet.DefinitionStages.WithUnmanagedCreate>,
        VirtualMachineScaleSetUnmanagedDataDisk.UpdateDefinition<VirtualMachineScaleSet.UpdateStages.WithApply>,
        VirtualMachineScaleSetUnmanagedDataDisk.Update,
        VirtualMachineScaleSetUnmanagedDataDisk {

    protected VirtualMachineScaleSetUnmanagedDataDiskImpl(
        VirtualMachineScaleSetDataDisk innerObject, VirtualMachineScaleSetImpl parent) {
        super(innerObject, parent);
    }

    protected static VirtualMachineScaleSetUnmanagedDataDiskImpl prepareDataDisk(
        String name, VirtualMachineScaleSetImpl parent) {
        VirtualMachineScaleSetDataDisk dataDiskInner = new VirtualMachineScaleSetDataDisk();
        dataDiskInner.withLun(-1).withName(name);
        return new VirtualMachineScaleSetUnmanagedDataDiskImpl(dataDiskInner, parent);
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withNewVhd(int sizeInGB) {
        this.innerModel().withCreateOption(DiskCreateOptionTypes.EMPTY).withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl fromImage(int imageLun) {
        this.innerModel().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE).withLun(imageLun);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withSizeInGB(Integer sizeInGB) {
        this.innerModel().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withLun(Integer lun) {
        this.innerModel().withLun(lun);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withCaching(CachingTypes cachingType) {
        this.innerModel().withCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl attach() {
        return this.parent().withUnmanagedDataDisk(this);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    protected static void setDataDisksDefaults(List<VirtualMachineScaleSetDataDisk> dataDisks, String namePrefix) {
        if (dataDisks == null) {
            return;
        }
        List<Integer> usedLuns = new ArrayList<>();
        for (VirtualMachineScaleSetDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() != -1) {
                usedLuns.add(dataDisk.lun());
            }
        }
        for (VirtualMachineScaleSetDataDisk dataDisk : dataDisks) {
            if (dataDisk.lun() == -1) {
                Integer i = 0;
                while (usedLuns.contains(i)) {
                    i += 1;
                }
                dataDisk.withLun(i);
                usedLuns.add(i);
            }
            if (dataDisk.name() == null) {
                dataDisk.withName(namePrefix + "-data-disk-" + dataDisk.lun());
            }
            if (dataDisk.caching() == null) {
                dataDisk.withCaching(CachingTypes.READ_WRITE);
            }
        }
    }
}
