// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Kube State Metrics profile for the Azure Managed Prometheus addon. These optional settings are for the
 * kube-state-metrics pod that is deployed with the addon. See aka.ms/AzureManagedPrometheus-optional-parameters for
 * details.
 */
@Fluent
public final class ManagedClusterAzureMonitorProfileKubeStateMetrics
    implements JsonSerializable<ManagedClusterAzureMonitorProfileKubeStateMetrics> {
    /*
     * Comma-separated list of additional Kubernetes label keys that will be used in the resource's labels metric
     * (Example: 'namespaces=[k8s-label-1,k8s-label-n,...],pods=[app],...'). By default the metric contains only
     * resource name and namespace labels.
     */
    private String metricLabelsAllowlist;

    /*
     * Comma-separated list of Kubernetes annotation keys that will be used in the resource's labels metric (Example:
     * 'namespaces=[kubernetes.io/team,...],pods=[kubernetes.io/team],...'). By default the metric contains only
     * resource name and namespace labels.
     */
    private String metricAnnotationsAllowList;

    /**
     * Creates an instance of ManagedClusterAzureMonitorProfileKubeStateMetrics class.
     */
    public ManagedClusterAzureMonitorProfileKubeStateMetrics() {
    }

    /**
     * Get the metricLabelsAllowlist property: Comma-separated list of additional Kubernetes label keys that will be
     * used in the resource's labels metric (Example: 'namespaces=[k8s-label-1,k8s-label-n,...],pods=[app],...'). By
     * default the metric contains only resource name and namespace labels.
     * 
     * @return the metricLabelsAllowlist value.
     */
    public String metricLabelsAllowlist() {
        return this.metricLabelsAllowlist;
    }

    /**
     * Set the metricLabelsAllowlist property: Comma-separated list of additional Kubernetes label keys that will be
     * used in the resource's labels metric (Example: 'namespaces=[k8s-label-1,k8s-label-n,...],pods=[app],...'). By
     * default the metric contains only resource name and namespace labels.
     * 
     * @param metricLabelsAllowlist the metricLabelsAllowlist value to set.
     * @return the ManagedClusterAzureMonitorProfileKubeStateMetrics object itself.
     */
    public ManagedClusterAzureMonitorProfileKubeStateMetrics withMetricLabelsAllowlist(String metricLabelsAllowlist) {
        this.metricLabelsAllowlist = metricLabelsAllowlist;
        return this;
    }

    /**
     * Get the metricAnnotationsAllowList property: Comma-separated list of Kubernetes annotation keys that will be used
     * in the resource's labels metric (Example: 'namespaces=[kubernetes.io/team,...],pods=[kubernetes.io/team],...').
     * By default the metric contains only resource name and namespace labels.
     * 
     * @return the metricAnnotationsAllowList value.
     */
    public String metricAnnotationsAllowList() {
        return this.metricAnnotationsAllowList;
    }

    /**
     * Set the metricAnnotationsAllowList property: Comma-separated list of Kubernetes annotation keys that will be used
     * in the resource's labels metric (Example: 'namespaces=[kubernetes.io/team,...],pods=[kubernetes.io/team],...').
     * By default the metric contains only resource name and namespace labels.
     * 
     * @param metricAnnotationsAllowList the metricAnnotationsAllowList value to set.
     * @return the ManagedClusterAzureMonitorProfileKubeStateMetrics object itself.
     */
    public ManagedClusterAzureMonitorProfileKubeStateMetrics
        withMetricAnnotationsAllowList(String metricAnnotationsAllowList) {
        this.metricAnnotationsAllowList = metricAnnotationsAllowList;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("metricLabelsAllowlist", this.metricLabelsAllowlist);
        jsonWriter.writeStringField("metricAnnotationsAllowList", this.metricAnnotationsAllowList);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ManagedClusterAzureMonitorProfileKubeStateMetrics from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ManagedClusterAzureMonitorProfileKubeStateMetrics if the JsonReader was pointing to an
     * instance of it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ManagedClusterAzureMonitorProfileKubeStateMetrics.
     */
    public static ManagedClusterAzureMonitorProfileKubeStateMetrics fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ManagedClusterAzureMonitorProfileKubeStateMetrics deserializedManagedClusterAzureMonitorProfileKubeStateMetrics
                = new ManagedClusterAzureMonitorProfileKubeStateMetrics();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("metricLabelsAllowlist".equals(fieldName)) {
                    deserializedManagedClusterAzureMonitorProfileKubeStateMetrics.metricLabelsAllowlist
                        = reader.getString();
                } else if ("metricAnnotationsAllowList".equals(fieldName)) {
                    deserializedManagedClusterAzureMonitorProfileKubeStateMetrics.metricAnnotationsAllowList
                        = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedManagedClusterAzureMonitorProfileKubeStateMetrics;
        });
    }
}
