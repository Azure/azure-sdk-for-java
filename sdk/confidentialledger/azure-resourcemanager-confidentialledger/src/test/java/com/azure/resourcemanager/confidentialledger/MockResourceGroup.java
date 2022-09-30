package com.azure.resourcemanager.confidentialledger;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluent.models.ResourceGroupInner;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroupExportResult;
import com.azure.resourcemanager.resources.models.ResourceGroupExportTemplateOptions;
import reactor.core.publisher.Mono;

import java.util.Map;

public class MockResourceGroup implements ResourceGroup {
    private String mockResourceGroupName;
    public MockResourceGroup(String testGroupName) {
        this.mockResourceGroupName = testGroupName;
    }
    @Override
    public String provisioningState() {
        return null;
    }

    @Override
    public ResourceGroupExportResult exportTemplate(ResourceGroupExportTemplateOptions resourceGroupExportTemplateOptions) {
        return null;
    }

    @Override
    public Mono<ResourceGroupExportResult> exportTemplateAsync(ResourceGroupExportTemplateOptions resourceGroupExportTemplateOptions) {
        return null;
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public String regionName() {
        return null;
    }

    @Override
    public Region region() {
        return null;
    }

    @Override
    public Map<String, String> tags() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public String name() {
        return mockResourceGroupName;
    }

    @Override
    public ResourceGroupInner innerModel() {
        return null;
    }

    @Override
    public String key() {
        return null;
    }

    @Override
    public ResourceGroup refresh() {
        return null;
    }

    @Override
    public Mono<ResourceGroup> refreshAsync() {
        return null;
    }

    @Override
    public Update update() {
        return null;
    }
}
