// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.containers.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Cluster profile.
 */
@Fluent
public final class ClusterProfile implements JsonSerializable<ClusterProfile> {
    /*
     * Version with 3/4 part.
     */
    private String clusterVersion;

    /*
     * Version with three part.
     */
    private String ossVersion;

    /*
     * Component list of this cluster type and version.
     */
    private List<ClusterComponentsItem> components;

    /*
     * This is deprecated. Please use managed identity profile instead.
     */
    private IdentityProfile identityProfile;

    /*
     * This property is required by Trino, Spark and Flink cluster but is optional for Kafka cluster.
     */
    private ManagedIdentityProfile managedIdentityProfile;

    /*
     * Authorization profile with details of AAD user Ids and group Ids authorized for data plane access.
     */
    private AuthorizationProfile authorizationProfile;

    /*
     * The cluster secret profile.
     */
    private SecretsProfile secretsProfile;

    /*
     * The service configs profiles.
     */
    private List<ClusterServiceConfigsProfile> serviceConfigsProfiles;

    /*
     * Cluster connectivity profile.
     */
    private ConnectivityProfile connectivityProfile;

    /*
     * Cluster access profile.
     */
    private ClusterAccessProfile clusterAccessProfile;

    /*
     * Cluster log analytics profile to enable or disable OMS agent for cluster.
     */
    private ClusterLogAnalyticsProfile logAnalyticsProfile;

    /*
     * Cluster Prometheus profile.
     */
    private ClusterPrometheusProfile prometheusProfile;

    /*
     * Ssh profile for the cluster.
     */
    private SshProfile sshProfile;

    /*
     * This is the Autoscale profile for the cluster. This will allow customer to create cluster enabled with Autoscale.
     */
    private AutoscaleProfile autoscaleProfile;

    /*
     * Cluster Ranger plugin profile.
     */
    private ClusterRangerPluginProfile rangerPluginProfile;

    /*
     * The Kafka cluster profile.
     */
    private KafkaProfile kafkaProfile;

    /*
     * Trino Cluster profile.
     */
    private TrinoProfile trinoProfile;

    /*
     * LLAP cluster profile.
     */
    private Map<String, Object> llapProfile;

    /*
     * The Flink cluster profile.
     */
    private FlinkProfile flinkProfile;

    /*
     * The spark cluster profile.
     */
    private SparkProfile sparkProfile;

    /*
     * The ranger cluster profile.
     */
    private RangerProfile rangerProfile;

    /*
     * Stub cluster profile.
     */
    private Map<String, Object> stubProfile;

    /*
     * The script action profile list.
     */
    private List<ScriptActionProfile> scriptActionProfiles;

    /**
     * Creates an instance of ClusterProfile class.
     */
    public ClusterProfile() {
    }

    /**
     * Get the clusterVersion property: Version with 3/4 part.
     * 
     * @return the clusterVersion value.
     */
    public String clusterVersion() {
        return this.clusterVersion;
    }

    /**
     * Set the clusterVersion property: Version with 3/4 part.
     * 
     * @param clusterVersion the clusterVersion value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion;
        return this;
    }

    /**
     * Get the ossVersion property: Version with three part.
     * 
     * @return the ossVersion value.
     */
    public String ossVersion() {
        return this.ossVersion;
    }

    /**
     * Set the ossVersion property: Version with three part.
     * 
     * @param ossVersion the ossVersion value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withOssVersion(String ossVersion) {
        this.ossVersion = ossVersion;
        return this;
    }

    /**
     * Get the components property: Component list of this cluster type and version.
     * 
     * @return the components value.
     */
    public List<ClusterComponentsItem> components() {
        return this.components;
    }

    /**
     * Get the identityProfile property: This is deprecated. Please use managed identity profile instead.
     * 
     * @return the identityProfile value.
     */
    public IdentityProfile identityProfile() {
        return this.identityProfile;
    }

    /**
     * Set the identityProfile property: This is deprecated. Please use managed identity profile instead.
     * 
     * @param identityProfile the identityProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withIdentityProfile(IdentityProfile identityProfile) {
        this.identityProfile = identityProfile;
        return this;
    }

    /**
     * Get the managedIdentityProfile property: This property is required by Trino, Spark and Flink cluster but is
     * optional for Kafka cluster.
     * 
     * @return the managedIdentityProfile value.
     */
    public ManagedIdentityProfile managedIdentityProfile() {
        return this.managedIdentityProfile;
    }

    /**
     * Set the managedIdentityProfile property: This property is required by Trino, Spark and Flink cluster but is
     * optional for Kafka cluster.
     * 
     * @param managedIdentityProfile the managedIdentityProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withManagedIdentityProfile(ManagedIdentityProfile managedIdentityProfile) {
        this.managedIdentityProfile = managedIdentityProfile;
        return this;
    }

    /**
     * Get the authorizationProfile property: Authorization profile with details of AAD user Ids and group Ids
     * authorized for data plane access.
     * 
     * @return the authorizationProfile value.
     */
    public AuthorizationProfile authorizationProfile() {
        return this.authorizationProfile;
    }

    /**
     * Set the authorizationProfile property: Authorization profile with details of AAD user Ids and group Ids
     * authorized for data plane access.
     * 
     * @param authorizationProfile the authorizationProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withAuthorizationProfile(AuthorizationProfile authorizationProfile) {
        this.authorizationProfile = authorizationProfile;
        return this;
    }

    /**
     * Get the secretsProfile property: The cluster secret profile.
     * 
     * @return the secretsProfile value.
     */
    public SecretsProfile secretsProfile() {
        return this.secretsProfile;
    }

    /**
     * Set the secretsProfile property: The cluster secret profile.
     * 
     * @param secretsProfile the secretsProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withSecretsProfile(SecretsProfile secretsProfile) {
        this.secretsProfile = secretsProfile;
        return this;
    }

    /**
     * Get the serviceConfigsProfiles property: The service configs profiles.
     * 
     * @return the serviceConfigsProfiles value.
     */
    public List<ClusterServiceConfigsProfile> serviceConfigsProfiles() {
        return this.serviceConfigsProfiles;
    }

    /**
     * Set the serviceConfigsProfiles property: The service configs profiles.
     * 
     * @param serviceConfigsProfiles the serviceConfigsProfiles value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withServiceConfigsProfiles(List<ClusterServiceConfigsProfile> serviceConfigsProfiles) {
        this.serviceConfigsProfiles = serviceConfigsProfiles;
        return this;
    }

    /**
     * Get the connectivityProfile property: Cluster connectivity profile.
     * 
     * @return the connectivityProfile value.
     */
    public ConnectivityProfile connectivityProfile() {
        return this.connectivityProfile;
    }

    /**
     * Get the clusterAccessProfile property: Cluster access profile.
     * 
     * @return the clusterAccessProfile value.
     */
    public ClusterAccessProfile clusterAccessProfile() {
        return this.clusterAccessProfile;
    }

    /**
     * Set the clusterAccessProfile property: Cluster access profile.
     * 
     * @param clusterAccessProfile the clusterAccessProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withClusterAccessProfile(ClusterAccessProfile clusterAccessProfile) {
        this.clusterAccessProfile = clusterAccessProfile;
        return this;
    }

    /**
     * Get the logAnalyticsProfile property: Cluster log analytics profile to enable or disable OMS agent for cluster.
     * 
     * @return the logAnalyticsProfile value.
     */
    public ClusterLogAnalyticsProfile logAnalyticsProfile() {
        return this.logAnalyticsProfile;
    }

    /**
     * Set the logAnalyticsProfile property: Cluster log analytics profile to enable or disable OMS agent for cluster.
     * 
     * @param logAnalyticsProfile the logAnalyticsProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withLogAnalyticsProfile(ClusterLogAnalyticsProfile logAnalyticsProfile) {
        this.logAnalyticsProfile = logAnalyticsProfile;
        return this;
    }

    /**
     * Get the prometheusProfile property: Cluster Prometheus profile.
     * 
     * @return the prometheusProfile value.
     */
    public ClusterPrometheusProfile prometheusProfile() {
        return this.prometheusProfile;
    }

    /**
     * Set the prometheusProfile property: Cluster Prometheus profile.
     * 
     * @param prometheusProfile the prometheusProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withPrometheusProfile(ClusterPrometheusProfile prometheusProfile) {
        this.prometheusProfile = prometheusProfile;
        return this;
    }

    /**
     * Get the sshProfile property: Ssh profile for the cluster.
     * 
     * @return the sshProfile value.
     */
    public SshProfile sshProfile() {
        return this.sshProfile;
    }

    /**
     * Set the sshProfile property: Ssh profile for the cluster.
     * 
     * @param sshProfile the sshProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withSshProfile(SshProfile sshProfile) {
        this.sshProfile = sshProfile;
        return this;
    }

    /**
     * Get the autoscaleProfile property: This is the Autoscale profile for the cluster. This will allow customer to
     * create cluster enabled with Autoscale.
     * 
     * @return the autoscaleProfile value.
     */
    public AutoscaleProfile autoscaleProfile() {
        return this.autoscaleProfile;
    }

    /**
     * Set the autoscaleProfile property: This is the Autoscale profile for the cluster. This will allow customer to
     * create cluster enabled with Autoscale.
     * 
     * @param autoscaleProfile the autoscaleProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withAutoscaleProfile(AutoscaleProfile autoscaleProfile) {
        this.autoscaleProfile = autoscaleProfile;
        return this;
    }

    /**
     * Get the rangerPluginProfile property: Cluster Ranger plugin profile.
     * 
     * @return the rangerPluginProfile value.
     */
    public ClusterRangerPluginProfile rangerPluginProfile() {
        return this.rangerPluginProfile;
    }

    /**
     * Set the rangerPluginProfile property: Cluster Ranger plugin profile.
     * 
     * @param rangerPluginProfile the rangerPluginProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withRangerPluginProfile(ClusterRangerPluginProfile rangerPluginProfile) {
        this.rangerPluginProfile = rangerPluginProfile;
        return this;
    }

    /**
     * Get the kafkaProfile property: The Kafka cluster profile.
     * 
     * @return the kafkaProfile value.
     */
    public KafkaProfile kafkaProfile() {
        return this.kafkaProfile;
    }

    /**
     * Set the kafkaProfile property: The Kafka cluster profile.
     * 
     * @param kafkaProfile the kafkaProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withKafkaProfile(KafkaProfile kafkaProfile) {
        this.kafkaProfile = kafkaProfile;
        return this;
    }

    /**
     * Get the trinoProfile property: Trino Cluster profile.
     * 
     * @return the trinoProfile value.
     */
    public TrinoProfile trinoProfile() {
        return this.trinoProfile;
    }

    /**
     * Set the trinoProfile property: Trino Cluster profile.
     * 
     * @param trinoProfile the trinoProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withTrinoProfile(TrinoProfile trinoProfile) {
        this.trinoProfile = trinoProfile;
        return this;
    }

    /**
     * Get the llapProfile property: LLAP cluster profile.
     * 
     * @return the llapProfile value.
     */
    public Map<String, Object> llapProfile() {
        return this.llapProfile;
    }

    /**
     * Set the llapProfile property: LLAP cluster profile.
     * 
     * @param llapProfile the llapProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withLlapProfile(Map<String, Object> llapProfile) {
        this.llapProfile = llapProfile;
        return this;
    }

    /**
     * Get the flinkProfile property: The Flink cluster profile.
     * 
     * @return the flinkProfile value.
     */
    public FlinkProfile flinkProfile() {
        return this.flinkProfile;
    }

    /**
     * Set the flinkProfile property: The Flink cluster profile.
     * 
     * @param flinkProfile the flinkProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withFlinkProfile(FlinkProfile flinkProfile) {
        this.flinkProfile = flinkProfile;
        return this;
    }

    /**
     * Get the sparkProfile property: The spark cluster profile.
     * 
     * @return the sparkProfile value.
     */
    public SparkProfile sparkProfile() {
        return this.sparkProfile;
    }

    /**
     * Set the sparkProfile property: The spark cluster profile.
     * 
     * @param sparkProfile the sparkProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withSparkProfile(SparkProfile sparkProfile) {
        this.sparkProfile = sparkProfile;
        return this;
    }

    /**
     * Get the rangerProfile property: The ranger cluster profile.
     * 
     * @return the rangerProfile value.
     */
    public RangerProfile rangerProfile() {
        return this.rangerProfile;
    }

    /**
     * Set the rangerProfile property: The ranger cluster profile.
     * 
     * @param rangerProfile the rangerProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withRangerProfile(RangerProfile rangerProfile) {
        this.rangerProfile = rangerProfile;
        return this;
    }

    /**
     * Get the stubProfile property: Stub cluster profile.
     * 
     * @return the stubProfile value.
     */
    public Map<String, Object> stubProfile() {
        return this.stubProfile;
    }

    /**
     * Set the stubProfile property: Stub cluster profile.
     * 
     * @param stubProfile the stubProfile value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withStubProfile(Map<String, Object> stubProfile) {
        this.stubProfile = stubProfile;
        return this;
    }

    /**
     * Get the scriptActionProfiles property: The script action profile list.
     * 
     * @return the scriptActionProfiles value.
     */
    public List<ScriptActionProfile> scriptActionProfiles() {
        return this.scriptActionProfiles;
    }

    /**
     * Set the scriptActionProfiles property: The script action profile list.
     * 
     * @param scriptActionProfiles the scriptActionProfiles value to set.
     * @return the ClusterProfile object itself.
     */
    public ClusterProfile withScriptActionProfiles(List<ScriptActionProfile> scriptActionProfiles) {
        this.scriptActionProfiles = scriptActionProfiles;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (clusterVersion() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property clusterVersion in model ClusterProfile"));
        }
        if (ossVersion() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property ossVersion in model ClusterProfile"));
        }
        if (components() != null) {
            components().forEach(e -> e.validate());
        }
        if (identityProfile() != null) {
            identityProfile().validate();
        }
        if (managedIdentityProfile() != null) {
            managedIdentityProfile().validate();
        }
        if (authorizationProfile() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property authorizationProfile in model ClusterProfile"));
        } else {
            authorizationProfile().validate();
        }
        if (secretsProfile() != null) {
            secretsProfile().validate();
        }
        if (serviceConfigsProfiles() != null) {
            serviceConfigsProfiles().forEach(e -> e.validate());
        }
        if (connectivityProfile() != null) {
            connectivityProfile().validate();
        }
        if (clusterAccessProfile() != null) {
            clusterAccessProfile().validate();
        }
        if (logAnalyticsProfile() != null) {
            logAnalyticsProfile().validate();
        }
        if (prometheusProfile() != null) {
            prometheusProfile().validate();
        }
        if (sshProfile() != null) {
            sshProfile().validate();
        }
        if (autoscaleProfile() != null) {
            autoscaleProfile().validate();
        }
        if (rangerPluginProfile() != null) {
            rangerPluginProfile().validate();
        }
        if (kafkaProfile() != null) {
            kafkaProfile().validate();
        }
        if (trinoProfile() != null) {
            trinoProfile().validate();
        }
        if (flinkProfile() != null) {
            flinkProfile().validate();
        }
        if (sparkProfile() != null) {
            sparkProfile().validate();
        }
        if (rangerProfile() != null) {
            rangerProfile().validate();
        }
        if (scriptActionProfiles() != null) {
            scriptActionProfiles().forEach(e -> e.validate());
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(ClusterProfile.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("clusterVersion", this.clusterVersion);
        jsonWriter.writeStringField("ossVersion", this.ossVersion);
        jsonWriter.writeJsonField("authorizationProfile", this.authorizationProfile);
        jsonWriter.writeJsonField("identityProfile", this.identityProfile);
        jsonWriter.writeJsonField("managedIdentityProfile", this.managedIdentityProfile);
        jsonWriter.writeJsonField("secretsProfile", this.secretsProfile);
        jsonWriter.writeArrayField("serviceConfigsProfiles", this.serviceConfigsProfiles,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("clusterAccessProfile", this.clusterAccessProfile);
        jsonWriter.writeJsonField("logAnalyticsProfile", this.logAnalyticsProfile);
        jsonWriter.writeJsonField("prometheusProfile", this.prometheusProfile);
        jsonWriter.writeJsonField("sshProfile", this.sshProfile);
        jsonWriter.writeJsonField("autoscaleProfile", this.autoscaleProfile);
        jsonWriter.writeJsonField("rangerPluginProfile", this.rangerPluginProfile);
        jsonWriter.writeJsonField("kafkaProfile", this.kafkaProfile);
        jsonWriter.writeJsonField("trinoProfile", this.trinoProfile);
        jsonWriter.writeMapField("llapProfile", this.llapProfile, (writer, element) -> writer.writeUntyped(element));
        jsonWriter.writeJsonField("flinkProfile", this.flinkProfile);
        jsonWriter.writeJsonField("sparkProfile", this.sparkProfile);
        jsonWriter.writeJsonField("rangerProfile", this.rangerProfile);
        jsonWriter.writeMapField("stubProfile", this.stubProfile, (writer, element) -> writer.writeUntyped(element));
        jsonWriter.writeArrayField("scriptActionProfiles", this.scriptActionProfiles,
            (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ClusterProfile from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ClusterProfile if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ClusterProfile.
     */
    public static ClusterProfile fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ClusterProfile deserializedClusterProfile = new ClusterProfile();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("clusterVersion".equals(fieldName)) {
                    deserializedClusterProfile.clusterVersion = reader.getString();
                } else if ("ossVersion".equals(fieldName)) {
                    deserializedClusterProfile.ossVersion = reader.getString();
                } else if ("authorizationProfile".equals(fieldName)) {
                    deserializedClusterProfile.authorizationProfile = AuthorizationProfile.fromJson(reader);
                } else if ("components".equals(fieldName)) {
                    List<ClusterComponentsItem> components
                        = reader.readArray(reader1 -> ClusterComponentsItem.fromJson(reader1));
                    deserializedClusterProfile.components = components;
                } else if ("identityProfile".equals(fieldName)) {
                    deserializedClusterProfile.identityProfile = IdentityProfile.fromJson(reader);
                } else if ("managedIdentityProfile".equals(fieldName)) {
                    deserializedClusterProfile.managedIdentityProfile = ManagedIdentityProfile.fromJson(reader);
                } else if ("secretsProfile".equals(fieldName)) {
                    deserializedClusterProfile.secretsProfile = SecretsProfile.fromJson(reader);
                } else if ("serviceConfigsProfiles".equals(fieldName)) {
                    List<ClusterServiceConfigsProfile> serviceConfigsProfiles
                        = reader.readArray(reader1 -> ClusterServiceConfigsProfile.fromJson(reader1));
                    deserializedClusterProfile.serviceConfigsProfiles = serviceConfigsProfiles;
                } else if ("connectivityProfile".equals(fieldName)) {
                    deserializedClusterProfile.connectivityProfile = ConnectivityProfile.fromJson(reader);
                } else if ("clusterAccessProfile".equals(fieldName)) {
                    deserializedClusterProfile.clusterAccessProfile = ClusterAccessProfile.fromJson(reader);
                } else if ("logAnalyticsProfile".equals(fieldName)) {
                    deserializedClusterProfile.logAnalyticsProfile = ClusterLogAnalyticsProfile.fromJson(reader);
                } else if ("prometheusProfile".equals(fieldName)) {
                    deserializedClusterProfile.prometheusProfile = ClusterPrometheusProfile.fromJson(reader);
                } else if ("sshProfile".equals(fieldName)) {
                    deserializedClusterProfile.sshProfile = SshProfile.fromJson(reader);
                } else if ("autoscaleProfile".equals(fieldName)) {
                    deserializedClusterProfile.autoscaleProfile = AutoscaleProfile.fromJson(reader);
                } else if ("rangerPluginProfile".equals(fieldName)) {
                    deserializedClusterProfile.rangerPluginProfile = ClusterRangerPluginProfile.fromJson(reader);
                } else if ("kafkaProfile".equals(fieldName)) {
                    deserializedClusterProfile.kafkaProfile = KafkaProfile.fromJson(reader);
                } else if ("trinoProfile".equals(fieldName)) {
                    deserializedClusterProfile.trinoProfile = TrinoProfile.fromJson(reader);
                } else if ("llapProfile".equals(fieldName)) {
                    Map<String, Object> llapProfile = reader.readMap(reader1 -> reader1.readUntyped());
                    deserializedClusterProfile.llapProfile = llapProfile;
                } else if ("flinkProfile".equals(fieldName)) {
                    deserializedClusterProfile.flinkProfile = FlinkProfile.fromJson(reader);
                } else if ("sparkProfile".equals(fieldName)) {
                    deserializedClusterProfile.sparkProfile = SparkProfile.fromJson(reader);
                } else if ("rangerProfile".equals(fieldName)) {
                    deserializedClusterProfile.rangerProfile = RangerProfile.fromJson(reader);
                } else if ("stubProfile".equals(fieldName)) {
                    Map<String, Object> stubProfile = reader.readMap(reader1 -> reader1.readUntyped());
                    deserializedClusterProfile.stubProfile = stubProfile;
                } else if ("scriptActionProfiles".equals(fieldName)) {
                    List<ScriptActionProfile> scriptActionProfiles
                        = reader.readArray(reader1 -> ScriptActionProfile.fromJson(reader1));
                    deserializedClusterProfile.scriptActionProfiles = scriptActionProfiles;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedClusterProfile;
        });
    }
}
