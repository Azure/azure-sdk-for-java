// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Describes the certification package used by an execution plan.
 */
@Fluent
public final class CertificationPackageReference implements JsonSerializable<CertificationPackageReference> {
    private String osType;
    private String vmGenerationType;
    private String architectureType;
    private List<String> recommendedVMSizes;
    private CertificationPackageStorageProfile storageProfile;
    private Map<String, Object> additionalProperties;

    /**
     * Gets the operating system type.
     *
     * @return the operating system type.
     */
    public String osType() {
        return osType;
    }

    /**
     * Sets the operating system type.
     *
     * @param osType the operating system type.
     * @return this reference.
     */
    public CertificationPackageReference withOsType(String osType) {
        this.osType = osType;
        return this;
    }

    /**
     * Gets the VM generation type.
     *
     * @return the VM generation type.
     */
    public String vmGenerationType() {
        return vmGenerationType;
    }

    /**
     * Sets the VM generation type.
     *
     * @param vmGenerationType the VM generation type.
     * @return this reference.
     */
    public CertificationPackageReference withVmGenerationType(String vmGenerationType) {
        this.vmGenerationType = vmGenerationType;
        return this;
    }

    /**
     * Gets the architecture type.
     *
     * @return the architecture type.
     */
    public String architectureType() {
        return architectureType;
    }

    /**
     * Sets the architecture type.
     *
     * @param architectureType the architecture type.
     * @return this reference.
     */
    public CertificationPackageReference withArchitectureType(String architectureType) {
        this.architectureType = architectureType;
        return this;
    }

    /**
     * Gets the recommended VM sizes.
     *
     * @return the recommended VM sizes.
     */
    public List<String> recommendedVMSizes() {
        return recommendedVMSizes;
    }

    /**
     * Sets the recommended VM sizes.
     *
     * @param recommendedVMSizes the recommended VM sizes.
     * @return this reference.
     */
    public CertificationPackageReference withRecommendedVMSizes(List<String> recommendedVMSizes) {
        this.recommendedVMSizes = recommendedVMSizes;
        return this;
    }

    /**
     * Gets the storage profile.
     *
     * @return the storage profile.
     */
    public CertificationPackageStorageProfile storageProfile() {
        return storageProfile;
    }

    /**
     * Sets the storage profile.
     *
     * @param storageProfile the storage profile.
     * @return this reference.
     */
    public CertificationPackageReference withStorageProfile(CertificationPackageStorageProfile storageProfile) {
        this.storageProfile = storageProfile;
        return this;
    }

    /**
     * Gets provider-specific certification package properties.
     *
     * @return the additional properties.
     */
    public Map<String, Object> additionalProperties() {
        return additionalProperties;
    }

    /**
     * Sets provider-specific certification package properties.
     *
     * @param additionalProperties the additional properties.
     * @return this reference.
     */
    public CertificationPackageReference withAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        validate();

        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("osType", osType);
        jsonWriter.writeStringField("vmGenerationType", vmGenerationType);
        jsonWriter.writeStringField("architectureType", architectureType);
        jsonWriter.writeArrayField("recommendedVMSizes", recommendedVMSizes, JsonWriter::writeString);
        jsonWriter.writeJsonField("storageProfile", storageProfile);
        if (additionalProperties != null) {
            jsonWriter.writeUntypedField("additionalProperties", additionalProperties);
        }
        return jsonWriter.writeEndObject();
    }

    void validate() {
        ValidationUtils.requireNonBlank(osType, "parameters.certificationPackageReference.osType");
        ValidationUtils.requireNonBlank(vmGenerationType,
            "parameters.certificationPackageReference.vmGenerationType");
        ValidationUtils.requireNonBlank(architectureType,
            "parameters.certificationPackageReference.architectureType");
        if (recommendedVMSizes == null || recommendedVMSizes.isEmpty()) {
            throw new IllegalStateException(
                "parameters.certificationPackageReference.recommendedVMSizes must contain at least one VM size.");
        }
        for (String vmSize : recommendedVMSizes) {
            ValidationUtils.requireNonBlank(vmSize,
                "parameters.certificationPackageReference.recommendedVMSizes values");
        }
        if (storageProfile == null) {
            throw new IllegalStateException("parameters.certificationPackageReference.storageProfile is required.");
        }
        storageProfile.validate();
    }
}
