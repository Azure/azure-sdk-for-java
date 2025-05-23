// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.apimanagement.models.AlwaysLog;
import com.azure.resourcemanager.apimanagement.models.HttpCorrelationProtocol;
import com.azure.resourcemanager.apimanagement.models.OperationNameFormat;
import com.azure.resourcemanager.apimanagement.models.PipelineDiagnosticSettings;
import com.azure.resourcemanager.apimanagement.models.SamplingSettings;
import com.azure.resourcemanager.apimanagement.models.Verbosity;
import java.io.IOException;

/**
 * Diagnostic Entity Properties.
 */
@Fluent
public final class DiagnosticContractUpdateProperties implements JsonSerializable<DiagnosticContractUpdateProperties> {
    /*
     * Specifies for what type of messages sampling settings should not apply.
     */
    private AlwaysLog alwaysLog;

    /*
     * Resource Id of a target logger.
     */
    private String loggerId;

    /*
     * Sampling settings for Diagnostic.
     */
    private SamplingSettings sampling;

    /*
     * Diagnostic settings for incoming/outgoing HTTP messages to the Gateway.
     */
    private PipelineDiagnosticSettings frontend;

    /*
     * Diagnostic settings for incoming/outgoing HTTP messages to the Backend
     */
    private PipelineDiagnosticSettings backend;

    /*
     * Log the ClientIP. Default is false.
     */
    private Boolean logClientIp;

    /*
     * Sets correlation protocol to use for Application Insights diagnostics.
     */
    private HttpCorrelationProtocol httpCorrelationProtocol;

    /*
     * The verbosity level applied to traces emitted by trace policies.
     */
    private Verbosity verbosity;

    /*
     * The format of the Operation Name for Application Insights telemetries. Default is Name.
     */
    private OperationNameFormat operationNameFormat;

    /*
     * Emit custom metrics via emit-metric policy. Applicable only to Application Insights diagnostic settings.
     */
    private Boolean metrics;

    /**
     * Creates an instance of DiagnosticContractUpdateProperties class.
     */
    public DiagnosticContractUpdateProperties() {
    }

    /**
     * Get the alwaysLog property: Specifies for what type of messages sampling settings should not apply.
     * 
     * @return the alwaysLog value.
     */
    public AlwaysLog alwaysLog() {
        return this.alwaysLog;
    }

    /**
     * Set the alwaysLog property: Specifies for what type of messages sampling settings should not apply.
     * 
     * @param alwaysLog the alwaysLog value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withAlwaysLog(AlwaysLog alwaysLog) {
        this.alwaysLog = alwaysLog;
        return this;
    }

    /**
     * Get the loggerId property: Resource Id of a target logger.
     * 
     * @return the loggerId value.
     */
    public String loggerId() {
        return this.loggerId;
    }

    /**
     * Set the loggerId property: Resource Id of a target logger.
     * 
     * @param loggerId the loggerId value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withLoggerId(String loggerId) {
        this.loggerId = loggerId;
        return this;
    }

    /**
     * Get the sampling property: Sampling settings for Diagnostic.
     * 
     * @return the sampling value.
     */
    public SamplingSettings sampling() {
        return this.sampling;
    }

    /**
     * Set the sampling property: Sampling settings for Diagnostic.
     * 
     * @param sampling the sampling value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withSampling(SamplingSettings sampling) {
        this.sampling = sampling;
        return this;
    }

    /**
     * Get the frontend property: Diagnostic settings for incoming/outgoing HTTP messages to the Gateway.
     * 
     * @return the frontend value.
     */
    public PipelineDiagnosticSettings frontend() {
        return this.frontend;
    }

    /**
     * Set the frontend property: Diagnostic settings for incoming/outgoing HTTP messages to the Gateway.
     * 
     * @param frontend the frontend value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withFrontend(PipelineDiagnosticSettings frontend) {
        this.frontend = frontend;
        return this;
    }

    /**
     * Get the backend property: Diagnostic settings for incoming/outgoing HTTP messages to the Backend.
     * 
     * @return the backend value.
     */
    public PipelineDiagnosticSettings backend() {
        return this.backend;
    }

    /**
     * Set the backend property: Diagnostic settings for incoming/outgoing HTTP messages to the Backend.
     * 
     * @param backend the backend value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withBackend(PipelineDiagnosticSettings backend) {
        this.backend = backend;
        return this;
    }

    /**
     * Get the logClientIp property: Log the ClientIP. Default is false.
     * 
     * @return the logClientIp value.
     */
    public Boolean logClientIp() {
        return this.logClientIp;
    }

    /**
     * Set the logClientIp property: Log the ClientIP. Default is false.
     * 
     * @param logClientIp the logClientIp value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withLogClientIp(Boolean logClientIp) {
        this.logClientIp = logClientIp;
        return this;
    }

    /**
     * Get the httpCorrelationProtocol property: Sets correlation protocol to use for Application Insights diagnostics.
     * 
     * @return the httpCorrelationProtocol value.
     */
    public HttpCorrelationProtocol httpCorrelationProtocol() {
        return this.httpCorrelationProtocol;
    }

    /**
     * Set the httpCorrelationProtocol property: Sets correlation protocol to use for Application Insights diagnostics.
     * 
     * @param httpCorrelationProtocol the httpCorrelationProtocol value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties
        withHttpCorrelationProtocol(HttpCorrelationProtocol httpCorrelationProtocol) {
        this.httpCorrelationProtocol = httpCorrelationProtocol;
        return this;
    }

    /**
     * Get the verbosity property: The verbosity level applied to traces emitted by trace policies.
     * 
     * @return the verbosity value.
     */
    public Verbosity verbosity() {
        return this.verbosity;
    }

    /**
     * Set the verbosity property: The verbosity level applied to traces emitted by trace policies.
     * 
     * @param verbosity the verbosity value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withVerbosity(Verbosity verbosity) {
        this.verbosity = verbosity;
        return this;
    }

    /**
     * Get the operationNameFormat property: The format of the Operation Name for Application Insights telemetries.
     * Default is Name.
     * 
     * @return the operationNameFormat value.
     */
    public OperationNameFormat operationNameFormat() {
        return this.operationNameFormat;
    }

    /**
     * Set the operationNameFormat property: The format of the Operation Name for Application Insights telemetries.
     * Default is Name.
     * 
     * @param operationNameFormat the operationNameFormat value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withOperationNameFormat(OperationNameFormat operationNameFormat) {
        this.operationNameFormat = operationNameFormat;
        return this;
    }

    /**
     * Get the metrics property: Emit custom metrics via emit-metric policy. Applicable only to Application Insights
     * diagnostic settings.
     * 
     * @return the metrics value.
     */
    public Boolean metrics() {
        return this.metrics;
    }

    /**
     * Set the metrics property: Emit custom metrics via emit-metric policy. Applicable only to Application Insights
     * diagnostic settings.
     * 
     * @param metrics the metrics value to set.
     * @return the DiagnosticContractUpdateProperties object itself.
     */
    public DiagnosticContractUpdateProperties withMetrics(Boolean metrics) {
        this.metrics = metrics;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (sampling() != null) {
            sampling().validate();
        }
        if (frontend() != null) {
            frontend().validate();
        }
        if (backend() != null) {
            backend().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("alwaysLog", this.alwaysLog == null ? null : this.alwaysLog.toString());
        jsonWriter.writeStringField("loggerId", this.loggerId);
        jsonWriter.writeJsonField("sampling", this.sampling);
        jsonWriter.writeJsonField("frontend", this.frontend);
        jsonWriter.writeJsonField("backend", this.backend);
        jsonWriter.writeBooleanField("logClientIp", this.logClientIp);
        jsonWriter.writeStringField("httpCorrelationProtocol",
            this.httpCorrelationProtocol == null ? null : this.httpCorrelationProtocol.toString());
        jsonWriter.writeStringField("verbosity", this.verbosity == null ? null : this.verbosity.toString());
        jsonWriter.writeStringField("operationNameFormat",
            this.operationNameFormat == null ? null : this.operationNameFormat.toString());
        jsonWriter.writeBooleanField("metrics", this.metrics);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DiagnosticContractUpdateProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DiagnosticContractUpdateProperties if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DiagnosticContractUpdateProperties.
     */
    public static DiagnosticContractUpdateProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DiagnosticContractUpdateProperties deserializedDiagnosticContractUpdateProperties
                = new DiagnosticContractUpdateProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("alwaysLog".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.alwaysLog = AlwaysLog.fromString(reader.getString());
                } else if ("loggerId".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.loggerId = reader.getString();
                } else if ("sampling".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.sampling = SamplingSettings.fromJson(reader);
                } else if ("frontend".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.frontend
                        = PipelineDiagnosticSettings.fromJson(reader);
                } else if ("backend".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.backend
                        = PipelineDiagnosticSettings.fromJson(reader);
                } else if ("logClientIp".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.logClientIp
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("httpCorrelationProtocol".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.httpCorrelationProtocol
                        = HttpCorrelationProtocol.fromString(reader.getString());
                } else if ("verbosity".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.verbosity = Verbosity.fromString(reader.getString());
                } else if ("operationNameFormat".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.operationNameFormat
                        = OperationNameFormat.fromString(reader.getString());
                } else if ("metrics".equals(fieldName)) {
                    deserializedDiagnosticContractUpdateProperties.metrics = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDiagnosticContractUpdateProperties;
        });
    }
}
