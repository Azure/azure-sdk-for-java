// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.managedapplications.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.managedapplications.models.ApplicationArtifact;
import com.azure.resourcemanager.managedapplications.models.ApplicationLockLevel;
import com.azure.resourcemanager.managedapplications.models.ApplicationProviderAuthorization;
import com.azure.resourcemanager.managedapplications.models.GenericResource;
import com.azure.resourcemanager.managedapplications.models.Identity;
import com.azure.resourcemanager.managedapplications.models.Sku;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Information about managed application definition. */
@JsonFlatten
@Fluent
public class ApplicationDefinitionInner extends GenericResource {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(ApplicationDefinitionInner.class);

    /*
     * The managed application lock level.
     */
    @JsonProperty(value = "properties.lockLevel", required = true)
    private ApplicationLockLevel lockLevel;

    /*
     * The managed application definition display name.
     */
    @JsonProperty(value = "properties.displayName")
    private String displayName;

    /*
     * A value indicating whether the package is enabled or not.
     */
    @JsonProperty(value = "properties.isEnabled")
    private String isEnabled;

    /*
     * The managed application provider authorizations.
     */
    @JsonProperty(value = "properties.authorizations", required = true)
    private List<ApplicationProviderAuthorization> authorizations;

    /*
     * The collection of managed application artifacts. The portal will use the
     * files specified as artifacts to construct the user experience of
     * creating a managed application from a managed application definition.
     */
    @JsonProperty(value = "properties.artifacts")
    private List<ApplicationArtifact> artifacts;

    /*
     * The managed application definition description.
     */
    @JsonProperty(value = "properties.description")
    private String description;

    /*
     * The managed application definition package file Uri. Use this element
     */
    @JsonProperty(value = "properties.packageFileUri")
    private String packageFileUri;

    /*
     * The inline main template json which has resources to be provisioned. It
     * can be a JObject or well-formed JSON string.
     */
    @JsonProperty(value = "properties.mainTemplate")
    private Object mainTemplate;

    /*
     * The createUiDefinition json for the backing template with
     * Microsoft.Solutions/applications resource. It can be a JObject or
     * well-formed JSON string.
     */
    @JsonProperty(value = "properties.createUiDefinition")
    private Object createUiDefinition;

    /**
     * Get the lockLevel property: The managed application lock level.
     *
     * @return the lockLevel value.
     */
    public ApplicationLockLevel lockLevel() {
        return this.lockLevel;
    }

    /**
     * Set the lockLevel property: The managed application lock level.
     *
     * @param lockLevel the lockLevel value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withLockLevel(ApplicationLockLevel lockLevel) {
        this.lockLevel = lockLevel;
        return this;
    }

    /**
     * Get the displayName property: The managed application definition display name.
     *
     * @return the displayName value.
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: The managed application definition display name.
     *
     * @param displayName the displayName value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the isEnabled property: A value indicating whether the package is enabled or not.
     *
     * @return the isEnabled value.
     */
    public String isEnabled() {
        return this.isEnabled;
    }

    /**
     * Set the isEnabled property: A value indicating whether the package is enabled or not.
     *
     * @param isEnabled the isEnabled value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * Get the authorizations property: The managed application provider authorizations.
     *
     * @return the authorizations value.
     */
    public List<ApplicationProviderAuthorization> authorizations() {
        return this.authorizations;
    }

    /**
     * Set the authorizations property: The managed application provider authorizations.
     *
     * @param authorizations the authorizations value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withAuthorizations(List<ApplicationProviderAuthorization> authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    /**
     * Get the artifacts property: The collection of managed application artifacts. The portal will use the files
     * specified as artifacts to construct the user experience of creating a managed application from a managed
     * application definition.
     *
     * @return the artifacts value.
     */
    public List<ApplicationArtifact> artifacts() {
        return this.artifacts;
    }

    /**
     * Set the artifacts property: The collection of managed application artifacts. The portal will use the files
     * specified as artifacts to construct the user experience of creating a managed application from a managed
     * application definition.
     *
     * @param artifacts the artifacts value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withArtifacts(List<ApplicationArtifact> artifacts) {
        this.artifacts = artifacts;
        return this;
    }

    /**
     * Get the description property: The managed application definition description.
     *
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: The managed application definition description.
     *
     * @param description the description value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the packageFileUri property: The managed application definition package file Uri. Use this element.
     *
     * @return the packageFileUri value.
     */
    public String packageFileUri() {
        return this.packageFileUri;
    }

    /**
     * Set the packageFileUri property: The managed application definition package file Uri. Use this element.
     *
     * @param packageFileUri the packageFileUri value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withPackageFileUri(String packageFileUri) {
        this.packageFileUri = packageFileUri;
        return this;
    }

    /**
     * Get the mainTemplate property: The inline main template json which has resources to be provisioned. It can be a
     * JObject or well-formed JSON string.
     *
     * @return the mainTemplate value.
     */
    public Object mainTemplate() {
        return this.mainTemplate;
    }

    /**
     * Set the mainTemplate property: The inline main template json which has resources to be provisioned. It can be a
     * JObject or well-formed JSON string.
     *
     * @param mainTemplate the mainTemplate value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withMainTemplate(Object mainTemplate) {
        this.mainTemplate = mainTemplate;
        return this;
    }

    /**
     * Get the createUiDefinition property: The createUiDefinition json for the backing template with
     * Microsoft.Solutions/applications resource. It can be a JObject or well-formed JSON string.
     *
     * @return the createUiDefinition value.
     */
    public Object createUiDefinition() {
        return this.createUiDefinition;
    }

    /**
     * Set the createUiDefinition property: The createUiDefinition json for the backing template with
     * Microsoft.Solutions/applications resource. It can be a JObject or well-formed JSON string.
     *
     * @param createUiDefinition the createUiDefinition value to set.
     * @return the ApplicationDefinitionInner object itself.
     */
    public ApplicationDefinitionInner withCreateUiDefinition(Object createUiDefinition) {
        this.createUiDefinition = createUiDefinition;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDefinitionInner withManagedBy(String managedBy) {
        super.withManagedBy(managedBy);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDefinitionInner withSku(Sku sku) {
        super.withSku(sku);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDefinitionInner withIdentity(Identity identity) {
        super.withIdentity(identity);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDefinitionInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDefinitionInner withTags(Map<String, String> tags) {
        super.withTags(tags);
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
        if (lockLevel() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property lockLevel in model ApplicationDefinitionInner"));
        }
        if (authorizations() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property authorizations in model ApplicationDefinitionInner"));
        } else {
            authorizations().forEach(e -> e.validate());
        }
        if (artifacts() != null) {
            artifacts().forEach(e -> e.validate());
        }
    }
}
