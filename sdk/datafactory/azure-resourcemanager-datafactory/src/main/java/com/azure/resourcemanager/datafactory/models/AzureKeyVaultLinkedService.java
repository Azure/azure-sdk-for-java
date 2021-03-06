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

/** Azure Key Vault linked service. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("AzureKeyVault")
@JsonFlatten
@Fluent
public class AzureKeyVaultLinkedService extends LinkedService {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(AzureKeyVaultLinkedService.class);

    /*
     * The base URL of the Azure Key Vault. e.g. https://myakv.vault.azure.net
     * Type: string (or Expression with resultType string).
     */
    @JsonProperty(value = "typeProperties.baseUrl", required = true)
    private Object baseUrl;

    /**
     * Get the baseUrl property: The base URL of the Azure Key Vault. e.g. https://myakv.vault.azure.net Type: string
     * (or Expression with resultType string).
     *
     * @return the baseUrl value.
     */
    public Object baseUrl() {
        return this.baseUrl;
    }

    /**
     * Set the baseUrl property: The base URL of the Azure Key Vault. e.g. https://myakv.vault.azure.net Type: string
     * (or Expression with resultType string).
     *
     * @param baseUrl the baseUrl value to set.
     * @return the AzureKeyVaultLinkedService object itself.
     */
    public AzureKeyVaultLinkedService withBaseUrl(Object baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureKeyVaultLinkedService withConnectVia(IntegrationRuntimeReference connectVia) {
        super.withConnectVia(connectVia);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureKeyVaultLinkedService withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureKeyVaultLinkedService withParameters(Map<String, ParameterSpecification> parameters) {
        super.withParameters(parameters);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AzureKeyVaultLinkedService withAnnotations(List<Object> annotations) {
        super.withAnnotations(annotations);
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
        if (baseUrl() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property baseUrl in model AzureKeyVaultLinkedService"));
        }
    }
}
