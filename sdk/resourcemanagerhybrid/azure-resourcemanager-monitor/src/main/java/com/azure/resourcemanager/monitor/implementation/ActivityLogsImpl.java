// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.ActivityLogs;
import com.azure.resourcemanager.monitor.models.EventData;
import com.azure.resourcemanager.monitor.models.EventDataPropertyName;
import com.azure.resourcemanager.monitor.models.LocalizableString;
import com.azure.resourcemanager.monitor.fluent.ActivityLogsClient;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for {@link ActivityLogs}. */
public class ActivityLogsImpl implements ActivityLogs, ActivityLogs.ActivityLogsQueryDefinition {

    private final MonitorManager myManager;
    private OffsetDateTime queryStartTime = null;
    private OffsetDateTime queryEndTime = null;
    private final TreeSet<String> responsePropertySelector;
    private String filterString;
    private boolean filterForTenant;

    public ActivityLogsImpl(final MonitorManager monitorManager) {
        this.myManager = monitorManager;
        this.responsePropertySelector = new TreeSet<>();
        this.filterString = "";
        this.filterForTenant = false;
    }

    @Override
    public MonitorManager manager() {
        return this.myManager;
    }

    public ActivityLogsClient inner() {
        return this.myManager.serviceClient().getActivityLogs();
    }

    @Override
    public PagedIterable<LocalizableString> listEventCategories() {
        return PagedConverter.mapPage(this.manager().serviceClient().getEventCategories().list(), LocalizableStringImpl::new);
    }

    @Override
    public PagedFlux<LocalizableString> listEventCategoriesAsync() {
        return PagedConverter.mapPage(this.manager().serviceClient().getEventCategories().listAsync(), LocalizableStringImpl::new);
    }

    @Override
    public ActivityLogsQueryDefinitionStages.WithEventDataStartTimeFilter defineQuery() {
        this.responsePropertySelector.clear();
        this.filterString = "";
        this.filterForTenant = false;
        return this;
    }

    @Override
    public ActivityLogsImpl startingFrom(OffsetDateTime startTime) {
        this.queryStartTime = startTime;
        return this;
    }

    @Override
    public ActivityLogsImpl endsBefore(OffsetDateTime endTime) {
        this.queryEndTime = endTime;
        return this;
    }

    @Override
    public ActivityLogsImpl withAllPropertiesInResponse() {
        this.responsePropertySelector.clear();
        return this;
    }

    @Override
    public ActivityLogsImpl withResponseProperties(EventDataPropertyName... responseProperties) {
        this.responsePropertySelector.clear();

        this
            .responsePropertySelector
            .addAll(
                Arrays.stream(responseProperties).map(EventDataPropertyName::toString).collect(Collectors.toList()));
        return this;
    }

    @Override
    public ActivityLogsImpl filterByResourceGroup(String resourceGroupName) {
        this.filterString = String.format(" and resourceGroupName eq '%s'", resourceGroupName);
        return this;
    }

    @Override
    public ActivityLogsImpl filterByResource(String resourceId) {
        this.filterString = String.format(" and resourceUri eq '%s'", resourceId);
        return this;
    }

    @Override
    public ActivityLogsImpl filterByResourceProvider(String resourceProviderName) {
        this.filterString = String.format(" and resourceProvider eq '%s'", resourceProviderName);
        return this;
    }

    @Override
    public ActivityLogsImpl filterByCorrelationId(String correlationId) {
        this.filterString = String.format(" and correlationId eq '%s'", correlationId);
        return this;
    }

    @Override
    public ActivityLogsImpl filterAtTenantLevel() {
        this.filterForTenant = true;
        return this;
    }

    @Override
    public PagedIterable<EventData> execute() {
        if (this.filterForTenant) {
            return listEventDataForTenant(
                getOdataFilterString() + this.filterString + " eventChannels eq 'Admin, Operation'");
        }
        return listEventData(getOdataFilterString() + this.filterString);
    }

    @Override
    public PagedFlux<EventData> executeAsync() {
        if (this.filterForTenant) {
            return listEventDataForTenantAsync(
                getOdataFilterString() + this.filterString + " eventChannels eq 'Admin, Operation'");
        }
        return listEventDataAsync(getOdataFilterString() + this.filterString);
    }

    private String getOdataFilterString() {
        return String
            .format(
                "eventTimestamp ge '%s' and eventTimestamp le '%s'",
                DateTimeFormatter.ISO_INSTANT.format(this.queryStartTime.atZoneSameInstant(ZoneOffset.UTC)),
                DateTimeFormatter.ISO_INSTANT.format(this.queryEndTime.atZoneSameInstant(ZoneOffset.UTC)));
    }

    private PagedIterable<EventData> listEventData(String filter) {
        return PagedConverter.mapPage(this.inner().list(filter, createPropertyFilter(), Context.NONE), EventDataImpl::new);
    }

    private PagedIterable<EventData> listEventDataForTenant(String filter) {
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getTenantActivityLogs()
            .list(filter, createPropertyFilter(), Context.NONE),
            EventDataImpl::new);
    }

    private PagedFlux<EventData> listEventDataAsync(String filter) {
        return PagedConverter.mapPage(this.inner().listAsync(filter, createPropertyFilter()), EventDataImpl::new);
    }

    private PagedFlux<EventData> listEventDataForTenantAsync(String filter) {
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getTenantActivityLogs()
            .listAsync(filter, createPropertyFilter()),
            EventDataImpl::new);
    }

    private String createPropertyFilter() {
        String propertyFilter =
            this.responsePropertySelector == null ? null : String.join(",", this.responsePropertySelector);
        if (propertyFilter != null && propertyFilter.trim().isEmpty()) {
            propertyFilter = null;
        }
        return propertyFilter;
    }
}
