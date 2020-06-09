// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.DiagnosticSettingsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.List;
import reactor.core.publisher.Mono;

/** Entry point for diagnostic settings management API. */
@Fluent
public interface DiagnosticSettings
    extends SupportsCreating<DiagnosticSetting.DefinitionStages.Blank>,
        SupportsBatchCreation<DiagnosticSetting>,
        SupportsGettingById<DiagnosticSetting>,
        SupportsDeletingById,
        SupportsBatchDeletion,
        HasManager<MonitorManager>,
        HasInner<DiagnosticSettingsClient> {

    /**
     * Lists all the Diagnostic Settings categories for Log and Metric Settings for a specific resource.
     *
     * @param resourceId of the requested resource.
     * @return list of Diagnostic Settings category available for the resource.
     */
    List<DiagnosticSettingsCategory> listCategoriesByResource(String resourceId);

    /**
     * Lists all the Diagnostic Settings categories for Log and Metric Settings for a specific resource.
     *
     * @param resourceId of the requested resource.
     * @return list of Diagnostic Settings category available for the resource.
     */
    PagedFlux<DiagnosticSettingsCategory> listCategoriesByResourceAsync(String resourceId);

    /**
     * Gets the information about Diagnostic Setting category for Log or Metric Setting for a specific resource.
     *
     * @param resourceId of the requested resource.
     * @param name of the Log or Metric category.
     * @return Diagnostic Setting category available for the resource.
     */
    DiagnosticSettingsCategory getCategory(String resourceId, String name);

    /**
     * Gets the information about Diagnostic Setting category for Log or Metric Setting for a specific resource.
     *
     * @param resourceId of the requested resource.
     * @param name of the Log or Metric category.
     * @return Diagnostic Setting category available for the resource.
     */
    Mono<DiagnosticSettingsCategory> getCategoryAsync(String resourceId, String name);

    /**
     * Lists all the diagnostic settings in the currently selected subscription for a specific resource.
     *
     * @param resourceId that Diagnostic Setting is associated with.
     * @return list of resources
     */
    PagedIterable<DiagnosticSetting> listByResource(String resourceId);

    /**
     * Lists all the diagnostic settings in the currently selected subscription for a specific resource.
     *
     * @param resourceId that Diagnostic Setting is associated with.
     * @return list of resources
     */
    PagedFlux<DiagnosticSetting> listByResourceAsync(String resourceId);

    /**
     * Deletes a Diagnostic Setting from Azure, identifying it by its resourceId and name.
     *
     * @param resourceId that Diagnostic Setting is associated with.
     * @param name the name of Diagnostic Setting.
     */
    void delete(String resourceId, String name);

    /**
     * Asynchronously delete a Diagnostic Setting from Azure, identifying it by its resourceId and name.
     *
     * @param resourceId that Diagnostic Setting is associated with.
     * @param name the name of Diagnostic Setting.
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync(String resourceId, String name);

    /**
     * Gets the information about Diagnostic Setting from Azure based on the resource id and setting name.
     *
     * @param resourceId that Diagnostic Setting is associated with.
     * @param name the name of Diagnostic Setting.
     * @return an immutable representation of the resource
     */
    DiagnosticSetting get(String resourceId, String name);

    /**
     * Gets the information about Diagnostic Setting from Azure based on the resource id and setting name.
     *
     * @param resourceId that Diagnostic Setting is associated with.
     * @param name the name of Diagnostic Setting.
     * @return an immutable representation of the resource
     */
    Mono<DiagnosticSetting> getAsync(String resourceId, String name);
}
