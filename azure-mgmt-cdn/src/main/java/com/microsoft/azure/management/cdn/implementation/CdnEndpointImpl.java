/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CustomDomainValidationResult;
import com.microsoft.azure.management.cdn.DeepCreatedOrigin;
import com.microsoft.azure.management.cdn.EndpointResourceState;
import com.microsoft.azure.management.cdn.GeoFilter;
import com.microsoft.azure.management.cdn.GeoFilterActions;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.functions.FuncN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation for {@link CdnEndpoint}.
 */
@LangDefinition
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
        CdnEndpoint.UpdatePremiumEndpoint {

    private final EndpointsInner client;
    private final OriginsInner originsClient;
    private final CustomDomainsInner customDomainsClient;
    private List<CustomDomainInner> customDomainList;
    private List<CustomDomainInner> deletedCustomDomainList;

    CdnEndpointImpl(String name,
                    CdnProfileImpl parent,
                    EndpointInner inner,
                    EndpointsInner client,
                    OriginsInner originsClient,
                    CustomDomainsInner customDomainsClient) {
        super(name, parent, inner);
        this.client = client;
        this.originsClient = originsClient;
        this.customDomainsClient = customDomainsClient;
        this.customDomainList = new ArrayList<CustomDomainInner>();
        this.deletedCustomDomainList = new ArrayList<CustomDomainInner>();
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
                        for (CustomDomainInner itemToCreate : self.customDomainList) {
                            self.customDomainsClient.create(
                                    self.parent().resourceGroupName(),
                                    self.parent().name(),
                                    self.name(),
                                    ResourceNamer.randomResourceName("CustomDomain", 50),
                                    itemToCreate.hostName());
                        }
                        self.customDomainList.clear();
                        self.customDomainList.addAll(self.customDomainsClient.listByEndpoint(
                                self.parent().resourceGroupName(),
                                self.parent().name(),
                                self.name()));
                        return self;
                    }
                });
    }

    @Override
    public Observable<CdnEndpoint> updateAsync() {
        final CdnEndpointImpl self = this;
        EndpointUpdateParametersInner updateInner = new EndpointUpdateParametersInner();
        updateInner.withIsHttpAllowed(this.inner().isHttpAllowed())
                .withIsHttpsAllowed(this.inner().isHttpsAllowed())
                .withOriginPath(this.inner().originPath())
                .withOriginHostHeader(this.inner().originHostHeader())
                .withIsCompressionEnabled(this.inner().isCompressionEnabled())
                .withContentTypesToCompress(this.inner().contentTypesToCompress())
                .withGeoFilters(this.inner().geoFilters())
                .withOptimizationType(this.inner().optimizationType())
                .withQueryStringCachingBehavior(this.inner().queryStringCachingBehavior())
                .withTags(this.inner().getTags());

        DeepCreatedOrigin originInner = this.inner().origins().get(0);
        OriginUpdateParametersInner originParameters = new OriginUpdateParametersInner()
                .withHostName(originInner.hostName())
                .withHttpPort(originInner.httpPort())
                .withHttpsPort(originInner.httpsPort());

        Observable<OriginInner> originObservable = this.originsClient.updateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                originInner.name(),
                originParameters);

        Observable<CdnEndpoint> endpointObservable = this.client.updateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                updateInner)
                .map(new Func1<EndpointInner, CdnEndpoint>() {
                    @Override
                    public CdnEndpoint call(EndpointInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                });

        List<Observable<CustomDomainInner>> customDomainDeleteObservables = new ArrayList<>();

        for (CustomDomainInner itemToDelete : this.deletedCustomDomainList) {
            customDomainDeleteObservables.add(this.customDomainsClient.deleteAsync(
                    this.parent().resourceGroupName(),
                    this.parent().name(),
                    this.name(),
                    itemToDelete.name()));
        }
        Observable<CustomDomainInner> deleteObservable = Observable.zip(customDomainDeleteObservables, new FuncN<CustomDomainInner>() {
            @Override
            public CustomDomainInner call(Object... objects) {
                return null;
            }
        });

        return Observable.zip(
                originObservable,
                endpointObservable,
                deleteObservable,
                new Func3<OriginInner, CdnEndpoint, CustomDomainInner, CdnEndpoint>() {
            @Override
            public CdnEndpoint call(OriginInner originInner, CdnEndpoint cdnEndpoint, CustomDomainInner customDomain) {
                return cdnEndpoint;
            }
        }).doOnNext(new Action1<CdnEndpoint>() {
            @Override
            public void call(CdnEndpoint cdnEndpoint) {
                self.deletedCustomDomainList.clear();
            }
        });
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
        this.customDomainList.clear();
        this.deletedCustomDomainList.clear();
        this.customDomainList.addAll(this.customDomainsClient.listByEndpoint(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name()));
        return this;
    }

    @Override
    public CdnProfileImpl attach() {
        return this.parent().withEndpoint(this);
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
    public List<String> contentTypesToCompress() {
        return this.inner().contentTypesToCompress();
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
        return this.inner().optimizationType();
    }

    @Override
    public List<GeoFilter> geoFilters() {
        return this.inner().geoFilters();
    }

    @Override
    public String hostName() {
        return this.inner().hostName();
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
            return this.inner().origins().get(0).hostName();
        }
        return null;
    }

    @Override
    public int httpPort() {
        if (this.inner().origins() != null && !this.inner().origins().isEmpty()) {
            return this.inner().origins().get(0).httpPort();
        }
        return 0;
    }

    @Override
    public int httpsPort() {
        if (this.inner().origins() != null && !this.inner().origins().isEmpty()) {
            return this.inner().origins().get(0).httpsPort();
        }
        return 0;
    }

    @Override
    public List<String> customDomains() {
        return Collections.unmodifiableList(
                Lists.transform(this.customDomainList,
                        new Function<CustomDomainInner, String>() {
                    public String apply(CustomDomainInner customDomain) {
                        return customDomain.hostName();
                    }
                }));
    }

    @Override
    public void start() {
        this.parent().startEndpoint(this.name());
    }

    @Override
    public void stop() {
        this.parent().stopEndpoint(this.name());
    }

    @Override
    public void purgeContent(List<String> contentPaths) {
        this.parent().purgeEndpointContent(this.name(), contentPaths);
    }

    @Override
    public void loadContent(List<String> contentPaths) {
        this.parent().loadEndpointContent(this.name(), contentPaths);
    }

    @Override
    public CustomDomainValidationResult validateCustomDomain(String hostName) {
        return this.parent().validateEndpointCustomDomain(this.name(), hostName);
    }

    @Override
    public CdnEndpointImpl withOrigin(String originName, String hostname) {
        this.inner().origins().add(
                new DeepCreatedOrigin()
                        .withName(originName)
                        .withHostName(hostname));
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
    public CdnEndpointImpl withContentTypesToCompress(List<String> contentTypesToCompress) {
        this.inner().withContentTypesToCompress(contentTypesToCompress);
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
            this.inner().withContentTypesToCompress(new ArrayList<String>());
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
    public CdnEndpointImpl withGeoFilters(List<GeoFilter> geoFilters) {
        this.inner().withGeoFilters(geoFilters);
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
    public CdnEndpointImpl withGeoFilter(String relativePath, GeoFilterActions action, CountryISOCode countryCode) {
        GeoFilter geoFilter = this.createGeoFiltersObject(relativePath, action);

        if (geoFilter.countryCodes() == null) {
            geoFilter.withCountryCodes(new ArrayList<String>());
        }
        geoFilter.countryCodes().add(countryCode.toString());

        this.inner().geoFilters().add(geoFilter);
        return this;
    }

    @Override
    public CdnEndpointImpl withGeoFilter(String relativePath, GeoFilterActions action, List<CountryISOCode> countryCodes) {
        GeoFilter geoFilter = this.createGeoFiltersObject(relativePath, action);

        if (geoFilter.countryCodes() == null) {
            geoFilter.withCountryCodes(new ArrayList<String>());
        } else {
            geoFilter.countryCodes().clear();
        }

        for (CountryISOCode countryCode : countryCodes) {
            geoFilter.countryCodes().add(countryCode.toString());
        }

        this.inner().geoFilters().add(geoFilter);
        return this;
    }

    @Override
    public CdnEndpointImpl withoutGeoFilter(String relativePath) {
        for (Iterator<GeoFilter> iter = this.inner().geoFilters().listIterator(); iter.hasNext();) {
            GeoFilter geoFilter = iter.next();
            if (geoFilter.relativePath().equals(relativePath)) {
                iter.remove();
            }
        }
        return this;
    }

    @Override
    public CdnEndpointImpl withCustomDomain(String hostName) {
        if (this.customDomainList == null) {
            this.customDomainList = new ArrayList<CustomDomainInner>();
        }
        this.customDomainList.add(new CustomDomainInner().withHostName(hostName));
        return this;
    }

    @Override
    public CdnEndpointImpl withoutCustomDomain(String hostName) {
        for (Iterator<CustomDomainInner> iter = this.customDomainList.listIterator(); iter.hasNext();) {
            CustomDomainInner customDomain = iter.next();
            if (hostName.equals(customDomain.hostName())) {
                iter.remove();
                deletedCustomDomainList.add(customDomain);
            }
        }
        return this;
    }

    private GeoFilter createGeoFiltersObject(String relativePath, GeoFilterActions action) {
        if (this.inner().geoFilters() == null) {
            this.inner().withGeoFilters(new ArrayList<GeoFilter>());
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
        }
        else {
            this.inner().geoFilters().remove(geoFilter);
        }
        geoFilter.withRelativePath(relativePath)
                .withAction(action);

        return geoFilter;
    }
}
