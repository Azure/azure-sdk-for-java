// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonProviders;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a Platform Validation execution plan.
 */
@Fluent
public final class ExecutionPlanConfiguration implements JsonSerializable<ExecutionPlanConfiguration> {
    private static final String API_VERSION = "microsoft.validate/executionPlan.v0";
    private static final String KIND = "ExecutionPlan";

    private String name;
    private CertificationPackageReference certificationPackageReference;
    private List<ValidationStep> steps = new ArrayList<>();

    /**
     * Gets the execution plan name.
     *
     * @return the execution plan name.
     */
    public String name() {
        return name;
    }

    /**
     * Sets the execution plan name.
     *
     * @param name the execution plan name.
     * @return this configuration.
     */
    public ExecutionPlanConfiguration withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the certification package reference.
     *
     * @return the certification package reference.
     */
    public CertificationPackageReference certificationPackageReference() {
        return certificationPackageReference;
    }

    /**
     * Sets the certification package reference.
     *
     * @param certificationPackageReference the certification package reference.
     * @return this configuration.
     */
    public ExecutionPlanConfiguration
        withCertificationPackageReference(CertificationPackageReference certificationPackageReference) {
        this.certificationPackageReference = certificationPackageReference;
        return this;
    }

    /**
     * Gets the validation steps.
     *
     * @return the validation steps.
     */
    public List<ValidationStep> steps() {
        return steps;
    }

    /**
     * Sets the validation steps.
     *
     * @param steps the validation steps.
     * @return this configuration.
     */
    public ExecutionPlanConfiguration withSteps(List<ValidationStep> steps) {
        this.steps = steps;
        return this;
    }

    /**
     * Adds a validation step.
     *
     * @param step the validation step.
     * @return this configuration.
     */
    public ExecutionPlanConfiguration addStep(ValidationStep step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
        return this;
    }

    /**
     * Serializes this configuration to the value accepted by
     * {@link ValidationExecutionPlanProperties#withPlanConfigurationJson(String)}.
     *
     * @return the serialized execution plan configuration.
     * @throws IllegalStateException if the configuration is incomplete.
     */
    public String toJsonString() {
        validate();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(output)) {
            toJson(jsonWriter);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize the execution plan configuration.", exception);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        validate();

        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("apiVersion", API_VERSION);
        jsonWriter.writeStringField("kind", KIND);
        jsonWriter.writeStartObject("metadata");
        jsonWriter.writeStringField("name", name);
        jsonWriter.writeEndObject();
        jsonWriter.writeStartObject("parameters");
        jsonWriter.writeJsonField("certificationPackageReference", certificationPackageReference);
        jsonWriter.writeEndObject();
        jsonWriter.writeStartObject("authoring");
        jsonWriter.writeArrayField("steps", steps, (writer, step) -> writer.writeJson(step));
        jsonWriter.writeEndObject();
        return jsonWriter.writeEndObject();
    }

    private void validate() {
        ValidationUtils.requireNonBlank(name, "metadata.name");
        if (certificationPackageReference == null) {
            throw new IllegalStateException("parameters.certificationPackageReference is required.");
        }
        certificationPackageReference.validate();
        if (steps == null || steps.isEmpty()) {
            throw new IllegalStateException("authoring.steps must contain at least one step.");
        }
        for (ValidationStep step : steps) {
            if (step == null) {
                throw new IllegalStateException("authoring.steps cannot contain null values.");
            }
            step.validate();
        }
    }
}
