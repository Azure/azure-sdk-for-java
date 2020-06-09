// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.WebAppsClient;
import com.azure.resourcemanager.appservice.fluent.inner.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebApps;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/** The implementation for WebApps. */
public class WebAppsImpl
    extends TopLevelModifiableResourcesImpl<WebApp, WebAppImpl, SiteInner, WebAppsClient, AppServiceManager>
    implements WebApps {

    public WebAppsImpl(final AppServiceManager manager) {
        super(manager.inner().getWebApps(), manager);
    }

    @Override
    public Mono<WebApp> getByResourceGroupAsync(final String groupName, final String name) {
        final WebAppsImpl self = this;
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
    protected WebAppImpl wrapModel(String name) {
        return new WebAppImpl(name, new SiteInner().withKind("app"), null, null, this.manager());
    }

    protected WebAppImpl wrapModel(SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new WebAppImpl(inner.name(), inner, siteConfig, logConfig, this.manager());
    }

    @Override
    protected WebAppImpl wrapModel(SiteInner inner) {
        return wrapModel(inner, null, null);
    }

    @Override
    protected PagedFlux<WebApp> wrapPageAsync(PagedFlux<SiteInner> innerPage) {
        return PagedConverter
            .flatMapPage(
                innerPage,
                siteInner -> {
                    if (siteInner.kind() == null || Arrays.asList(siteInner.kind().split(",")).contains("app")) {
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
    public WebAppImpl define(String name) {
        return wrapModel(name);
    }
}
