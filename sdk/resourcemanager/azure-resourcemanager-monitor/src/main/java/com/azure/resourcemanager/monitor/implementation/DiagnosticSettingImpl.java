// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.CategoryType;
import com.azure.resourcemanager.monitor.models.DiagnosticSetting;
import com.azure.resourcemanager.monitor.models.DiagnosticSettingsCategory;
import com.azure.resourcemanager.monitor.models.LogSettings;
import com.azure.resourcemanager.monitor.models.MetricSettings;
import com.azure.resourcemanager.monitor.models.RetentionPolicy;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsResourceInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** The Azure metric definition entries are of type DiagnosticSetting. */
class DiagnosticSettingImpl
    extends CreatableUpdatableImpl<DiagnosticSetting, DiagnosticSettingsResourceInner, DiagnosticSettingImpl>
    implements DiagnosticSetting, DiagnosticSetting.Definition, DiagnosticSetting.Update {

    private final ClientLogger logger = new ClientLogger(getClass());

    public static final String DIAGNOSTIC_SETTINGS_URI = "/providers/microsoft.insights/diagnosticSettings/";

    private String resourceId;
    private TreeMap<String, MetricSettings> metricSet;
    private TreeMap<String, LogSettings> logSet;
    private final MonitorManager myManager;

    DiagnosticSettingImpl(
        String name, DiagnosticSettingsResourceInner innerModel, final MonitorManager monitorManager) {
        super(name, innerModel);
        this.myManager = monitorManager;
        initializeSets();
    }

    @Override
    public DiagnosticSettingImpl withResource(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    @Override
    public DiagnosticSettingImpl withStorageAccount(String storageAccountId) {
        this.innerModel().withStorageAccountId(storageAccountId);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withLogAnalytics(String workspaceId) {
        this.innerModel().withWorkspaceId(workspaceId);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutLogAnalytics() {
        this.innerModel().withWorkspaceId(null);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutStorageAccount() {
        this.innerModel().withStorageAccountId(null);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withEventHub(String eventHubAuthorizationRuleId) {
        this.innerModel().withEventHubAuthorizationRuleId(eventHubAuthorizationRuleId);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withEventHub(String eventHubAuthorizationRuleId, String eventHubName) {
        this.withEventHub(eventHubAuthorizationRuleId);
        this.innerModel().withEventHubName(eventHubName);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutEventHub() {
        this.innerModel().withEventHubAuthorizationRuleId(null);
        this.innerModel().withEventHubName(null);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withMetric(String category, Duration timeGrain, int retentionDays) {
        MetricSettings nm = new MetricSettings();
        nm.withCategory(category);
        nm.withEnabled(true);
        nm.withRetentionPolicy(new RetentionPolicy());
        nm.retentionPolicy().withDays(retentionDays);
        if (retentionDays > 0) {
            nm.retentionPolicy().withEnabled(true);
        }
        nm.withTimeGrain(timeGrain);
        this.metricSet.put(category, nm);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withLog(String category, int retentionDays) {
        LogSettings nl = new LogSettings();
        nl.withCategory(category);
        nl.withEnabled(true);
        nl.withRetentionPolicy(new RetentionPolicy());
        nl.retentionPolicy().withDays(retentionDays);
        if (retentionDays > 0) {
            nl.retentionPolicy().withEnabled(true);
        }
        this.logSet.put(category, nl);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withLogsAndMetrics(
        List<DiagnosticSettingsCategory> categories, Duration timeGrain, int retentionDays) {
        for (DiagnosticSettingsCategory dsc : categories) {
            if (dsc.type() == CategoryType.METRICS) {
                this.withMetric(dsc.name(), timeGrain, retentionDays);
            } else if (dsc.type() == CategoryType.LOGS) {
                this.withLog(dsc.name(), retentionDays);
            } else {
                throw logger.logExceptionAsError(
                    new UnsupportedOperationException(dsc.type().toString() + " is unsupported."));
            }
        }
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutMetric(String category) {
        this.metricSet.remove(category);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutLog(String category) {
        this.logSet.remove(category);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutLogs() {
        this.logSet.clear();
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutMetrics() {
        this.metricSet.clear();
        return this;
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String resourceId() {
        return this.resourceId;
    }

    @Override
    public String storageAccountId() {
        return this.innerModel().storageAccountId();
    }

    @Override
    public String eventHubAuthorizationRuleId() {
        return this.innerModel().eventHubAuthorizationRuleId();
    }

    @Override
    public String eventHubName() {
        return this.innerModel().eventHubName();
    }

    @Override
    public List<MetricSettings> metrics() {
        if (this.innerModel().metrics() == null) {
            return null;
        }
        return Collections.unmodifiableList(this.innerModel().metrics());
    }

    @Override
    public List<LogSettings> logs() {
        if (this.innerModel().logs() == null) {
            return null;
        }
        return Collections.unmodifiableList(this.innerModel().logs());
    }

    @Override
    public String workspaceId() {
        return this.innerModel().workspaceId();
    }

    @Override
    public MonitorManager manager() {
        return this.myManager;
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public Mono<DiagnosticSetting> createResourceAsync() {
        this.innerModel().withLogs(new ArrayList<>(logSet.values()));
        this.innerModel().withMetrics(new ArrayList<>(metricSet.values()));
        return this
            .manager()
            .serviceClient()
            .getDiagnosticSettings()
            .createOrUpdateAsync(this.resourceId, this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<DiagnosticSettingsResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getDiagnosticSettings().getAsync(this.resourceId, this.name());
    }

    @Override
    public void setInner(DiagnosticSettingsResourceInner inner) {
        super.setInner(inner);
        initializeSets();
        this.metricSet.clear();
        this.logSet.clear();
        if (!isInCreateMode()) {
            this.resourceId =
                inner
                    .id()
                    .substring(
                        0,
                        this.innerModel().id().length()
                            - (DiagnosticSettingImpl.DIAGNOSTIC_SETTINGS_URI + this.innerModel().name()).length());
            for (MetricSettings ms : this.innerModel().metrics()) {
                this.metricSet.put(ms.category(), ms);
            }
            for (LogSettings ls : this.innerModel().logs()) {
                this.logSet.put(ls.category(), ls);
            }
        }
    }

    private void initializeSets() {
        if (this.metricSet == null) {
            this.metricSet = new TreeMap<>();
        }
        if (this.logSet == null) {
            this.logSet = new TreeMap<>();
        }
    }
}
