package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Features;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.FeatureClientImpl;
import com.microsoft.azure.management.resources.implementation.api.FeaturesInner;
import com.microsoft.azure.management.resources.models.Feature;
import com.microsoft.azure.management.resources.models.implementation.FeatureImpl;
import com.microsoft.azure.management.resources.models.implementation.api.FeatureResultInner;

import java.io.IOException;
import java.util.List;

public final class FeaturesImpl
        implements Features {
    private FeaturesInner features;
    private FeatureClientImpl serviceClient;

    public FeaturesImpl(FeatureClientImpl serviceClient) {
        this.serviceClient = serviceClient;
        this.features = serviceClient.features();
    }

    @Override
    public List<Feature> list() throws CloudException, IOException {
        PagedListConverter<FeatureResultInner, Feature> converter = new PagedListConverter<FeatureResultInner, Feature>() {
            @Override
            public Feature typeConvert(FeatureResultInner tenantInner) {
                return new FeatureImpl(tenantInner);
            }
        };
        return converter.convert(features.listAll().getBody());
    }

    @Override
    public InResourceProvider resourceProvider(String resourceProviderName) {
        return null;
    }
}
