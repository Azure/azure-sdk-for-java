// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.DiagnosticSettingsOperationsClient;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsCategoryResourceInner;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsResourceInner;
import com.azure.resourcemanager.monitor.models.DiagnosticSetting;
import com.azure.resourcemanager.monitor.models.DiagnosticSettings;
import com.azure.resourcemanager.monitor.models.DiagnosticSettingsCategory;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.exception.AggregatedManagementException;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Implementation for DiagnosticSettings. */
public class DiagnosticSettingsImpl
    extends CreatableResourcesImpl<DiagnosticSetting, DiagnosticSettingImpl, DiagnosticSettingsResourceInner>
    implements DiagnosticSettings {

    private final ClientLogger logger = new ClientLogger(getClass());

    private final MonitorManager manager;

    public DiagnosticSettingsImpl(final MonitorManager manager) {
        this.manager = manager;
    }

    @Override
    public DiagnosticSettingImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected DiagnosticSettingImpl wrapModel(String name) {
        DiagnosticSettingsResourceInner inner = new DiagnosticSettingsResourceInner();

        return new DiagnosticSettingImpl(name, inner, this.manager());
    }

    @Override
    protected DiagnosticSettingImpl wrapModel(DiagnosticSettingsResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new DiagnosticSettingImpl(inner.name(), inner, this.manager());
    }

    @Override
    public MonitorManager manager() {
        return this.manager;
    }

    public DiagnosticSettingsOperationsClient inner() {
        return this.manager().serviceClient().getDiagnosticSettingsOperations();
    }

    @Override
    public List<DiagnosticSettingsCategory> listCategoriesByResource(String resourceId) {
        List<DiagnosticSettingsCategory> categories = new ArrayList<>();
        PagedIterable<DiagnosticSettingsCategoryResourceInner> collection = this.manager()
            .serviceClient()
            .getDiagnosticSettingsCategories()
            .list(ResourceUtils.encodeResourceId(resourceId));
        if (collection != null) {
            for (DiagnosticSettingsCategoryResourceInner category : collection) {
                categories.add(new DiagnosticSettingsCategoryImpl(category));
            }
        }
        return categories;
    }

    @Override
    public PagedFlux<DiagnosticSettingsCategory> listCategoriesByResourceAsync(String resourceId) {
        return PagedConverter.mapPage(this.manager.serviceClient()
            .getDiagnosticSettingsCategories()
            .listAsync(ResourceUtils.encodeResourceId(resourceId)), DiagnosticSettingsCategoryImpl::new);
    }

    @Override
    public DiagnosticSettingsCategory getCategory(String resourceId, String name) {
        return new DiagnosticSettingsCategoryImpl(this.manager()
            .serviceClient()
            .getDiagnosticSettingsCategories()
            .get(ResourceUtils.encodeResourceId(resourceId), name));
    }

    @Override
    public Mono<DiagnosticSettingsCategory> getCategoryAsync(String resourceId, String name) {
        return this.manager()
            .serviceClient()
            .getDiagnosticSettingsCategories()
            .getAsync(ResourceUtils.encodeResourceId(resourceId), name)
            .map(DiagnosticSettingsCategoryImpl::new);
    }

    @Override
    public PagedIterable<DiagnosticSetting> listByResource(String resourceId) {
        return new PagedIterable<>(this.listByResourceAsync(resourceId));
    }

    @Override
    public PagedFlux<DiagnosticSetting> listByResourceAsync(String resourceId) {
        return PagedConverter.mapPage(
            this.manager()
                .serviceClient()
                .getDiagnosticSettingsOperations()
                .listAsync(ResourceUtils.encodeResourceId(resourceId)),
            inner -> new DiagnosticSettingImpl(inner.name(), inner, manager));
    }

    @Override
    public void delete(String resourceId, String name) {
        this.manager()
            .serviceClient()
            .getDiagnosticSettingsOperations()
            .delete(ResourceUtils.encodeResourceId(resourceId), name);
    }

    @Override
    public Mono<Void> deleteAsync(String resourceId, String name) {
        return this.manager()
            .serviceClient()
            .getDiagnosticSettingsOperations()
            .deleteAsync(ResourceUtils.encodeResourceId(resourceId), name);
    }

    @Override
    public DiagnosticSetting get(String resourceId, String name) {
        return wrapModel(this.manager()
            .serviceClient()
            .getDiagnosticSettingsOperations()
            .get(ResourceUtils.encodeResourceId(resourceId), name));
    }

    @Override
    public Mono<DiagnosticSetting> getAsync(String resourceId, String name) {
        return this.manager()
            .serviceClient()
            .getDiagnosticSettingsOperations()
            .getAsync(ResourceUtils.encodeResourceId(resourceId), name)
            .map(this::wrapModel);
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return this.manager()
            .serviceClient()
            .getDiagnosticSettingsOperations()
            .deleteAsync(getResourceIdFromSettingsId(id), getNameFromSettingsId(id));
    }

    @Override
    public Flux<String> deleteByIdsAsync(Collection<String> ids) {
        if (CoreUtils.isNullOrEmpty(ids)) {
            return Flux.empty();
        }
        return Flux.fromIterable(ids)
            .flatMapDelayError(
                id -> deleteAsync(getResourceIdFromSettingsId(id), getNameFromSettingsId(id)).then(Mono.just(id)), 32,
                32)
            .onErrorMap(AggregatedManagementException::convertToManagementException)
            .subscribeOn(ResourceManagerUtils.InternalRuntimeContext.getReactorScheduler());
    }

    @Override
    public Flux<String> deleteByIdsAsync(String... ids) {
        return this.deleteByIdsAsync(new ArrayList<>(Arrays.asList(ids)));
    }

    @Override
    public void deleteByIds(Collection<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.deleteByIdsAsync(ids).blockLast();
        }
    }

    @Override
    public void deleteByIds(String... ids) {
        this.deleteByIds(new ArrayList<>(Arrays.asList(ids)));
    }

    @Override
    public DiagnosticSetting getById(String id) {
        return wrapModel(this.inner().get(getResourceIdFromSettingsId(id), getNameFromSettingsId(id)));
    }

    @Override
    public Mono<DiagnosticSetting> getByIdAsync(String id) {
        return this.inner().getAsync(getResourceIdFromSettingsId(id), getNameFromSettingsId(id)).map(this::wrapModel);
    }

    /**
     * Get the resourceID from the diagnostic setting ID, with proper encoding.
     *
     * @param diagnosticSettingId ID of the diagnostic setting resource
     * @return properly encoded resourceID of the diagnostic setting
     */
    private String getResourceIdFromSettingsId(String diagnosticSettingId) {
        return getResourceIdFromSettingsId(diagnosticSettingId, true);
    }

    /**
     * Get the resourceID from the diagnostic setting ID.
     *
     * @param diagnosticSettingId ID of the diagnostic setting resource
     * @param encodeResourceId whether to ensure the resourceID is properly encoded
     * @return resourceID of the diagnostic setting
     */
    private String getResourceIdFromSettingsId(String diagnosticSettingId, boolean encodeResourceId) {
        if (diagnosticSettingId == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'resourceId' is required and cannot be null."));
        }
        if (encodeResourceId) {
            diagnosticSettingId = ResourceUtils.encodeResourceId(diagnosticSettingId);
        }
        int dsIdx = diagnosticSettingId.lastIndexOf(DiagnosticSettingImpl.DIAGNOSTIC_SETTINGS_URI);
        if (dsIdx == -1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Parameter 'resourceId' does not represent a valid Diagnostic Settings resource Id ["
                    + diagnosticSettingId + "]."));
        }

        return diagnosticSettingId.substring(0, dsIdx);
    }

    /**
     * Get raw diagnostic setting name from id.
     *
     * @param diagnosticSettingId ID of the diagnostic settting
     * @return raw name of the diagnostic setting
     */
    private String getNameFromSettingsId(String diagnosticSettingId) {
        String resourceId = getResourceIdFromSettingsId(diagnosticSettingId, false);
        return diagnosticSettingId
            .substring(resourceId.length() + DiagnosticSettingImpl.DIAGNOSTIC_SETTINGS_URI.length());
    }
}
