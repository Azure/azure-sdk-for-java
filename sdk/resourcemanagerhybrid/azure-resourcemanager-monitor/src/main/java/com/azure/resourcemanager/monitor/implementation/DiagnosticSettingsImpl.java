// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.DiagnosticSetting;
import com.azure.resourcemanager.monitor.models.DiagnosticSettings;
import com.azure.resourcemanager.monitor.models.DiagnosticSettingsCategory;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsCategoryResourceCollectionInner;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsCategoryResourceInner;
import com.azure.resourcemanager.monitor.fluent.DiagnosticSettingsClient;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.BatchDeletionImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public DiagnosticSettingsClient inner() {
        return this.manager().serviceClient().getDiagnosticSettings();
    }

    @Override
    public List<DiagnosticSettingsCategory> listCategoriesByResource(String resourceId) {
        List<DiagnosticSettingsCategory> categories = new ArrayList<>();
        DiagnosticSettingsCategoryResourceCollectionInner collection =
            this.manager().serviceClient().getDiagnosticSettingsCategories().list(resourceId);
        if (collection != null) {
            for (DiagnosticSettingsCategoryResourceInner category : collection.value()) {
                categories.add(new DiagnosticSettingsCategoryImpl(category));
            }
        }
        return categories;
    }

    @Override
    public PagedFlux<DiagnosticSettingsCategory> listCategoriesByResourceAsync(String resourceId) {
        return PagedConverter.mapPage(PagedConverter
            .convertListToPagedFlux(
                this
                    .manager
                    .serviceClient()
                    .getDiagnosticSettingsCategories()
                    .listWithResponseAsync(resourceId)
                    .map(r -> new SimpleResponse<>(r.getRequest(), r.getStatusCode(), r.getHeaders(),
                        r.getValue().value() == null ? Collections.emptyList() : r.getValue().value()))),
            DiagnosticSettingsCategoryImpl::new);
    }

    @Override
    public DiagnosticSettingsCategory getCategory(String resourceId, String name) {
        return new DiagnosticSettingsCategoryImpl(
            this.manager().serviceClient().getDiagnosticSettingsCategories().get(resourceId, name));
    }

    @Override
    public Mono<DiagnosticSettingsCategory> getCategoryAsync(String resourceId, String name) {
        return this
            .manager()
            .serviceClient()
            .getDiagnosticSettingsCategories()
            .getAsync(resourceId, name)
            .map(DiagnosticSettingsCategoryImpl::new);
    }

    @Override
    public PagedIterable<DiagnosticSetting> listByResource(String resourceId) {
        return new PagedIterable<>(this.listByResourceAsync(resourceId));
    }

    @Override
    public PagedFlux<DiagnosticSetting> listByResourceAsync(String resourceId) {
        return PagedConverter.mapPage(PagedConverter
            .convertListToPagedFlux(
                this
                    .manager()
                    .serviceClient()
                    .getDiagnosticSettings()
                    .listWithResponseAsync(resourceId)
                    .map(r -> new SimpleResponse<>(r.getRequest(), r.getStatusCode(), r.getHeaders(),
                        r.getValue().value() == null ? Collections.emptyList() : r.getValue().value()))),
            inner -> new DiagnosticSettingImpl(inner.name(), inner, this.manager()));
    }

    @Override
    public void delete(String resourceId, String name) {
        this.manager().serviceClient().getDiagnosticSettings().delete(resourceId, name);
    }

    @Override
    public Mono<Void> deleteAsync(String resourceId, String name) {
        return this.manager().serviceClient().getDiagnosticSettings().deleteAsync(resourceId, name);
    }

    @Override
    public DiagnosticSetting get(String resourceId, String name) {
        return wrapModel(this.manager().serviceClient().getDiagnosticSettings().get(resourceId, name));
    }

    @Override
    public Mono<DiagnosticSetting> getAsync(String resourceId, String name) {
        return this.manager().serviceClient().getDiagnosticSettings().getAsync(resourceId, name).map(this::wrapModel);
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return this
            .manager()
            .serviceClient()
            .getDiagnosticSettings()
            .deleteAsync(getResourceIdFromSettingsId(id), getNameFromSettingsId(id));
    }

    @Override
    public Flux<String> deleteByIdsAsync(Collection<String> ids) {
        return BatchDeletionImpl.deleteByIdsAsync(ids, (rgName, name) -> this.inner().deleteAsync(rgName, name));
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

    private String getResourceIdFromSettingsId(String diagnosticSettingId) {
        if (diagnosticSettingId == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'resourceId' is required and cannot be null."));
        }
        int dsIdx = diagnosticSettingId.lastIndexOf(DiagnosticSettingImpl.DIAGNOSTIC_SETTINGS_URI);
        if (dsIdx == -1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Parameter 'resourceId' does not represent a valid Diagnostic Settings resource Id ["
                    + diagnosticSettingId
                    + "]."));
        }

        return diagnosticSettingId.substring(0, dsIdx);
    }

    private String getNameFromSettingsId(String diagnosticSettingId) {
        String resourceId = getResourceIdFromSettingsId(diagnosticSettingId);
        return diagnosticSettingId
            .substring(resourceId.length() + DiagnosticSettingImpl.DIAGNOSTIC_SETTINGS_URI.length());
    }
}
