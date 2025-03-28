// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.alertsmanagement.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The PrometheusRule model.
 */
@Fluent
public final class PrometheusRule implements JsonSerializable<PrometheusRule> {
    /*
     * the name of the recording rule.
     */
    private String record;

    /*
     * the name of the alert rule.
     */
    private String alert;

    /*
     * the flag that indicates whether the Prometheus rule is enabled.
     */
    private Boolean enabled;

    /*
     * the expression to run for the rule.
     */
    private String expression;

    /*
     * the severity of the alerts fired by the rule. Only relevant for alerts.
     */
    private Integer severity;

    /*
     * the amount of time alert must be active before firing. Only relevant for alerts.
     */
    private String forProperty;

    /*
     * labels for rule group. Only relevant for alerts.
     */
    private Map<String, String> labels;

    /*
     * annotations for rule group. Only relevant for alerts.
     */
    private Map<String, String> annotations;

    /*
     * The array of actions that are performed when the alert rule becomes active, and when an alert condition is
     * resolved. Only relevant for alerts.
     */
    private List<PrometheusRuleGroupAction> actions;

    /*
     * defines the configuration for resolving fired alerts. Only relevant for alerts.
     */
    private PrometheusRuleResolveConfiguration resolveConfiguration;

    /**
     * Creates an instance of PrometheusRule class.
     */
    public PrometheusRule() {
    }

    /**
     * Get the record property: the name of the recording rule.
     * 
     * @return the record value.
     */
    public String record() {
        return this.record;
    }

    /**
     * Set the record property: the name of the recording rule.
     * 
     * @param record the record value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withRecord(String record) {
        this.record = record;
        return this;
    }

    /**
     * Get the alert property: the name of the alert rule.
     * 
     * @return the alert value.
     */
    public String alert() {
        return this.alert;
    }

    /**
     * Set the alert property: the name of the alert rule.
     * 
     * @param alert the alert value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withAlert(String alert) {
        this.alert = alert;
        return this;
    }

    /**
     * Get the enabled property: the flag that indicates whether the Prometheus rule is enabled.
     * 
     * @return the enabled value.
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled property: the flag that indicates whether the Prometheus rule is enabled.
     * 
     * @param enabled the enabled value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the expression property: the expression to run for the rule.
     * 
     * @return the expression value.
     */
    public String expression() {
        return this.expression;
    }

    /**
     * Set the expression property: the expression to run for the rule.
     * 
     * @param expression the expression value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withExpression(String expression) {
        this.expression = expression;
        return this;
    }

    /**
     * Get the severity property: the severity of the alerts fired by the rule. Only relevant for alerts.
     * 
     * @return the severity value.
     */
    public Integer severity() {
        return this.severity;
    }

    /**
     * Set the severity property: the severity of the alerts fired by the rule. Only relevant for alerts.
     * 
     * @param severity the severity value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withSeverity(Integer severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Get the forProperty property: the amount of time alert must be active before firing. Only relevant for alerts.
     * 
     * @return the forProperty value.
     */
    public String forProperty() {
        return this.forProperty;
    }

    /**
     * Set the forProperty property: the amount of time alert must be active before firing. Only relevant for alerts.
     * 
     * @param forProperty the forProperty value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withForProperty(String forProperty) {
        this.forProperty = forProperty;
        return this;
    }

    /**
     * Get the labels property: labels for rule group. Only relevant for alerts.
     * 
     * @return the labels value.
     */
    public Map<String, String> labels() {
        return this.labels;
    }

    /**
     * Set the labels property: labels for rule group. Only relevant for alerts.
     * 
     * @param labels the labels value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Get the annotations property: annotations for rule group. Only relevant for alerts.
     * 
     * @return the annotations value.
     */
    public Map<String, String> annotations() {
        return this.annotations;
    }

    /**
     * Set the annotations property: annotations for rule group. Only relevant for alerts.
     * 
     * @param annotations the annotations value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
        return this;
    }

    /**
     * Get the actions property: The array of actions that are performed when the alert rule becomes active, and when an
     * alert condition is resolved. Only relevant for alerts.
     * 
     * @return the actions value.
     */
    public List<PrometheusRuleGroupAction> actions() {
        return this.actions;
    }

    /**
     * Set the actions property: The array of actions that are performed when the alert rule becomes active, and when an
     * alert condition is resolved. Only relevant for alerts.
     * 
     * @param actions the actions value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withActions(List<PrometheusRuleGroupAction> actions) {
        this.actions = actions;
        return this;
    }

    /**
     * Get the resolveConfiguration property: defines the configuration for resolving fired alerts. Only relevant for
     * alerts.
     * 
     * @return the resolveConfiguration value.
     */
    public PrometheusRuleResolveConfiguration resolveConfiguration() {
        return this.resolveConfiguration;
    }

    /**
     * Set the resolveConfiguration property: defines the configuration for resolving fired alerts. Only relevant for
     * alerts.
     * 
     * @param resolveConfiguration the resolveConfiguration value to set.
     * @return the PrometheusRule object itself.
     */
    public PrometheusRule withResolveConfiguration(PrometheusRuleResolveConfiguration resolveConfiguration) {
        this.resolveConfiguration = resolveConfiguration;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (expression() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property expression in model PrometheusRule"));
        }
        if (actions() != null) {
            actions().forEach(e -> e.validate());
        }
        if (resolveConfiguration() != null) {
            resolveConfiguration().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(PrometheusRule.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("expression", this.expression);
        jsonWriter.writeStringField("record", this.record);
        jsonWriter.writeStringField("alert", this.alert);
        jsonWriter.writeBooleanField("enabled", this.enabled);
        jsonWriter.writeNumberField("severity", this.severity);
        jsonWriter.writeStringField("for", this.forProperty);
        jsonWriter.writeMapField("labels", this.labels, (writer, element) -> writer.writeString(element));
        jsonWriter.writeMapField("annotations", this.annotations, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("actions", this.actions, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("resolveConfiguration", this.resolveConfiguration);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PrometheusRule from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PrometheusRule if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the PrometheusRule.
     */
    public static PrometheusRule fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PrometheusRule deserializedPrometheusRule = new PrometheusRule();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("expression".equals(fieldName)) {
                    deserializedPrometheusRule.expression = reader.getString();
                } else if ("record".equals(fieldName)) {
                    deserializedPrometheusRule.record = reader.getString();
                } else if ("alert".equals(fieldName)) {
                    deserializedPrometheusRule.alert = reader.getString();
                } else if ("enabled".equals(fieldName)) {
                    deserializedPrometheusRule.enabled = reader.getNullable(JsonReader::getBoolean);
                } else if ("severity".equals(fieldName)) {
                    deserializedPrometheusRule.severity = reader.getNullable(JsonReader::getInt);
                } else if ("for".equals(fieldName)) {
                    deserializedPrometheusRule.forProperty = reader.getString();
                } else if ("labels".equals(fieldName)) {
                    Map<String, String> labels = reader.readMap(reader1 -> reader1.getString());
                    deserializedPrometheusRule.labels = labels;
                } else if ("annotations".equals(fieldName)) {
                    Map<String, String> annotations = reader.readMap(reader1 -> reader1.getString());
                    deserializedPrometheusRule.annotations = annotations;
                } else if ("actions".equals(fieldName)) {
                    List<PrometheusRuleGroupAction> actions
                        = reader.readArray(reader1 -> PrometheusRuleGroupAction.fromJson(reader1));
                    deserializedPrometheusRule.actions = actions;
                } else if ("resolveConfiguration".equals(fieldName)) {
                    deserializedPrometheusRule.resolveConfiguration
                        = PrometheusRuleResolveConfiguration.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPrometheusRule;
        });
    }
}
