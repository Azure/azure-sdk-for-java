/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.appservice.FunctionApp;
import com.azure.management.appservice.FunctionApps;
import com.azure.management.appservice.FunctionEnvelope;
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
class FunctionAppsImpl
        extends TopLevelModifiableResourcesImpl<
                    FunctionApp,
                    FunctionAppImpl,
                    SiteInner,
                    WebAppsInner,
                    AppServiceManager>
        implements FunctionApps {

    FunctionAppsImpl(final AppServiceManager manager) {
        super(manager.inner().webApps(), manager);
    }

    @Override
    public FunctionApp getByResourceGroup(String groupName, String name) {
        SiteInner siteInner = this.inner().getByResourceGroup(groupName, name);
        if (siteInner == null) {
            return null;
        }
        return wrapModel(siteInner, this.inner().getConfiguration(groupName, name), this.inner().getDiagnosticLogsConfiguration(groupName, name));
    }

    @Override
    public Mono<FunctionApp> getByResourceGroupAsync(final String groupName, final String name) {
        final FunctionAppsImpl self = this;
        return this.inner().getByResourceGroupAsync(groupName, name).flatMap(siteInner -> Mono.zip(
                self.inner().getConfigurationAsync(groupName, name),
                self.inner().getDiagnosticLogsConfigurationAsync(groupName, name),
                (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) -> wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    public PagedIterable<FunctionEnvelope> listFunctions(String resourceGroupName, String name) {
        return this.manager().webApps().inner().listFunctions(resourceGroupName, name).mapPage(FunctionEnvelopeImpl::new);
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

    private FunctionAppImpl wrapModel(SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new FunctionAppImpl(inner.getName(), inner, siteConfig, logConfig, this.manager());
    }

    @Override
    protected PagedFlux<FunctionApp> wrapPageAsync(PagedFlux<SiteInner> innerPage) {
        return PagedConverter.flatMapPage(innerPage, siteInner -> {
            if (siteInner.kind() != null && Arrays.asList(siteInner.kind().split(",")).contains("functionapp")) {
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
    public FunctionAppImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }
}
