package com.microsoft.azure.implementation;

import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.storage.ResourceGroupContext;
import com.microsoft.azure.management.storage.implementation.ResourceGroupContextImpl;

public class AzureResourceGroupImpl extends ResourceGroupImpl implements Azure.ResourceGroup {
    private ResourceGroup group;

    public AzureResourceGroupImpl(ResourceGroup group, ResourceManagementClientImpl serviceClient) {
        super(group.inner(), serviceClient);
        this.group = group;
    }

    public ResourceGroupContext.StorageAccounts storageAccounts() {
        return new ResourceGroupContextImpl(this.group).storageAccounts(null);
    }
}
