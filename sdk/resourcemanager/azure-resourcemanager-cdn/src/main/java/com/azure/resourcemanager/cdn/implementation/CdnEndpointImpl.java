// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.cdn.fluent.models.CustomDomainInner;
import com.azure.resourcemanager.cdn.fluent.models.EndpointInner;
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
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

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
        return this.innerModel().id();
    }

    @Override
    public Mono<CdnEndpoint> createResourceAsync() {
        final CdnEndpointImpl self = this;
        return this.parent().manager().serviceClient().getEndpoints().createAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.innerModel())
            .flatMap(inner -> {
                self.setInner(inner);
                return Flux.fromIterable(self.customDomainList)
                    .flatMapDelayError(customDomainInner -> self.parent().manager().serviceClient()
                        .getCustomDomains().createAsync(
                            self.parent().resourceGroupName(),
                            self.parent().name(),
                            self.name(),
                            self.parent().manager().resourceManager().internalContext()
                                .randomResourceName("CustomDomain", 50),
                            customDomainInner.hostname()), 32, 32)
                    .then(self.parent().manager().serviceClient()
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
        endpointUpdateParameters.withIsHttpAllowed(this.innerModel().isHttpAllowed())
                .withIsHttpsAllowed(this.innerModel().isHttpsAllowed())
                .withOriginPath(this.innerModel().originPath())
                .withOriginHostHeader(this.innerModel().originHostHeader())
                .withIsCompressionEnabled(this.innerModel().isCompressionEnabled())
                .withContentTypesToCompress(this.innerModel().contentTypesToCompress())
                .withGeoFilters(this.innerModel().geoFilters())
                .withOptimizationType(this.innerModel().optimizationType())
                .withQueryStringCachingBehavior(this.innerModel().queryStringCachingBehavior())
                .withTags(this.innerModel().tags());

        DeepCreatedOrigin originInner = this.innerModel().origins().get(0);
        OriginUpdateParameters originUpdateParameters = new OriginUpdateParameters()
                .withHostname(originInner.hostname())
                .withHttpPort(originInner.httpPort())
                .withHttpsPort(originInner.httpsPort());

        Mono<EndpointInner> originUpdateTask = this.parent().manager().serviceClient().getOrigins().updateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                originInner.name(),
                originUpdateParameters)
            .then(Mono.empty());

        Mono<EndpointInner> endpointUpdateTask = this.parent().manager().serviceClient().getEndpoints().updateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                endpointUpdateParameters);

        Flux<CustomDomainInner> customDomainCreateTask = Flux.fromIterable(this.customDomainList)
            .flatMapDelayError(itemToCreate -> this.parent().manager().serviceClient().getCustomDomains().createAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                self.parent().manager().resourceManager().internalContext()
                    .randomResourceName("CustomDomain", 50),
                itemToCreate.hostname()
            ), 32, 32);

        Flux<CustomDomainInner> customDomainDeleteTask = Flux.fromIterable(this.deletedCustomDomainList)
            .flatMapDelayError(itemToDelete -> this.parent().manager().serviceClient().getCustomDomains().deleteAsync(
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
        return this.parent().manager().serviceClient().getEndpoints().deleteAsync(this.parent().resourceGroupName(),
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
                return self.parent().manager().serviceClient().getCustomDomains().listByEndpointAsync(
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
        return this.parent().manager().serviceClient().getEndpoints().getAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
    }

    @Override
    public PagedIterable<ResourceUsage> listResourceUsage() {
        return PagedConverter.mapPage(this.parent().manager().serviceClient().getEndpoints().listResourceUsage(
            this.parent().resourceGroupName(),
            this.parent().name(),
            this.name()),
            ResourceUsage::new);
    }

    @Override
    public CdnProfileImpl attach() {
        return this.parent();
    }

    @Override
    public String originHostHeader() {
        return this.innerModel().originHostHeader();
    }

    @Override
    public String originPath() {
        return this.innerModel().originPath();
    }

    @Override
    public Set<String> contentTypesToCompress() {
        List<String> contentTypes = this.innerModel().contentTypesToCompress();
        Set<String> set = new HashSet<>();
        if (contentTypes != null) {
            set.addAll(contentTypes);
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public boolean isCompressionEnabled() {
        return this.innerModel().isCompressionEnabled();
    }

    @Override
    public boolean isHttpAllowed() {
        return this.innerModel().isHttpAllowed();
    }

    @Override
    public boolean isHttpsAllowed() {
        return this.innerModel().isHttpsAllowed();
    }

    @Override
    public QueryStringCachingBehavior queryStringCachingBehavior() {
        return this.innerModel().queryStringCachingBehavior();
    }

    @Override
    public String optimizationType() {
        if (this.innerModel().optimizationType() == null) {
            return null;
        }
        return this.innerModel().optimizationType().toString();
    }

    @Override
    public List<GeoFilter> geoFilters() {
        return this.innerModel().geoFilters();
    }

    @Override
    public String hostname() {
        return this.innerModel().hostname();
    }

    @Override
    public EndpointResourceState resourceState() {
        return this.innerModel().resourceState();
    }

    @Override
    public String provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public String originHostName() {
        if (this.innerModel().origins() != null && !this.innerModel().origins().isEmpty()) {
            return this.innerModel().origins().get(0).hostname();
        }
        return null;
    }

    @Override
    public int httpPort() {
        if (this.innerModel().origins() != null && !this.innerModel().origins().isEmpty()) {
            Integer httpPort = this.innerModel().origins().get(0).httpPort();
            return (httpPort != null) ? httpPort : 0;
        }
        return 0;
    }

    @Override
    public int httpsPort() {
        if (this.innerModel().origins() != null && !this.innerModel().origins().isEmpty()) {
            Integer httpsPort = this.innerModel().origins().get(0).httpsPort();
            return (httpsPort != null) ? httpsPort : 0;
        }
        return 0;
    }

    @Override
    public Set<String> customDomains() {
        Set<String> set = new HashSet<>();
        for (CustomDomainInner customDomainInner : this.parent().manager().serviceClient().getCustomDomains()
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
        this.innerModel().origins().add(
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
        this.innerModel().withOriginPath(originPath);
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpAllowed(boolean httpAllowed) {
        this.innerModel().withIsHttpAllowed(httpAllowed);
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpsAllowed(boolean httpsAllowed) {
        this.innerModel().withIsHttpsAllowed(httpsAllowed);
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpPort(int httpPort) {
        if (this.innerModel().origins() != null && !this.innerModel().origins().isEmpty()) {
            this.innerModel().origins().get(0).withHttpPort(httpPort);
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withHttpsPort(int httpsPort) {
        if (this.innerModel().origins() != null && !this.innerModel().origins().isEmpty()) {
            this.innerModel().origins().get(0).withHttpsPort(httpsPort);
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withHostHeader(String hostHeader) {
        this.innerModel().withOriginHostHeader(hostHeader);
        return this;
    }

    @Override
    public CdnEndpointImpl withContentTypesToCompress(Set<String> contentTypesToCompress) {
        List<String> list = null;
        if (contentTypesToCompress != null) {
            list = new ArrayList<>(contentTypesToCompress);
        }
        this.innerModel().withContentTypesToCompress(list);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutContentTypesToCompress() {
        if (this.innerModel().contentTypesToCompress() != null) {
            this.innerModel().contentTypesToCompress().clear();
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withContentTypeToCompress(String contentTypeToCompress) {
        if (this.innerModel().contentTypesToCompress() == null) {
            this.innerModel().withContentTypesToCompress(new ArrayList<>());
        }
        this.innerModel().contentTypesToCompress().add(contentTypeToCompress);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutContentTypeToCompress(String contentTypeToCompress) {
        if (this.innerModel().contentTypesToCompress() != null) {
            this.innerModel().contentTypesToCompress().remove(contentTypeToCompress);
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withCompressionEnabled(boolean compressionEnabled) {
        this.innerModel().withIsCompressionEnabled(compressionEnabled);
        return this;
    }

    @Override
    public CdnEndpointImpl withQueryStringCachingBehavior(QueryStringCachingBehavior cachingBehavior) {
        this.innerModel().withQueryStringCachingBehavior(cachingBehavior);
        return this;
    }

    @Override
    public CdnEndpointImpl withGeoFilters(Collection<GeoFilter> geoFilters) {
        List<GeoFilter> list = null;
        if (geoFilters != null) {
            list = new ArrayList<>(geoFilters);
        }

        this.innerModel().withGeoFilters(list);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutGeoFilters() {
        if (this.innerModel().geoFilters() != null) {
            this.innerModel().geoFilters().clear();
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

        this.innerModel().geoFilters().add(geoFilter);
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

        this.innerModel().geoFilters().add(geoFilter);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutGeoFilter(String relativePath) {
        this.innerModel().geoFilters().removeIf(geoFilter -> geoFilter.relativePath().equals(relativePath));
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
        if (this.innerModel().geoFilters() == null) {
            this.innerModel().withGeoFilters(new ArrayList<>());
        }
        GeoFilter geoFilter = null;
        for (GeoFilter filter : this.innerModel().geoFilters()) {
            if (filter.relativePath().equals(relativePath)) {
                geoFilter = filter;
                break;
            }
        }
        if (geoFilter == null) {
            geoFilter = new GeoFilter();
        } else {
            this.innerModel().geoFilters().remove(geoFilter);
        }
        geoFilter.withRelativePath(relativePath)
                .withAction(action);

        return geoFilter;
    }
}
