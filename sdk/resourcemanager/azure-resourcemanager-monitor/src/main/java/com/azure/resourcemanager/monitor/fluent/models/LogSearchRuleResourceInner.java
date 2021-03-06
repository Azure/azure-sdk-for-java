// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.monitor.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.Resource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.models.Action;
import com.azure.resourcemanager.monitor.models.Enabled;
import com.azure.resourcemanager.monitor.models.ProvisioningState;
import com.azure.resourcemanager.monitor.models.Schedule;
import com.azure.resourcemanager.monitor.models.Source;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;

/** The Log Search Rule resource. */
@JsonFlatten
@Fluent
public class LogSearchRuleResourceInner extends Resource {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(LogSearchRuleResourceInner.class);

    /*
     * The api-version used when creating this alert rule
     */
    @JsonProperty(value = "properties.createdWithApiVersion", access = JsonProperty.Access.WRITE_ONLY)
    private String createdWithApiVersion;

    /*
     * True if alert rule is legacy Log Analytic rule
     */
    @JsonProperty(value = "properties.isLegacyLogAnalyticsRule", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isLegacyLogAnalyticsRule;

    /*
     * The description of the Log Search rule.
     */
    @JsonProperty(value = "properties.description")
    private String description;

    /*
     * The display name of the alert rule
     */
    @JsonProperty(value = "properties.displayName")
    private String displayName;

    /*
     * The flag that indicates whether the alert should be automatically
     * resolved or not. The default is false.
     */
    @JsonProperty(value = "properties.autoMitigate")
    private Boolean autoMitigate;

    /*
     * The flag which indicates whether the Log Search rule is enabled. Value
     * should be true or false
     */
    @JsonProperty(value = "properties.enabled")
    private Enabled enabled;

    /*
     * Last time the rule was updated in IS08601 format.
     */
    @JsonProperty(value = "properties.lastUpdatedTime", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime lastUpdatedTime;

    /*
     * Provisioning state of the scheduled query rule
     */
    @JsonProperty(value = "properties.provisioningState", access = JsonProperty.Access.WRITE_ONLY)
    private ProvisioningState provisioningState;

    /*
     * Data Source against which rule will Query Data
     */
    @JsonProperty(value = "properties.source", required = true)
    private Source source;

    /*
     * Schedule (Frequency, Time Window) for rule. Required for action type -
     * AlertingAction
     */
    @JsonProperty(value = "properties.schedule")
    private Schedule schedule;

    /*
     * Action needs to be taken on rule execution.
     */
    @JsonProperty(value = "properties.action", required = true)
    private Action action;

    /*
     * Metadata used by portal/tooling/etc to render different UX experiences
     * for resources of the same type; e.g. ApiApps are a kind of
     * Microsoft.Web/sites type.  If supported, the resource provider must
     * validate and persist this value.
     */
    @JsonProperty(value = "kind", access = JsonProperty.Access.WRITE_ONLY)
    private String kind;

    /*
     * The etag field is *not* required. If it is provided in the response
     * body, it must also be provided as a header per the normal etag
     * convention.  Entity tags are used for comparing two or more entities
     * from the same requested resource. HTTP/1.1 uses entity tags in the etag
     * (section 14.19), If-Match (section 14.24), If-None-Match (section
     * 14.26), and If-Range (section 14.27) header fields.
     */
    @JsonProperty(value = "etag", access = JsonProperty.Access.WRITE_ONLY)
    private String etag;

    /**
     * Get the createdWithApiVersion property: The api-version used when creating this alert rule.
     *
     * @return the createdWithApiVersion value.
     */
    public String createdWithApiVersion() {
        return this.createdWithApiVersion;
    }

    /**
     * Get the isLegacyLogAnalyticsRule property: True if alert rule is legacy Log Analytic rule.
     *
     * @return the isLegacyLogAnalyticsRule value.
     */
    public Boolean isLegacyLogAnalyticsRule() {
        return this.isLegacyLogAnalyticsRule;
    }

    /**
     * Get the description property: The description of the Log Search rule.
     *
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: The description of the Log Search rule.
     *
     * @param description the description value to set.
     * @return the LogSearchRuleResourceInner object itself.
     */
    public LogSearchRuleResourceInner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the displayName property: The display name of the alert rule.
     *
     * @return the displayName value.
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: The display name of the alert rule.
     *
     * @param displayName the displayName value to set.
     * @return the LogSearchRuleResourceInner object itself.
     */
    public LogSearchRuleResourceInner withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the autoMitigate property: The flag that indicates whether the alert should be automatically resolved or not.
     * The default is false.
     *
     * @return the autoMitigate value.
     */
    public Boolean autoMitigate() {
        return this.autoMitigate;
    }

    /**
     * Set the autoMitigate property: The flag that indicates whether the alert should be automatically resolved or not.
     * The default is false.
     *
     * @param autoMitigate the autoMitigate value to set.
     * @return the LogSearchRuleResourceInner object itself.
     */
    public LogSearchRuleResourceInner withAutoMitigate(Boolean autoMitigate) {
        this.autoMitigate = autoMitigate;
        return this;
    }

    /**
     * Get the enabled property: The flag which indicates whether the Log Search rule is enabled. Value should be true
     * or false.
     *
     * @return the enabled value.
     */
    public Enabled enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled property: The flag which indicates whether the Log Search rule is enabled. Value should be true
     * or false.
     *
     * @param enabled the enabled value to set.
     * @return the LogSearchRuleResourceInner object itself.
     */
    public LogSearchRuleResourceInner withEnabled(Enabled enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the lastUpdatedTime property: Last time the rule was updated in IS08601 format.
     *
     * @return the lastUpdatedTime value.
     */
    public OffsetDateTime lastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    /**
     * Get the provisioningState property: Provisioning state of the scheduled query rule.
     *
     * @return the provisioningState value.
     */
    public ProvisioningState provisioningState() {
        return this.provisioningState;
    }

    /**
     * Get the source property: Data Source against which rule will Query Data.
     *
     * @return the source value.
     */
    public Source source() {
        return this.source;
    }

    /**
     * Set the source property: Data Source against which rule will Query Data.
     *
     * @param source the source value to set.
     * @return the LogSearchRuleResourceInner object itself.
     */
    public LogSearchRuleResourceInner withSource(Source source) {
        this.source = source;
        return this;
    }

    /**
     * Get the schedule property: Schedule (Frequency, Time Window) for rule. Required for action type - AlertingAction.
     *
     * @return the schedule value.
     */
    public Schedule schedule() {
        return this.schedule;
    }

    /**
     * Set the schedule property: Schedule (Frequency, Time Window) for rule. Required for action type - AlertingAction.
     *
     * @param schedule the schedule value to set.
     * @return the LogSearchRuleResourceInner object itself.
     */
    public LogSearchRuleResourceInner withSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    /**
     * Get the action property: Action needs to be taken on rule execution.
     *
     * @return the action value.
     */
    public Action action() {
        return this.action;
    }

    /**
     * Set the action property: Action needs to be taken on rule execution.
     *
     * @param action the action value to set.
     * @return the LogSearchRuleResourceInner object itself.
     */
    public LogSearchRuleResourceInner withAction(Action action) {
        this.action = action;
        return this;
    }

    /**
     * Get the kind property: Metadata used by portal/tooling/etc to render different UX experiences for resources of
     * the same type; e.g. ApiApps are a kind of Microsoft.Web/sites type. If supported, the resource provider must
     * validate and persist this value.
     *
     * @return the kind value.
     */
    public String kind() {
        return this.kind;
    }

    /**
     * Get the etag property: The etag field is *not* required. If it is provided in the response body, it must also be
     * provided as a header per the normal etag convention. Entity tags are used for comparing two or more entities from
     * the same requested resource. HTTP/1.1 uses entity tags in the etag (section 14.19), If-Match (section 14.24),
     * If-None-Match (section 14.26), and If-Range (section 14.27) header fields.
     *
     * @return the etag value.
     */
    public String etag() {
        return this.etag;
    }

    /** {@inheritDoc} */
    @Override
    public LogSearchRuleResourceInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public LogSearchRuleResourceInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (source() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property source in model LogSearchRuleResourceInner"));
        } else {
            source().validate();
        }
        if (schedule() != null) {
            schedule().validate();
        }
        if (action() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property action in model LogSearchRuleResourceInner"));
        } else {
            action().validate();
        }
    }
}
