package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Features;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.FeatureClientImpl;
import com.microsoft.azure.management.resources.implementation.api.FeaturesInner;
import com.microsoft.azure.management.resources.Feature;
import com.microsoft.azure.management.resources.implementation.api.FeatureResultInner;

import java.io.IOException;
import java.util.List;

public final class FeaturesInResourceProviderImpl
        implements Features.InResourceProvider {
    private FeaturesInner features;
    private FeatureClientImpl serviceClient;
    private String resourceProviderNamespace;

    public FeaturesInResourceProviderImpl(FeatureClientImpl serviceClient, String resourceProviderNamespace) {
        this.serviceClient = serviceClient;
        this.features = serviceClient.features();
        this.resourceProviderNamespace = resourceProviderNamespace;
    }

    @Override
    public List<Feature> list() throws CloudException, IOException {
        PagedListConverter<FeatureResultInner, Feature> converter = new PagedListConverter<FeatureResultInner, Feature>() {
            @Override
            public Feature typeConvert(FeatureResultInner tenantInner) {
                return new FeatureImpl(tenantInner);
            }
        };
        return converter.convert(features.list(resourceProviderNamespace).getBody());
    }

    @Override
    public Feature register(String featureName) throws IOException, CloudException {
        return new FeatureImpl(features.register(resourceProviderNamespace, featureName).getBody());
    }

    @Override
    public Feature get(String name) throws CloudException, IOException {
        return new FeatureImpl(features.get(resourceProviderNamespace, name).getBody());
    }
}
