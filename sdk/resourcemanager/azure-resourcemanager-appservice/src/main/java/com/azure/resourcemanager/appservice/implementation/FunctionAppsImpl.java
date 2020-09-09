// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.WebAppsClient;
import com.azure.resourcemanager.appservice.fluent.inner.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionApps;
import com.azure.resourcemanager.appservice.models.FunctionEnvelope;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/** The implementation for WebApps. */
public class FunctionAppsImpl
    extends TopLevelModifiableResourcesImpl<FunctionApp, FunctionAppImpl, SiteInner, WebAppsClient, AppServiceManager>
    implements FunctionApps {

    public FunctionAppsImpl(final AppServiceManager manager) {
        super(manager.inner().getWebApps(), manager);
    }

    @Override
    public FunctionApp getByResourceGroup(String groupName, String name) {
        SiteInner siteInner = this.inner().getByResourceGroup(groupName, name);
        if (siteInner == null) {
            return null;
        }
        return wrapModel(
            siteInner,
            this.inner().getConfiguration(groupName, name),
            this.inner().getDiagnosticLogsConfiguration(groupName, name));
    }

    @Override
    public Mono<FunctionApp> getByResourceGroupAsync(final String groupName, final String name) {
        final FunctionAppsImpl self = this;
        return this
            .inner()
            .getByResourceGroupAsync(groupName, name)
            .flatMap(
                siteInner ->
                    Mono
                        .zip(
                            self.inner().getConfigurationAsync(groupName, name),
                            self.inner().getDiagnosticLogsConfigurationAsync(groupName, name),
                            (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) ->
                                wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    public PagedIterable<FunctionEnvelope> listFunctions(String resourceGroupName, String name) {
        return this
            .manager()
            .webApps()
            .inner()
            .listFunctions(resourceGroupName, name)
            .mapPage(FunctionEnvelopeImpl::new);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    protected FunctionAppImpl wrapModel(String name) {
        return new FunctionAppImpl(name, new SiteInner().withKind("functionapp"), null, null, this.manager());
    }

    @Override
    protected FunctionAppImpl wrapModel(SiteInner inner) {
        if (inner == null) {
            return null;
        }
        return wrapModel(inner, null, null);
    }

    private FunctionAppImpl wrapModel(
        SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new FunctionAppImpl(inner.name(), inner, siteConfig, logConfig, this.manager());
    }

    @Override
    protected PagedFlux<FunctionApp> wrapPageAsync(PagedFlux<SiteInner> innerPage) {
        return PagedConverter
            .flatMapPage(
                innerPage,
                siteInner -> {
                    if (siteInner.kind() != null
                        && Arrays.asList(siteInner.kind().split(",")).contains("functionapp")) {
                        return Mono
                            .zip(
                                this.inner().getConfigurationAsync(siteInner.resourceGroup(), siteInner.name()),
                                this
                                    .inner()
                                    .getDiagnosticLogsConfigurationAsync(
                                        siteInner.resourceGroup(), siteInner.name()),
                                (siteConfigResourceInner, logsConfigInner) ->
                                    this.wrapModel(siteInner, siteConfigResourceInner, logsConfigInner));
                    } else {
                        return Mono.empty();
                    }
                });
    }

    @Override
    public FunctionAppImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }
}
