/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.management.appservice.WebApp;
import com.azure.management.appservice.WebApps;
import com.azure.management.appservice.models.SiteConfigResourceInner;
import com.azure.management.appservice.models.SiteInner;
import com.azure.management.appservice.models.SiteLogsConfigInner;
import com.azure.management.appservice.models.WebAppsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * The implementation for WebApps.
 */
class WebAppsImpl
        extends TopLevelModifiableResourcesImpl<
                    WebApp,
                    WebAppImpl,
                    SiteInner,
                    WebAppsInner,
                    AppServiceManager>
        implements WebApps {

    WebAppsImpl(final AppServiceManager manager) {
        super(manager.inner().webApps(), manager);
    }

    @Override
    public Mono<WebApp> getByResourceGroupAsync(final String groupName, final String name) {
        final WebAppsImpl self = this;
        return this.inner().getByResourceGroupAsync(groupName, name).flatMap(siteInner -> Mono.zip(
                self.inner().getConfigurationAsync(groupName, name),
                self.inner().getDiagnosticLogsConfigurationAsync(groupName, name),
                (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) -> wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    protected WebAppImpl wrapModel(String name) {
        return new WebAppImpl(name, new SiteInner().withKind("app"), null, null, this.manager());
    }

    protected WebAppImpl wrapModel(SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new WebAppImpl(inner.getName(), inner, siteConfig, logConfig, this.manager());
    }

    @Override
    protected WebAppImpl wrapModel(SiteInner inner) {
        return wrapModel(inner, null, null);
    }

    @Override
    protected PagedFlux<WebApp> wrapPageAsync(PagedFlux<SiteInner> innerPage) {
        return PagedConverter.flatMapPage(innerPage, siteInner -> {
            if (siteInner.kind() == null || Arrays.asList(siteInner.kind().split(",")).contains("app")) {
                return Mono.zip(
                        this.inner().getConfigurationAsync(siteInner.resourceGroup(), siteInner.getName()),
                        this.inner().getDiagnosticLogsConfigurationAsync(siteInner.resourceGroup(), siteInner.getName()),
                        (siteConfigResourceInner, logsConfigInner) -> this.wrapModel(siteInner, siteConfigResourceInner, logsConfigInner));
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
