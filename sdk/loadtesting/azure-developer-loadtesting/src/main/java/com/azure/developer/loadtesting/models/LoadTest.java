// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.developer.loadtesting.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.core.util.CoreUtils;
import com.azure.developer.loadtesting.implementation.JsonMergePatchHelper;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Load test model.
 */
@Fluent
public final class LoadTest implements JsonSerializable<LoadTest> {

    /*
     * Pass fail criteria for a test.
     */
    @Generated
    private PassFailCriteria passFailCriteria;

    /*
     * Auto stop criteria for a test. This will automatically stop a load test if the error percentage is high for a
     * certain time window.
     */
    @Generated
    private AutoStopCriteria autoStopCriteria;

    /*
     * Secrets can be stored in an Azure Key Vault or any other secret store. If the
     * secret is stored in an Azure Key Vault, the value should be the secret
     * identifier and the type should be AKV_SECRET_URI. If the secret is stored
     * elsewhere, the secret value should be provided directly and the type should be
     * SECRET_VALUE.
     */
    @Generated
    private Map<String, TestSecret> secrets;

    /*
     * Certificates metadata.
     */
    @Generated
    private TestCertificate certificate;

    /*
     * Environment variables which are defined as a set of <name,value> pairs.
     */
    @Generated
    private Map<String, String> environmentVariables;

    /*
     * The load test configuration.
     */
    @Generated
    private LoadTestConfiguration loadTestConfiguration;

    /*
     * Id of the test run to be marked as baseline to view trends of client-side metrics from recent test runs
     */
    @Generated
    private String baselineTestRunId;

    /*
     * The input artifacts for the test.
     */
    @Generated
    private TestInputArtifacts inputArtifacts;

    /*
     * Unique test identifier for the load test, must contain only lower-case alphabetic, numeric, underscore or hyphen
     * characters.
     */
    @Generated
    private String testId;

    /*
     * The test description.
     */
    @Generated
    private String description;

    /*
     * Display name of a test.
     */
    @Generated
    private String displayName;

    /*
     * Subnet ID on which the load test instances should run.
     */
    @Generated
    private String subnetId;

    /*
     * Kind of test.
     */
    @Generated
    private LoadTestKind kind;

    /*
     * Inject load test engines without deploying public IP for outbound access
     */
    @Generated
    private Boolean publicIpDisabled;

    /*
     * Type of the managed identity referencing the Key vault.
     */
    @Generated
    private String keyvaultReferenceIdentityType;

    /*
     * Resource Id of the managed identity referencing the Key vault.
     */
    @Generated
    private String keyvaultReferenceIdentityId;

    /*
     * Type of the managed identity referencing the metrics.
     */
    @Generated
    private LoadTestingManagedIdentityType metricsReferenceIdentityType;

    /*
     * Resource Id of the managed identity referencing the metrics.
     */
    @Generated
    private String metricsReferenceIdentityId;

    /*
     * Type of the managed identity built in load test engines
     */
    @Generated
    private LoadTestingManagedIdentityType engineBuiltInIdentityType;

    /*
     * Resource Ids of the managed identity built in to load test engines. Required if engineBuiltInIdentityType is
     * UserAssigned.
     */
    @Generated
    private List<String> engineBuiltInIdentityIds;

    /*
     * The creation datetime(RFC 3339 literal format).
     */
    @Generated
    private OffsetDateTime createdDateTime;

    /*
     * The user that created.
     */
    @Generated
    private String createdBy;

    /*
     * The last Modified datetime(RFC 3339 literal format).
     */
    @Generated
    private OffsetDateTime lastModifiedDateTime;

    /*
     * The user that last modified.
     */
    @Generated
    private String lastModifiedBy;

    /**
     * Stores updated model property, the value is property name, not serialized name.
     */
    @Generated
    private final Set<String> updatedProperties = new HashSet<>();

    @Generated
    private boolean jsonMergePatch;

    @Generated
    private void serializeAsJsonMergePatch(boolean jsonMergePatch) {
        this.jsonMergePatch = jsonMergePatch;
    }

    static {
        JsonMergePatchHelper.setLoadTestAccessor(new JsonMergePatchHelper.LoadTestAccessor() {

            @Override
            public LoadTest prepareModelForJsonMergePatch(LoadTest model, boolean jsonMergePatchEnabled) {
                model.serializeAsJsonMergePatch(jsonMergePatchEnabled);
                return model;
            }

            @Override
            public boolean isJsonMergePatch(LoadTest model) {
                return model.jsonMergePatch;
            }
        });
    }

    /**
     * Creates an instance of LoadTest class.
     */
    @Generated
    public LoadTest() {
    }

    /**
     * Get the passFailCriteria property: Pass fail criteria for a test.
     *
     * @return the passFailCriteria value.
     */
    @Generated
    public PassFailCriteria getPassFailCriteria() {
        return this.passFailCriteria;
    }

    /**
     * Set the passFailCriteria property: Pass fail criteria for a test.
     *
     * @param passFailCriteria the passFailCriteria value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setPassFailCriteria(PassFailCriteria passFailCriteria) {
        this.passFailCriteria = passFailCriteria;
        this.updatedProperties.add("passFailCriteria");
        return this;
    }

    /**
     * Get the autoStopCriteria property: Auto stop criteria for a test. This will automatically stop a load test if the
     * error percentage is high for a certain time window.
     *
     * @return the autoStopCriteria value.
     */
    @Generated
    public AutoStopCriteria getAutoStopCriteria() {
        return this.autoStopCriteria;
    }

    /**
     * Set the autoStopCriteria property: Auto stop criteria for a test. This will automatically stop a load test if the
     * error percentage is high for a certain time window.
     *
     * @param autoStopCriteria the autoStopCriteria value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setAutoStopCriteria(AutoStopCriteria autoStopCriteria) {
        this.autoStopCriteria = autoStopCriteria;
        this.updatedProperties.add("autoStopCriteria");
        return this;
    }

    /**
     * Get the secrets property: Secrets can be stored in an Azure Key Vault or any other secret store. If the
     * secret is stored in an Azure Key Vault, the value should be the secret
     * identifier and the type should be AKV_SECRET_URI. If the secret is stored
     * elsewhere, the secret value should be provided directly and the type should be
     * SECRET_VALUE.
     *
     * @return the secrets value.
     */
    @Generated
    public Map<String, TestSecret> getSecrets() {
        return this.secrets;
    }

    /**
     * Set the secrets property: Secrets can be stored in an Azure Key Vault or any other secret store. If the
     * secret is stored in an Azure Key Vault, the value should be the secret
     * identifier and the type should be AKV_SECRET_URI. If the secret is stored
     * elsewhere, the secret value should be provided directly and the type should be
     * SECRET_VALUE.
     *
     * @param secrets the secrets value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setSecrets(Map<String, TestSecret> secrets) {
        this.secrets = secrets;
        this.updatedProperties.add("secrets");
        return this;
    }

    /**
     * Get the certificate property: Certificates metadata.
     *
     * @return the certificate value.
     */
    @Generated
    public TestCertificate getCertificate() {
        return this.certificate;
    }

    /**
     * Set the certificate property: Certificates metadata.
     *
     * @param certificate the certificate value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setCertificate(TestCertificate certificate) {
        this.certificate = certificate;
        this.updatedProperties.add("certificate");
        return this;
    }

    /**
     * Get the environmentVariables property: Environment variables which are defined as a set of &lt;name,value&gt;
     * pairs.
     *
     * @return the environmentVariables value.
     */
    @Generated
    public Map<String, String> getEnvironmentVariables() {
        return this.environmentVariables;
    }

    /**
     * Set the environmentVariables property: Environment variables which are defined as a set of &lt;name,value&gt;
     * pairs.
     *
     * @param environmentVariables the environmentVariables value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
        this.updatedProperties.add("environmentVariables");
        return this;
    }

    /**
     * Get the loadTestConfiguration property: The load test configuration.
     *
     * @return the loadTestConfiguration value.
     */
    @Generated
    public LoadTestConfiguration getLoadTestConfiguration() {
        return this.loadTestConfiguration;
    }

    /**
     * Set the loadTestConfiguration property: The load test configuration.
     *
     * @param loadTestConfiguration the loadTestConfiguration value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setLoadTestConfiguration(LoadTestConfiguration loadTestConfiguration) {
        this.loadTestConfiguration = loadTestConfiguration;
        this.updatedProperties.add("loadTestConfiguration");
        return this;
    }

    /**
     * Get the baselineTestRunId property: Id of the test run to be marked as baseline to view trends of client-side
     * metrics from recent test runs.
     *
     * @return the baselineTestRunId value.
     */
    @Generated
    public String getBaselineTestRunId() {
        return this.baselineTestRunId;
    }

    /**
     * Set the baselineTestRunId property: Id of the test run to be marked as baseline to view trends of client-side
     * metrics from recent test runs.
     *
     * @param baselineTestRunId the baselineTestRunId value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setBaselineTestRunId(String baselineTestRunId) {
        this.baselineTestRunId = baselineTestRunId;
        this.updatedProperties.add("baselineTestRunId");
        return this;
    }

    /**
     * Get the inputArtifacts property: The input artifacts for the test.
     *
     * @return the inputArtifacts value.
     */
    @Generated
    public TestInputArtifacts getInputArtifacts() {
        return this.inputArtifacts;
    }

    /**
     * Get the testId property: Unique test identifier for the load test, must contain only lower-case alphabetic,
     * numeric, underscore or hyphen characters.
     *
     * @return the testId value.
     */
    @Generated
    public String getTestId() {
        return this.testId;
    }

    /**
     * Get the description property: The test description.
     *
     * @return the description value.
     */
    @Generated
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: The test description.
     *
     * @param description the description value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setDescription(String description) {
        this.description = description;
        this.updatedProperties.add("description");
        return this;
    }

    /**
     * Get the displayName property: Display name of a test.
     *
     * @return the displayName value.
     */
    @Generated
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: Display name of a test.
     *
     * @param displayName the displayName value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setDisplayName(String displayName) {
        this.displayName = displayName;
        this.updatedProperties.add("displayName");
        return this;
    }

    /**
     * Get the subnetId property: Subnet ID on which the load test instances should run.
     *
     * @return the subnetId value.
     */
    @Generated
    public String getSubnetId() {
        return this.subnetId;
    }

    /**
     * Set the subnetId property: Subnet ID on which the load test instances should run.
     *
     * @param subnetId the subnetId value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setSubnetId(String subnetId) {
        this.subnetId = subnetId;
        this.updatedProperties.add("subnetId");
        return this;
    }

    /**
     * Get the kind property: Kind of test.
     *
     * @return the kind value.
     */
    @Generated
    public LoadTestKind getKind() {
        return this.kind;
    }

    /**
     * Set the kind property: Kind of test.
     *
     * @param kind the kind value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setKind(LoadTestKind kind) {
        this.kind = kind;
        this.updatedProperties.add("kind");
        return this;
    }

    /**
     * Get the publicIpDisabled property: Inject load test engines without deploying public IP for outbound access.
     *
     * @return the publicIpDisabled value.
     */
    @Generated
    public Boolean isPublicIpDisabled() {
        return this.publicIpDisabled;
    }

    /**
     * Set the publicIpDisabled property: Inject load test engines without deploying public IP for outbound access.
     *
     * @param publicIpDisabled the publicIpDisabled value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setPublicIpDisabled(Boolean publicIpDisabled) {
        this.publicIpDisabled = publicIpDisabled;
        this.updatedProperties.add("publicIpDisabled");
        return this;
    }

    /**
     * Get the keyvaultReferenceIdentityType property: Type of the managed identity referencing the Key vault.
     *
     * @return the keyvaultReferenceIdentityType value.
     */
    @Generated
    public String getKeyvaultReferenceIdentityType() {
        return this.keyvaultReferenceIdentityType;
    }

    /**
     * Set the keyvaultReferenceIdentityType property: Type of the managed identity referencing the Key vault.
     *
     * @param keyvaultReferenceIdentityType the keyvaultReferenceIdentityType value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setKeyvaultReferenceIdentityType(String keyvaultReferenceIdentityType) {
        this.keyvaultReferenceIdentityType = keyvaultReferenceIdentityType;
        this.updatedProperties.add("keyvaultReferenceIdentityType");
        return this;
    }

    /**
     * Get the keyvaultReferenceIdentityId property: Resource Id of the managed identity referencing the Key vault.
     *
     * @return the keyvaultReferenceIdentityId value.
     */
    @Generated
    public String getKeyvaultReferenceIdentityId() {
        return this.keyvaultReferenceIdentityId;
    }

    /**
     * Set the keyvaultReferenceIdentityId property: Resource Id of the managed identity referencing the Key vault.
     *
     * @param keyvaultReferenceIdentityId the keyvaultReferenceIdentityId value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setKeyvaultReferenceIdentityId(String keyvaultReferenceIdentityId) {
        this.keyvaultReferenceIdentityId = keyvaultReferenceIdentityId;
        this.updatedProperties.add("keyvaultReferenceIdentityId");
        return this;
    }

    /**
     * Get the metricsReferenceIdentityType property: Type of the managed identity referencing the metrics.
     *
     * @return the metricsReferenceIdentityType value.
     */
    @Generated
    public LoadTestingManagedIdentityType getMetricsReferenceIdentityType() {
        return this.metricsReferenceIdentityType;
    }

    /**
     * Set the metricsReferenceIdentityType property: Type of the managed identity referencing the metrics.
     *
     * @param metricsReferenceIdentityType the metricsReferenceIdentityType value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setMetricsReferenceIdentityType(LoadTestingManagedIdentityType metricsReferenceIdentityType) {
        this.metricsReferenceIdentityType = metricsReferenceIdentityType;
        this.updatedProperties.add("metricsReferenceIdentityType");
        return this;
    }

    /**
     * Get the metricsReferenceIdentityId property: Resource Id of the managed identity referencing the metrics.
     *
     * @return the metricsReferenceIdentityId value.
     */
    @Generated
    public String getMetricsReferenceIdentityId() {
        return this.metricsReferenceIdentityId;
    }

    /**
     * Set the metricsReferenceIdentityId property: Resource Id of the managed identity referencing the metrics.
     *
     * @param metricsReferenceIdentityId the metricsReferenceIdentityId value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setMetricsReferenceIdentityId(String metricsReferenceIdentityId) {
        this.metricsReferenceIdentityId = metricsReferenceIdentityId;
        this.updatedProperties.add("metricsReferenceIdentityId");
        return this;
    }

    /**
     * Get the engineBuiltInIdentityType property: Type of the managed identity built in load test engines.
     *
     * @return the engineBuiltInIdentityType value.
     */
    @Generated
    public LoadTestingManagedIdentityType getEngineBuiltInIdentityType() {
        return this.engineBuiltInIdentityType;
    }

    /**
     * Set the engineBuiltInIdentityType property: Type of the managed identity built in load test engines.
     *
     * @param engineBuiltInIdentityType the engineBuiltInIdentityType value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setEngineBuiltInIdentityType(LoadTestingManagedIdentityType engineBuiltInIdentityType) {
        this.engineBuiltInIdentityType = engineBuiltInIdentityType;
        this.updatedProperties.add("engineBuiltInIdentityType");
        return this;
    }

    /**
     * Get the engineBuiltInIdentityIds property: Resource Ids of the managed identity built in to load test engines.
     * Required if engineBuiltInIdentityType is UserAssigned.
     *
     * @return the engineBuiltInIdentityIds value.
     */
    @Generated
    public List<String> getEngineBuiltInIdentityIds() {
        return this.engineBuiltInIdentityIds;
    }

    /**
     * Set the engineBuiltInIdentityIds property: Resource Ids of the managed identity built in to load test engines.
     * Required if engineBuiltInIdentityType is UserAssigned.
     *
     * @param engineBuiltInIdentityIds the engineBuiltInIdentityIds value to set.
     * @return the LoadTest object itself.
     */
    @Generated
    public LoadTest setEngineBuiltInIdentityIds(List<String> engineBuiltInIdentityIds) {
        this.engineBuiltInIdentityIds = engineBuiltInIdentityIds;
        this.updatedProperties.add("engineBuiltInIdentityIds");
        return this;
    }

    /**
     * Get the createdDateTime property: The creation datetime(RFC 3339 literal format).
     *
     * @return the createdDateTime value.
     */
    @Generated
    public OffsetDateTime getCreatedDateTime() {
        return this.createdDateTime;
    }

    /**
     * Get the createdBy property: The user that created.
     *
     * @return the createdBy value.
     */
    @Generated
    public String getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Get the lastModifiedDateTime property: The last Modified datetime(RFC 3339 literal format).
     *
     * @return the lastModifiedDateTime value.
     */
    @Generated
    public OffsetDateTime getLastModifiedDateTime() {
        return this.lastModifiedDateTime;
    }

    /**
     * Get the lastModifiedBy property: The user that last modified.
     *
     * @return the lastModifiedBy value.
     */
    @Generated
    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        if (jsonMergePatch) {
            return toJsonMergePatch(jsonWriter);
        } else {
            jsonWriter.writeStartObject();
            jsonWriter.writeJsonField("passFailCriteria", this.passFailCriteria);
            jsonWriter.writeJsonField("autoStopCriteria", this.autoStopCriteria);
            jsonWriter.writeMapField("secrets", this.secrets, (writer, element) -> writer.writeJson(element));
            jsonWriter.writeJsonField("certificate", this.certificate);
            jsonWriter.writeMapField("environmentVariables", this.environmentVariables,
                (writer, element) -> writer.writeString(element));
            jsonWriter.writeJsonField("loadTestConfiguration", this.loadTestConfiguration);
            jsonWriter.writeStringField("baselineTestRunId", this.baselineTestRunId);
            jsonWriter.writeStringField("description", this.description);
            jsonWriter.writeStringField("displayName", this.displayName);
            jsonWriter.writeStringField("subnetId", this.subnetId);
            jsonWriter.writeStringField("kind", this.kind == null ? null : this.kind.toString());
            jsonWriter.writeBooleanField("publicIPDisabled", this.publicIpDisabled);
            jsonWriter.writeStringField("keyvaultReferenceIdentityType", this.keyvaultReferenceIdentityType);
            jsonWriter.writeStringField("keyvaultReferenceIdentityId", this.keyvaultReferenceIdentityId);
            jsonWriter.writeStringField("metricsReferenceIdentityType",
                this.metricsReferenceIdentityType == null ? null : this.metricsReferenceIdentityType.toString());
            jsonWriter.writeStringField("metricsReferenceIdentityId", this.metricsReferenceIdentityId);
            jsonWriter.writeStringField("engineBuiltInIdentityType",
                this.engineBuiltInIdentityType == null ? null : this.engineBuiltInIdentityType.toString());
            jsonWriter.writeArrayField("engineBuiltInIdentityIds", this.engineBuiltInIdentityIds,
                (writer, element) -> writer.writeString(element));
            return jsonWriter.writeEndObject();
        }
    }

    @Generated
    private JsonWriter toJsonMergePatch(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        if (updatedProperties.contains("passFailCriteria")) {
            if (this.passFailCriteria == null) {
                jsonWriter.writeNullField("passFailCriteria");
            } else {
                JsonMergePatchHelper.getPassFailCriteriaAccessor()
                    .prepareModelForJsonMergePatch(this.passFailCriteria, true);
                jsonWriter.writeJsonField("passFailCriteria", this.passFailCriteria);
                JsonMergePatchHelper.getPassFailCriteriaAccessor()
                    .prepareModelForJsonMergePatch(this.passFailCriteria, false);
            }
        }
        if (updatedProperties.contains("autoStopCriteria")) {
            if (this.autoStopCriteria == null) {
                jsonWriter.writeNullField("autoStopCriteria");
            } else {
                JsonMergePatchHelper.getAutoStopCriteriaAccessor()
                    .prepareModelForJsonMergePatch(this.autoStopCriteria, true);
                jsonWriter.writeJsonField("autoStopCriteria", this.autoStopCriteria);
                JsonMergePatchHelper.getAutoStopCriteriaAccessor()
                    .prepareModelForJsonMergePatch(this.autoStopCriteria, false);
            }
        }
        if (updatedProperties.contains("secrets")) {
            if (this.secrets == null) {
                jsonWriter.writeNullField("secrets");
            } else {
                jsonWriter.writeMapField("secrets", this.secrets, (writer, element) -> {
                    if (element != null) {
                        JsonMergePatchHelper.getTestSecretAccessor().prepareModelForJsonMergePatch(element, true);
                        writer.writeJson(element);
                        JsonMergePatchHelper.getTestSecretAccessor().prepareModelForJsonMergePatch(element, false);
                    } else {
                        writer.writeNull();
                    }
                });
            }
        }
        if (updatedProperties.contains("certificate")) {
            if (this.certificate == null) {
                jsonWriter.writeNullField("certificate");
            } else {
                JsonMergePatchHelper.getTestCertificateAccessor().prepareModelForJsonMergePatch(this.certificate, true);
                jsonWriter.writeJsonField("certificate", this.certificate);
                JsonMergePatchHelper.getTestCertificateAccessor()
                    .prepareModelForJsonMergePatch(this.certificate, false);
            }
        }
        if (updatedProperties.contains("environmentVariables")) {
            if (this.environmentVariables == null) {
                jsonWriter.writeNullField("environmentVariables");
            } else {
                jsonWriter.writeMapField("environmentVariables", this.environmentVariables, (writer, element) -> {
                    if (element != null) {
                        writer.writeString(element);
                    } else {
                        writer.writeNull();
                    }
                });
            }
        }
        if (updatedProperties.contains("loadTestConfiguration")) {
            if (this.loadTestConfiguration == null) {
                jsonWriter.writeNullField("loadTestConfiguration");
            } else {
                JsonMergePatchHelper.getLoadTestConfigurationAccessor()
                    .prepareModelForJsonMergePatch(this.loadTestConfiguration, true);
                jsonWriter.writeJsonField("loadTestConfiguration", this.loadTestConfiguration);
                JsonMergePatchHelper.getLoadTestConfigurationAccessor()
                    .prepareModelForJsonMergePatch(this.loadTestConfiguration, false);
            }
        }
        if (updatedProperties.contains("baselineTestRunId")) {
            if (this.baselineTestRunId == null) {
                jsonWriter.writeNullField("baselineTestRunId");
            } else {
                jsonWriter.writeStringField("baselineTestRunId", this.baselineTestRunId);
            }
        }
        if (updatedProperties.contains("description")) {
            if (this.description == null) {
                jsonWriter.writeNullField("description");
            } else {
                jsonWriter.writeStringField("description", this.description);
            }
        }
        if (updatedProperties.contains("displayName")) {
            if (this.displayName == null) {
                jsonWriter.writeNullField("displayName");
            } else {
                jsonWriter.writeStringField("displayName", this.displayName);
            }
        }
        if (updatedProperties.contains("subnetId")) {
            if (this.subnetId == null) {
                jsonWriter.writeNullField("subnetId");
            } else {
                jsonWriter.writeStringField("subnetId", this.subnetId);
            }
        }
        if (updatedProperties.contains("kind")) {
            if (this.kind == null) {
                jsonWriter.writeNullField("kind");
            } else {
                jsonWriter.writeStringField("kind", this.kind.toString());
            }
        }
        if (updatedProperties.contains("publicIpDisabled")) {
            if (this.publicIpDisabled == null) {
                jsonWriter.writeNullField("publicIPDisabled");
            } else {
                jsonWriter.writeBooleanField("publicIPDisabled", this.publicIpDisabled);
            }
        }
        if (updatedProperties.contains("keyvaultReferenceIdentityType")) {
            if (this.keyvaultReferenceIdentityType == null) {
                jsonWriter.writeNullField("keyvaultReferenceIdentityType");
            } else {
                jsonWriter.writeStringField("keyvaultReferenceIdentityType", this.keyvaultReferenceIdentityType);
            }
        }
        if (updatedProperties.contains("keyvaultReferenceIdentityId")) {
            if (this.keyvaultReferenceIdentityId == null) {
                jsonWriter.writeNullField("keyvaultReferenceIdentityId");
            } else {
                jsonWriter.writeStringField("keyvaultReferenceIdentityId", this.keyvaultReferenceIdentityId);
            }
        }
        if (updatedProperties.contains("metricsReferenceIdentityType")) {
            if (this.metricsReferenceIdentityType == null) {
                jsonWriter.writeNullField("metricsReferenceIdentityType");
            } else {
                jsonWriter.writeStringField("metricsReferenceIdentityType",
                    this.metricsReferenceIdentityType.toString());
            }
        }
        if (updatedProperties.contains("metricsReferenceIdentityId")) {
            if (this.metricsReferenceIdentityId == null) {
                jsonWriter.writeNullField("metricsReferenceIdentityId");
            } else {
                jsonWriter.writeStringField("metricsReferenceIdentityId", this.metricsReferenceIdentityId);
            }
        }
        if (updatedProperties.contains("engineBuiltInIdentityType")) {
            if (this.engineBuiltInIdentityType == null) {
                jsonWriter.writeNullField("engineBuiltInIdentityType");
            } else {
                jsonWriter.writeStringField("engineBuiltInIdentityType", this.engineBuiltInIdentityType.toString());
            }
        }
        if (updatedProperties.contains("engineBuiltInIdentityIds")) {
            if (this.engineBuiltInIdentityIds == null) {
                jsonWriter.writeNullField("engineBuiltInIdentityIds");
            } else {
                jsonWriter.writeArrayField("engineBuiltInIdentityIds", this.engineBuiltInIdentityIds,
                    (writer, element) -> writer.writeString(element));
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of LoadTest from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of LoadTest if the JsonReader was pointing to an instance of it, or null if it was pointing
     * to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the LoadTest.
     */
    @Generated
    public static LoadTest fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LoadTest deserializedLoadTest = new LoadTest();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("testId".equals(fieldName)) {
                    deserializedLoadTest.testId = reader.getString();
                } else if ("passFailCriteria".equals(fieldName)) {
                    deserializedLoadTest.passFailCriteria = PassFailCriteria.fromJson(reader);
                } else if ("autoStopCriteria".equals(fieldName)) {
                    deserializedLoadTest.autoStopCriteria = AutoStopCriteria.fromJson(reader);
                } else if ("secrets".equals(fieldName)) {
                    Map<String, TestSecret> secrets = reader.readMap(reader1 -> TestSecret.fromJson(reader1));
                    deserializedLoadTest.secrets = secrets;
                } else if ("certificate".equals(fieldName)) {
                    deserializedLoadTest.certificate = TestCertificate.fromJson(reader);
                } else if ("environmentVariables".equals(fieldName)) {
                    Map<String, String> environmentVariables = reader.readMap(reader1 -> reader1.getString());
                    deserializedLoadTest.environmentVariables = environmentVariables;
                } else if ("loadTestConfiguration".equals(fieldName)) {
                    deserializedLoadTest.loadTestConfiguration = LoadTestConfiguration.fromJson(reader);
                } else if ("baselineTestRunId".equals(fieldName)) {
                    deserializedLoadTest.baselineTestRunId = reader.getString();
                } else if ("inputArtifacts".equals(fieldName)) {
                    deserializedLoadTest.inputArtifacts = TestInputArtifacts.fromJson(reader);
                } else if ("description".equals(fieldName)) {
                    deserializedLoadTest.description = reader.getString();
                } else if ("displayName".equals(fieldName)) {
                    deserializedLoadTest.displayName = reader.getString();
                } else if ("subnetId".equals(fieldName)) {
                    deserializedLoadTest.subnetId = reader.getString();
                } else if ("kind".equals(fieldName)) {
                    deserializedLoadTest.kind = LoadTestKind.fromString(reader.getString());
                } else if ("publicIPDisabled".equals(fieldName)) {
                    deserializedLoadTest.publicIpDisabled = reader.getNullable(JsonReader::getBoolean);
                } else if ("keyvaultReferenceIdentityType".equals(fieldName)) {
                    deserializedLoadTest.keyvaultReferenceIdentityType = reader.getString();
                } else if ("keyvaultReferenceIdentityId".equals(fieldName)) {
                    deserializedLoadTest.keyvaultReferenceIdentityId = reader.getString();
                } else if ("metricsReferenceIdentityType".equals(fieldName)) {
                    deserializedLoadTest.metricsReferenceIdentityType
                        = LoadTestingManagedIdentityType.fromString(reader.getString());
                } else if ("metricsReferenceIdentityId".equals(fieldName)) {
                    deserializedLoadTest.metricsReferenceIdentityId = reader.getString();
                } else if ("engineBuiltInIdentityType".equals(fieldName)) {
                    deserializedLoadTest.engineBuiltInIdentityType
                        = LoadTestingManagedIdentityType.fromString(reader.getString());
                } else if ("engineBuiltInIdentityIds".equals(fieldName)) {
                    List<String> engineBuiltInIdentityIds = reader.readArray(reader1 -> reader1.getString());
                    deserializedLoadTest.engineBuiltInIdentityIds = engineBuiltInIdentityIds;
                } else if ("createdDateTime".equals(fieldName)) {
                    deserializedLoadTest.createdDateTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("createdBy".equals(fieldName)) {
                    deserializedLoadTest.createdBy = reader.getString();
                } else if ("lastModifiedDateTime".equals(fieldName)) {
                    deserializedLoadTest.lastModifiedDateTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("lastModifiedBy".equals(fieldName)) {
                    deserializedLoadTest.lastModifiedBy = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedLoadTest;
        });
    }
}
