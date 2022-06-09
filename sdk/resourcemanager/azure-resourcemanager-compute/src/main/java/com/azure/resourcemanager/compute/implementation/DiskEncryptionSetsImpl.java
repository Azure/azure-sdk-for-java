// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.DiskEncryptionSetsClient;
import com.azure.resourcemanager.compute.fluent.models.DiskEncryptionSetInner;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSets;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

public class DiskEncryptionSetsImpl
    extends TopLevelModifiableResourcesImpl<
        DiskEncryptionSet,
        DiskEncryptionSetImpl,
        DiskEncryptionSetInner,
        DiskEncryptionSetsClient,
        ComputeManager>
    implements DiskEncryptionSets {
    public DiskEncryptionSetsImpl(ComputeManager manager) {
        super(manager.serviceClient().getDiskEncryptionSets(), manager);
    }

    @Override
    public DiskEncryptionSet.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    protected DiskEncryptionSetImpl wrapModel(String name) {
        DiskEncryptionSetInner inner = new DiskEncryptionSetInner();
        return new DiskEncryptionSetImpl(name, inner, manager());
    }

    @Override
    protected DiskEncryptionSetImpl wrapModel(DiskEncryptionSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new DiskEncryptionSetImpl(inner.name(), inner, manager());
    }
}
