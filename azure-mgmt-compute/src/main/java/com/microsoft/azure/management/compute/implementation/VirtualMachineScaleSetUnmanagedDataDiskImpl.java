/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetUnmanagedDataDisk;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link VirtualMachineScaleSetUnmanagedDataDisk} and its create and update interfaces.
 */
@LangDefinition
class VirtualMachineScaleSetUnmanagedDataDiskImpl
        extends ChildResourceImpl<VirtualMachineScaleSetDataDisk, VirtualMachineScaleSetImpl, VirtualMachineScaleSet>
        implements
        VirtualMachineScaleSetUnmanagedDataDisk.DefinitionWithNewVhd<VirtualMachineScaleSet.DefinitionStages.WithUnmanagedCreate>,
        VirtualMachineScaleSetUnmanagedDataDisk.DefinitionWithImage<VirtualMachineScaleSet.DefinitionStages.WithUnmanagedCreate>,
        VirtualMachineScaleSetUnmanagedDataDisk.UpdateDefinition<VirtualMachineScaleSet.UpdateStages.WithApply>,
        VirtualMachineScaleSetUnmanagedDataDisk.Update,
        VirtualMachineScaleSetUnmanagedDataDisk {

    protected VirtualMachineScaleSetUnmanagedDataDiskImpl(VirtualMachineScaleSetDataDisk innerObject,
                                                          VirtualMachineScaleSetImpl parent) {
        super(innerObject, parent);
    }

    protected static VirtualMachineScaleSetUnmanagedDataDiskImpl prepareDataDisk(String name,
                                                                                 VirtualMachineScaleSetImpl parent) {
        VirtualMachineScaleSetDataDisk dataDiskInner = new VirtualMachineScaleSetDataDisk();
        dataDiskInner.withLun(-1)
                .withName(name);
        return new VirtualMachineScaleSetUnmanagedDataDiskImpl(dataDiskInner, parent);
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withNewVhd(int sizeInGB) {
        this.inner()
                .withCreateOption(DiskCreateOptionTypes.EMPTY)
                .withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl fromImage(int imageLun) {
        this.inner()
                .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                .withLun(imageLun);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withSizeInGB(Integer sizeInGB) {
        this.inner().withDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withLun(Integer lun) {
        this.inner().withLun(lun);
        return this;
    }

    @Override
    public VirtualMachineScaleSetUnmanagedDataDiskImpl withCaching(CachingTypes cachingType) {
        this.inner().withCaching(cachingType);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl attach() {
        return this.parent().withUnmanagedDataDisk(this);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    protected static void setDataDisksDefaults(List<VirtualMachineScaleSetDataDisk> dataDisks,
                                               String namePrefix) {
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
                    i++;
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
