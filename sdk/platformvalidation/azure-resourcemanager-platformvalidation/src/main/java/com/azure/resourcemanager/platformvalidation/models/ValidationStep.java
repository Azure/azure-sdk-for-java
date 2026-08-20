// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Map;

/**
 * A test-provider-agnostic validation step.
 */
@Fluent
public final class ValidationStep implements JsonSerializable<ValidationStep> {
    private static final String TEST_TYPE = "test";

    private final String name;
    private final String testRef;
    private Map<String, Object> inputs;

    private ValidationStep(String name, String testRef) {
        this.name = name;
        this.testRef = testRef;
    }

    /**
     * Creates a test step using the complete test reference supplied by the caller.
     *
     * @param name the step name.
     * @param testRef the complete test reference, which is emitted unchanged.
     * @return the test step.
     */
    public static ValidationStep test(String name, String testRef) {
        return new ValidationStep(name, testRef);
    }

    /**
     * Gets the step name.
     *
     * @return the step name.
     */
    public String name() {
        return name;
    }

    /**
     * Gets the step type.
     *
     * @return {@code test}.
     */
    public String type() {
        return TEST_TYPE;
    }

    /**
     * Gets the complete test reference.
     *
     * @return the test reference.
     */
    public String testRef() {
        return testRef;
    }

    /**
     * Gets the generic test inputs.
     *
     * @return the test inputs.
     */
    public Map<String, Object> inputs() {
        return inputs;
    }

    /**
     * Sets generic JSON-capable test inputs.
     *
     * @param inputs the test inputs.
     * @return this step.
     */
    public ValidationStep withInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        validate();

        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", name);
        jsonWriter.writeStringField("type", TEST_TYPE);
        jsonWriter.writeStringField("testRef", testRef);
        if (inputs != null) {
            jsonWriter.writeUntypedField("inputs", inputs);
        }
        return jsonWriter.writeEndObject();
    }

    void validate() {
        ValidationUtils.requireNonBlank(name, "authoring.steps[].name");
        ValidationUtils.requireNonBlank(testRef, "authoring.steps[].testRef");
    }
}
