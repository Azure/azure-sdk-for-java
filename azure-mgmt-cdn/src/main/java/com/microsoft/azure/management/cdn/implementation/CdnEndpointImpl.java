/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CustomDomainValidationResult;
import com.microsoft.azure.management.cdn.GeoFilter;
import com.microsoft.azure.management.cdn.GeoFilterActions;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for {@link CdnEndpoint}.
 */
class CdnEndpointImpl extends ExternalChildResourceImpl<CdnEndpoint,
        EndpointInner,
        CdnProfileImpl,
        CdnProfile>
        implements CdnEndpoint,

        CdnEndpoint.DefinitionStages.Blank.StandardEndpoint<CdnProfile.DefinitionStages.WithStandardCreate>,
        CdnEndpoint.DefinitionStages.Blank.PremiumEndpoint<CdnProfile.DefinitionStages.WithPremiumVerizonCreate>,
        CdnEndpoint.DefinitionStages.WithStandardAttach<CdnProfile.DefinitionStages.WithStandardCreate>,
        CdnEndpoint.DefinitionStages.WithPremiumAttach<CdnProfile.DefinitionStages.WithPremiumVerizonCreate>,

        CdnEndpoint.UpdateDefinitionStages.Blank.StandardEndpoint<CdnProfile.Update>,
        CdnEndpoint.UpdateDefinitionStages.Blank.PremiumEndpoint<CdnProfile.Update>,
        CdnEndpoint.UpdateDefinitionStages.WithStandardAttach<CdnProfile.Update>,
        CdnEndpoint.UpdateDefinitionStages.WithPremiumAttach<CdnProfile.Update>,

        CdnEndpoint.UpdateStandardEndpoint,
        CdnEndpoint.UpdatePremiumEndpoint
{

    private final EndpointsInner client;

    CdnEndpointImpl(String name,
                    CdnProfileImpl parent,
                    EndpointInner inner,
                    EndpointsInner client) {
        super(name, parent, inner);
        this.client = client;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public Observable<CdnEndpoint> createAsync() {
        final CdnEndpointImpl self = this;
        return this.client.createAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.inner())
                .map(new Func1<EndpointInner, CdnEndpoint>() {
                    @Override
                    public CdnEndpoint call(EndpointInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<CdnEndpoint> updateAsync() {
        return createAsync();
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name()).map(new Func1<Void, Void>() {
            @Override
            public Void call(Void result) {
                return result;
            }
        });
    }

    @Override
    public CdnEndpointImpl refresh() {
        EndpointInner inner = this.client.get(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
        this.setInner(inner);
        return this;
    }

    @Override
    public CdnProfileImpl attach() {
        return this.parent();
    }

    @Override
    public List<String> customDomains() {
        return null;
    }

    @Override
    public void purgeContent(List<String> contentPaths) {

    }

    @Override
    public void loadContent(List<String> contentPaths) {

    }

    @Override
    public CustomDomainValidationResult validateCustomDomain(String hostName) {
        return null;
    }

    @Override
    public CdnEndpointImpl withOrigin(String originName, String hostname) {
        return this;
    }

    @Override
    public CdnEndpointImpl withOrigin(String hostname) {
        return this;
    }

    @Override
    public CdnEndpointImpl withPremiumOrigin(String originName, String hostname) {
        return this;
    }

    @Override
    public CdnEndpointImpl withPremiumOrigin(String hostname) {
        return this;
    }

    @Override
    public CdnEndpointImpl withOriginPath(String originPath) {
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpAllowed(boolean httpAllowed) {
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpsAllowed(boolean httpsAllowed) {
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpPort(int httpPort) {
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpsPort(int httpsPort) {
        return this;
    }

    @Override
    public CdnEndpointImpl withHostHeader(String hostHeader) {
        return this;
    }

    @Override
    public CdnEndpointImpl withContentTypesToCompress(List<String> contentTypesToCompress) {
        return this;
    }

    @Override
    public CdnEndpointImpl withoutContentTypesToCompress() {
        return this;
    }

    @Override
    public CdnEndpointImpl withContentTypeToCompress(String contentTypeToCompress) {
        return this;
    }

    @Override
    public CdnEndpointImpl withoutContentTypeToCompress(String contentTypeToCompress) {
        return this;
    }

    @Override
    public CdnEndpointImpl withCompressionEnabled(boolean compressionEnabled) {
        return this;
    }

    @Override
    public CdnEndpointImpl withCachingBehavior(QueryStringCachingBehavior cachingBehavior) {
        return this;
    }

    @Override
    public CdnEndpointImpl withGeoFilters(List<GeoFilter> geoFilters) {
        return this;
    }

    @Override
    public CdnEndpointImpl withoutGeoFilters() {
        return this;
    }

    @Override
    public CdnEndpointImpl withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCodes) {
        return this;
    }

    @Override
    public CdnEndpointImpl withoutGeoFilter(String relativePath) {
        return this;
    }

    @Override
    public CdnEndpointImpl withCustomDomain(String hostName) {
        return this;
    }

    @Override
    public CdnEndpointImpl withoutCustomDomain(String hostName) {
        return this;
    }
}
