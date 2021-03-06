// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.Map;

/** The Office365 account. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("Office365Table")
@JsonFlatten
@Fluent
public class Office365Dataset extends Dataset {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(Office365Dataset.class);

    /*
     * Name of the dataset to extract from Office 365. Type: string (or
     * Expression with resultType string).
     */
    @JsonProperty(value = "typeProperties.tableName", required = true)
    private Object tableName;

    /*
     * A predicate expression that can be used to filter the specific rows to
     * extract from Office 365. Type: string (or Expression with resultType
     * string).
     */
    @JsonProperty(value = "typeProperties.predicate")
    private Object predicate;

    /**
     * Get the tableName property: Name of the dataset to extract from Office 365. Type: string (or Expression with
     * resultType string).
     *
     * @return the tableName value.
     */
    public Object tableName() {
        return this.tableName;
    }

    /**
     * Set the tableName property: Name of the dataset to extract from Office 365. Type: string (or Expression with
     * resultType string).
     *
     * @param tableName the tableName value to set.
     * @return the Office365Dataset object itself.
     */
    public Office365Dataset withTableName(Object tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * Get the predicate property: A predicate expression that can be used to filter the specific rows to extract from
     * Office 365. Type: string (or Expression with resultType string).
     *
     * @return the predicate value.
     */
    public Object predicate() {
        return this.predicate;
    }

    /**
     * Set the predicate property: A predicate expression that can be used to filter the specific rows to extract from
     * Office 365. Type: string (or Expression with resultType string).
     *
     * @param predicate the predicate value to set.
     * @return the Office365Dataset object itself.
     */
    public Office365Dataset withPredicate(Object predicate) {
        this.predicate = predicate;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Office365Dataset withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Office365Dataset withStructure(Object structure) {
        super.withStructure(structure);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Office365Dataset withSchema(Object schema) {
        super.withSchema(schema);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Office365Dataset withLinkedServiceName(LinkedServiceReference linkedServiceName) {
        super.withLinkedServiceName(linkedServiceName);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Office365Dataset withParameters(Map<String, ParameterSpecification> parameters) {
        super.withParameters(parameters);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Office365Dataset withAnnotations(List<Object> annotations) {
        super.withAnnotations(annotations);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Office365Dataset withFolder(DatasetFolder folder) {
        super.withFolder(folder);
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
        if (tableName() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException("Missing required property tableName in model Office365Dataset"));
        }
    }
}
