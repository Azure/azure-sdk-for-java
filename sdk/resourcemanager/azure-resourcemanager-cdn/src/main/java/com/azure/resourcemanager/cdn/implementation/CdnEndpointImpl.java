// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.cdn.fluent.inner.CustomDomainInner;
import com.azure.resourcemanager.cdn.fluent.inner.EndpointInner;
import com.azure.resourcemanager.cdn.models.EndpointUpdateParameters;
import com.azure.resourcemanager.cdn.models.OriginUpdateParameters;
import com.azure.resourcemanager.cdn.models.QueryStringCachingBehavior;
import com.azure.resourcemanager.cdn.models.ResourceUsage;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CustomDomainValidationResult;
import com.azure.resourcemanager.cdn.models.DeepCreatedOrigin;
import com.azure.resourcemanager.cdn.models.EndpointResourceState;
import com.azure.resourcemanager.cdn.models.GeoFilter;
import com.azure.resourcemanager.cdn.models.GeoFilterActions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation for {@link CdnEndpoint}.
 */
class CdnEndpointImpl
    extends ExternalChildResourceImpl<
        CdnEndpoint,
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
        CdnEndpoint.UpdatePremiumEndpoint {

    private List<CustomDomainInner> customDomainList;
    private List<CustomDomainInner> deletedCustomDomainList;

    CdnEndpointImpl(String name, CdnProfileImpl parent, EndpointInner inner) {
        super(name, parent, inner);
        this.customDomainList = new ArrayList<>();
        this.deletedCustomDomainList = new ArrayList<>();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public Mono<CdnEndpoint> createResourceAsync() {
        final CdnEndpointImpl self = this;
        return this.parent().manager().inner().getEndpoints().createAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.inner())
            .flatMap(inner -> {
                self.setInner(inner);
                return Flux.fromIterable(self.customDomainList)
                    .flatMapDelayError(customDomainInner -> self.parent().manager().inner()
                        .getCustomDomains().createAsync(
                            self.parent().resourceGroupName(),
                            self.parent().name(),
                            self.name(),
                            self.parent().manager().sdkContext().randomResourceName("CustomDomain", 50),
                            customDomainInner.hostname()), 32, 32)
                    .then(self.parent().manager().inner()
                        .getCustomDomains().listByEndpointAsync(
                            self.parent().resourceGroupName(),
                            self.parent().name(),
                            self.name())
                        .collectList()
                        .map(customDomainInners -> {
                            self.customDomainList.addAll(customDomainInners);
                            return self;
                        }));
            });
    }

    @Override
    public Mono<CdnEndpoint> updateResourceAsync() {
        final CdnEndpointImpl self = this;
        EndpointUpdateParameters endpointUpdateParameters = new EndpointUpdateParameters();
        endpointUpdateParameters.withIsHttpAllowed(this.inner().isHttpAllowed())
                .withIsHttpsAllowed(this.inner().isHttpsAllowed())
                .withOriginPath(this.inner().originPath())
                .withOriginHostHeader(this.inner().originHostHeader())
                .withIsCompressionEnabled(this.inner().isCompressionEnabled())
                .withContentTypesToCompress(this.inner().contentTypesToCompress())
                .withGeoFilters(this.inner().geoFilters())
                .withOptimizationType(this.inner().optimizationType())
                .withQueryStringCachingBehavior(this.inner().queryStringCachingBehavior())
                .withTags(this.inner().tags());

        DeepCreatedOrigin originInner = this.inner().origins().get(0);
        OriginUpdateParameters originUpdateParameters = new OriginUpdateParameters()
                .withHostname(originInner.hostname())
                .withHttpPort(originInner.httpPort())
                .withHttpsPort(originInner.httpsPort());

        Mono<EndpointInner> originUpdateTask = this.parent().manager().inner().getOrigins().updateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                originInner.name(),
                originUpdateParameters)
            .then(Mono.empty());

        Mono<EndpointInner> endpointUpdateTask = this.parent().manager().inner().getEndpoints().updateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                endpointUpdateParameters);

        Flux<CustomDomainInner> customDomainCreateTask = Flux.fromIterable(this.customDomainList)
            .flatMapDelayError(itemToCreate -> this.parent().manager().inner().getCustomDomains().createAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                self.parent().manager().sdkContext().randomResourceName("CustomDomain", 50),
                itemToCreate.hostname()
            ), 32, 32);

        Flux<CustomDomainInner> customDomainDeleteTask = Flux.fromIterable(this.deletedCustomDomainList)
            .flatMapDelayError(itemToDelete -> this.parent().manager().inner().getCustomDomains().deleteAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                itemToDelete.name()
            ), 32, 32);

        Mono<EndpointInner> customDomainTask = Flux.concat(customDomainCreateTask, customDomainDeleteTask)
            .then(Mono.empty());

        return Flux.mergeDelayError(32, customDomainTask, originUpdateTask, endpointUpdateTask)
            .last()
            .map(inner -> {
                self.setInner(inner);
                self.customDomainList.clear();
                self.deletedCustomDomainList.clear();
                return self;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.parent().manager().inner().getEndpoints().deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
    }

    @Override
    public Mono<CdnEndpoint> refreshAsync() {
        final CdnEndpointImpl self = this;
        return super.refreshAsync()
            .flatMap(cdnEndpoint -> {
                self.customDomainList.clear();
                self.deletedCustomDomainList.clear();
                return self.parent().manager().inner().getCustomDomains().listByEndpointAsync(
                        self.parent().resourceGroupName(),
                        self.parent().name(),
                        self.name()
                    )
                    .collectList()
                    .map(customDomainInners -> {
                        self.customDomainList.addAll(customDomainInners);
                        return self;
                    });
            });
    }

    @Override
    protected Mono<EndpointInner> getInnerAsync() {
        return this.parent().manager().inner().getEndpoints().getAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
    }

    @Override
    public PagedIterable<ResourceUsage> listResourceUsage() {
        return this.parent().manager().inner().getEndpoints().listResourceUsage(
            this.parent().resourceGroupName(),
            this.parent().name(),
            this.name())
            .mapPage(ResourceUsage::new);
    }

    @Override
    public CdnProfileImpl attach() {
        return this.parent();
    }

    @Override
    public String originHostHeader() {
        return this.inner().originHostHeader();
    }

    @Override
    public String originPath() {
        return this.inner().originPath();
    }

    @Override
    public Set<String> contentTypesToCompress() {
        List<String> contentTypes = this.inner().contentTypesToCompress();
        Set<String> set = new HashSet<>();
        if (contentTypes != null) {
            set.addAll(contentTypes);
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public boolean isCompressionEnabled() {
        return this.inner().isCompressionEnabled();
    }

    @Override
    public boolean isHttpAllowed() {
        return this.inner().isHttpAllowed();
    }

    @Override
    public boolean isHttpsAllowed() {
        return this.inner().isHttpsAllowed();
    }

    @Override
    public QueryStringCachingBehavior queryStringCachingBehavior() {
        return this.inner().queryStringCachingBehavior();
    }

    @Override
    public String optimizationType() {
        if (this.inner().optimizationType() == null) {
            return null;
        }
        return this.inner().optimizationType().toString();
    }

    @Override
    public List<GeoFilter> geoFilters() {
        return this.inner().geoFilters();
    }

    @Override
    public String hostname() {
        return this.inner().hostname();
    }

    @Override
    public EndpointResourceState resourceState() {
        return this.inner().resourceState();
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public String originHostName() {
        if (this.inner().origins() != null && !this.inner().origins().isEmpty()) {
            return this.inner().origins().get(0).hostname();
        }
        return null;
    }

    @Override
    public int httpPort() {
        if (this.inner().origins() != null && !this.inner().origins().isEmpty()) {
            Integer httpPort = this.inner().origins().get(0).httpPort();
            return (httpPort != null) ? httpPort : 0;
        }
        return 0;
    }

    @Override
    public int httpsPort() {
        if (this.inner().origins() != null && !this.inner().origins().isEmpty()) {
            Integer httpsPort = this.inner().origins().get(0).httpsPort();
            return (httpsPort != null) ? httpsPort : 0;
        }
        return 0;
    }

    @Override
    public Set<String> customDomains() {
        Set<String> set = new HashSet<>();
        for (CustomDomainInner customDomainInner : this.parent().manager().inner().getCustomDomains()
            .listByEndpoint(this.parent().resourceGroupName(), this.parent().name(), this.name())) {
            set.add(customDomainInner.hostname());
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public void start() {
        this.parent().startEndpoint(this.name());
    }

    @Override
    public Mono<Void> startAsync() {
        return this.parent().startEndpointAsync(this.name());
    }

    @Override
    public void stop() {
        this.stopAsync().block();
    }

    @Override
    public Mono<Void> stopAsync() {
        return this.parent().stopEndpointAsync(this.name());
    }

    @Override
    public void purgeContent(Set<String> contentPaths) {
        if (contentPaths != null) {
            this.purgeContentAsync(contentPaths).block();
        }
    }

    @Override
    public Mono<Void> purgeContentAsync(Set<String> contentPaths) {
        return this.parent().purgeEndpointContentAsync(this.name(), contentPaths);
    }

    @Override
    public void loadContent(Set<String> contentPaths) {
        this.loadContentAsync(contentPaths).block();
    }

    @Override
    public Mono<Void> loadContentAsync(Set<String> contentPaths) {
        return this.parent().loadEndpointContentAsync(this.name(), contentPaths);
    }

    @Override
    public CustomDomainValidationResult validateCustomDomain(String hostName) {
        return this.validateCustomDomainAsync(hostName).block();
    }

    @Override
    public Mono<CustomDomainValidationResult> validateCustomDomainAsync(String hostName) {
        return this.parent().validateEndpointCustomDomainAsync(this.name(), hostName);
    }

    @Override
    public CdnEndpointImpl withOrigin(String originName, String hostname) {
        this.inner().origins().add(
                new DeepCreatedOrigin()
                        .withName(originName)
                        .withHostname(hostname));
        return this;
    }

    @Override
    public CdnEndpointImpl withOrigin(String hostname) {
        return this.withOrigin("origin", hostname);
    }

    @Override
    public CdnEndpointImpl withPremiumOrigin(String originName, String hostname) {
        return this.withOrigin(originName, hostname);
    }

    @Override
    public CdnEndpointImpl withPremiumOrigin(String hostname) {
        return this.withOrigin(hostname);
    }

    @Override
    public CdnEndpointImpl withOriginPath(String originPath) {
        this.inner().withOriginPath(originPath);
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpAllowed(boolean httpAllowed) {
        this.inner().withIsHttpAllowed(httpAllowed);
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpsAllowed(boolean httpsAllowed) {
        this.inner().withIsHttpsAllowed(httpsAllowed);
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpPort(int httpPort) {
        if (this.inner().origins() != null && !this.inner().origins().isEmpty()) {
            this.inner().origins().get(0).withHttpPort(httpPort);
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpsPort(int httpsPort) {
        if (this.inner().origins() != null && !this.inner().origins().isEmpty()) {
            this.inner().origins().get(0).withHttpsPort(httpsPort);
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withHostHeader(String hostHeader) {
        this.inner().withOriginHostHeader(hostHeader);
        return this;
    }

    @Override
    public CdnEndpointImpl withContentTypesToCompress(Set<String> contentTypesToCompress) {
        List<String> list = null;
        if (contentTypesToCompress != null) {
            list = new ArrayList<>(contentTypesToCompress);
        }
        this.inner().withContentTypesToCompress(list);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutContentTypesToCompress() {
        if (this.inner().contentTypesToCompress() != null) {
            this.inner().contentTypesToCompress().clear();
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withContentTypeToCompress(String contentTypeToCompress) {
        if (this.inner().contentTypesToCompress() == null) {
            this.inner().withContentTypesToCompress(new ArrayList<>());
        }
        this.inner().contentTypesToCompress().add(contentTypeToCompress);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutContentTypeToCompress(String contentTypeToCompress) {
        if (this.inner().contentTypesToCompress() != null) {
            this.inner().contentTypesToCompress().remove(contentTypeToCompress);
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withCompressionEnabled(boolean compressionEnabled) {
        this.inner().withIsCompressionEnabled(compressionEnabled);
        return this;
    }

    @Override
    public CdnEndpointImpl withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior) {
        this.inner().withQueryStringCachingBehavior(cachingBehavior);
        return this;
    }

    @Override
    public CdnEndpointImpl withGeoFilters(Collection<GeoFilter> geoFilters) {
        List<GeoFilter> list = null;
        if (geoFilters != null) {
            list = new ArrayList<>(geoFilters);
        }

        this.inner().withGeoFilters(list);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutGeoFilters() {
        if (this.inner().geoFilters() != null) {
            this.inner().geoFilters().clear();
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withGeoFilter(String relativePath, GeoFilterActions action, CountryIsoCode countryCode) {
        GeoFilter geoFilter = this.createGeoFiltersObject(relativePath, action);

        if (geoFilter.countryCodes() == null) {
            geoFilter.withCountryCodes(new ArrayList<>());
        }
        geoFilter.countryCodes().add(countryCode.toString());

        this.inner().geoFilters().add(geoFilter);
        return this;
    }

    @Override
    public CdnEndpointImpl withGeoFilter(
        String relativePath, GeoFilterActions action, Collection<CountryIsoCode> countryCodes) {
        GeoFilter geoFilter = this.createGeoFiltersObject(relativePath, action);

        if (geoFilter.countryCodes() == null) {
            geoFilter.withCountryCodes(new ArrayList<>());
        } else {
            geoFilter.countryCodes().clear();
        }

        for (CountryIsoCode countryCode : countryCodes) {
            geoFilter.countryCodes().add(countryCode.toString());
        }

        this.inner().geoFilters().add(geoFilter);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutGeoFilter(String relativePath) {
        this.inner().geoFilters().removeIf(geoFilter -> geoFilter.relativePath().equals(relativePath));
        return this;
    }

    @Override
    public CdnEndpointImpl withCustomDomain(String hostName) {
        this.customDomainList.add(new CustomDomainInner().withHostname(hostName));
        return this;
    }

    @Override
    public CdnEndpointImpl withoutCustomDomain(String hostName) {
        deletedCustomDomainList.add(new CustomDomainInner().withHostname(hostName));
        return this;
    }

    private GeoFilter createGeoFiltersObject(String relativePath, GeoFilterActions action) {
        if (this.inner().geoFilters() == null) {
            this.inner().withGeoFilters(new ArrayList<>());
        }
        GeoFilter geoFilter = null;
        for (GeoFilter filter : this.inner().geoFilters()) {
            if (filter.relativePath().equals(relativePath)) {
                geoFilter = filter;
                break;
            }
        }
        if (geoFilter == null) {
            geoFilter = new GeoFilter();
        } else {
            this.inner().geoFilters().remove(geoFilter);
        }
        geoFilter.withRelativePath(relativePath)
                .withAction(action);

        return geoFilter;
    }
}
