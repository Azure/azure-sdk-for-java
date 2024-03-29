// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurearcdata.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Resource;
import com.azure.core.management.SystemData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocation;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceSku;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** A SqlManagedInstance. */
@Fluent
public final class SqlManagedInstanceInner extends Resource {
    /*
     * null
     */
    @JsonProperty(value = "properties", required = true)
    private SqlManagedInstanceProperties properties;

    /*
     * The extendedLocation of the resource.
     */
    @JsonProperty(value = "extendedLocation")
    private ExtendedLocation extendedLocation;

    /*
     * Resource sku.
     */
    @JsonProperty(value = "sku")
    private SqlManagedInstanceSku sku;

    /*
     * Read only system data
     */
    @JsonProperty(value = "systemData", access = JsonProperty.Access.WRITE_ONLY)
    private SystemData systemData;

    /** Creates an instance of SqlManagedInstanceInner class. */
    public SqlManagedInstanceInner() {
    }

    /**
     * Get the properties property: null.
     *
     * @return the properties value.
     */
    public SqlManagedInstanceProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties property: null.
     *
     * @param properties the properties value to set.
     * @return the SqlManagedInstanceInner object itself.
     */
    public SqlManagedInstanceInner withProperties(SqlManagedInstanceProperties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the extendedLocation property: The extendedLocation of the resource.
     *
     * @return the extendedLocation value.
     */
    public ExtendedLocation extendedLocation() {
        return this.extendedLocation;
    }

    /**
     * Set the extendedLocation property: The extendedLocation of the resource.
     *
     * @param extendedLocation the extendedLocation value to set.
     * @return the SqlManagedInstanceInner object itself.
     */
    public SqlManagedInstanceInner withExtendedLocation(ExtendedLocation extendedLocation) {
        this.extendedLocation = extendedLocation;
        return this;
    }

    /**
     * Get the sku property: Resource sku.
     *
     * @return the sku value.
     */
    public SqlManagedInstanceSku sku() {
        return this.sku;
    }

    /**
     * Set the sku property: Resource sku.
     *
     * @param sku the sku value to set.
     * @return the SqlManagedInstanceInner object itself.
     */
    public SqlManagedInstanceInner withSku(SqlManagedInstanceSku sku) {
        this.sku = sku;
        return this;
    }

    /**
     * Get the systemData property: Read only system data.
     *
     * @return the systemData value.
     */
    public SystemData systemData() {
        return this.systemData;
    }

    /** {@inheritDoc} */
    @Override
    public SqlManagedInstanceInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SqlManagedInstanceInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (properties() == null) {
            throw LOGGER
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property properties in model SqlManagedInstanceInner"));
        } else {
            properties().validate();
        }
        if (extendedLocation() != null) {
            extendedLocation().validate();
        }
        if (sku() != null) {
            sku().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(SqlManagedInstanceInner.class);
}
