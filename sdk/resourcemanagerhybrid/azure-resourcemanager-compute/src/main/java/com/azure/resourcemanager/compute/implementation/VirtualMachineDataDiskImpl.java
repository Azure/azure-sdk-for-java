// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DataDisk;
import com.azure.resourcemanager.compute.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/** The implementation for {@link VirtualMachineDataDisk} interface. */
class VirtualMachineDataDiskImpl extends WrapperImpl<DataDisk> implements VirtualMachineDataDisk {

    VirtualMachineDataDiskImpl(DataDisk dataDiskInner) {
        super(dataDiskInner);
    }

    @Override
    public int size() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().diskSizeGB());
    }

    @Override
    public int lun() {
        return this.innerModel().lun();
    }

    @Override
    public CachingTypes cachingType() {
        return this.innerModel().caching();
    }

    @Override
    public DiskCreateOptionTypes creationMethod() {
        return this.innerModel().createOption();
    }

    @Override
    public StorageAccountTypes storageAccountType() {
        if (this.innerModel().managedDisk() == null) {
            return null;
        }
        return this.innerModel().managedDisk().storageAccountType();
    }

    @Override
    public String id() {
        if (this.innerModel().managedDisk() == null) {
            return null;
        }
        return this.innerModel().managedDisk().id();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }
}
