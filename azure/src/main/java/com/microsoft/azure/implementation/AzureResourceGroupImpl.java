package com.microsoft.azure.implementation;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;

final class AzureResourceGroupImpl 
    extends ResourceGroupImpl 
    implements Azure.ResourceGroup {
    
    AzureResourceGroupImpl(ResourceGroup resourceGroup, ResourceManagementClientImpl client) {
        super(resourceGroup.inner(), client);
    }
}
