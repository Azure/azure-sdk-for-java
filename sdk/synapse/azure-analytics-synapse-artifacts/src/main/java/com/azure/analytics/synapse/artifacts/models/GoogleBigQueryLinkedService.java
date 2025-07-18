// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.synapse.artifacts.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Google BigQuery service linked service.
 */
@Fluent
public class GoogleBigQueryLinkedService extends LinkedService {
    /*
     * Type of linked service.
     */
    @Generated
    private String type = "GoogleBigQuery";

    /*
     * The default BigQuery project to query against.
     */
    @Generated
    private Object project;

    /*
     * A comma-separated list of public BigQuery projects to access.
     */
    @Generated
    private Object additionalProjects;

    /*
     * Whether to request access to Google Drive. Allowing Google Drive access enables support for federated tables that
     * combine BigQuery data with data from Google Drive. The default value is false.
     */
    @Generated
    private Object requestGoogleDriveScope;

    /*
     * The OAuth 2.0 authentication mechanism used for authentication. ServiceAuthentication can only be used on
     * self-hosted IR.
     */
    @Generated
    private GoogleBigQueryAuthenticationType authenticationType;

    /*
     * The refresh token obtained from Google for authorizing access to BigQuery for UserAuthentication.
     */
    @Generated
    private SecretBase refreshToken;

    /*
     * The client id of the google application used to acquire the refresh token. Type: string (or Expression with
     * resultType string).
     */
    @Generated
    private Object clientId;

    /*
     * The client secret of the google application used to acquire the refresh token.
     */
    @Generated
    private SecretBase clientSecret;

    /*
     * The service account email ID that is used for ServiceAuthentication and can only be used on self-hosted IR.
     */
    @Generated
    private Object email;

    /*
     * The full path to the .p12 key file that is used to authenticate the service account email address and can only be
     * used on self-hosted IR.
     */
    @Generated
    private Object keyFilePath;

    /*
     * The full path of the .pem file containing trusted CA certificates for verifying the server when connecting over
     * SSL. This property can only be set when using SSL on self-hosted IR. The default value is the cacerts.pem file
     * installed with the IR.
     */
    @Generated
    private Object trustedCertPath;

    /*
     * Specifies whether to use a CA certificate from the system trust store or from a specified PEM file. The default
     * value is false.
     */
    @Generated
    private Object useSystemTrustStore;

    /*
     * The encrypted credential used for authentication. Credentials are encrypted using the integration runtime
     * credential manager. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object encryptedCredential;

    /**
     * Creates an instance of GoogleBigQueryLinkedService class.
     */
    @Generated
    public GoogleBigQueryLinkedService() {
    }

    /**
     * Get the type property: Type of linked service.
     * 
     * @return the type value.
     */
    @Generated
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * Get the project property: The default BigQuery project to query against.
     * 
     * @return the project value.
     */
    @Generated
    public Object getProject() {
        return this.project;
    }

    /**
     * Set the project property: The default BigQuery project to query against.
     * 
     * @param project the project value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setProject(Object project) {
        this.project = project;
        return this;
    }

    /**
     * Get the additionalProjects property: A comma-separated list of public BigQuery projects to access.
     * 
     * @return the additionalProjects value.
     */
    @Generated
    public Object getAdditionalProjects() {
        return this.additionalProjects;
    }

    /**
     * Set the additionalProjects property: A comma-separated list of public BigQuery projects to access.
     * 
     * @param additionalProjects the additionalProjects value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setAdditionalProjects(Object additionalProjects) {
        this.additionalProjects = additionalProjects;
        return this;
    }

    /**
     * Get the requestGoogleDriveScope property: Whether to request access to Google Drive. Allowing Google Drive access
     * enables support for federated tables that combine BigQuery data with data from Google Drive. The default value is
     * false.
     * 
     * @return the requestGoogleDriveScope value.
     */
    @Generated
    public Object getRequestGoogleDriveScope() {
        return this.requestGoogleDriveScope;
    }

    /**
     * Set the requestGoogleDriveScope property: Whether to request access to Google Drive. Allowing Google Drive access
     * enables support for federated tables that combine BigQuery data with data from Google Drive. The default value is
     * false.
     * 
     * @param requestGoogleDriveScope the requestGoogleDriveScope value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setRequestGoogleDriveScope(Object requestGoogleDriveScope) {
        this.requestGoogleDriveScope = requestGoogleDriveScope;
        return this;
    }

    /**
     * Get the authenticationType property: The OAuth 2.0 authentication mechanism used for authentication.
     * ServiceAuthentication can only be used on self-hosted IR.
     * 
     * @return the authenticationType value.
     */
    @Generated
    public GoogleBigQueryAuthenticationType getAuthenticationType() {
        return this.authenticationType;
    }

    /**
     * Set the authenticationType property: The OAuth 2.0 authentication mechanism used for authentication.
     * ServiceAuthentication can only be used on self-hosted IR.
     * 
     * @param authenticationType the authenticationType value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setAuthenticationType(GoogleBigQueryAuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
        return this;
    }

    /**
     * Get the refreshToken property: The refresh token obtained from Google for authorizing access to BigQuery for
     * UserAuthentication.
     * 
     * @return the refreshToken value.
     */
    @Generated
    public SecretBase getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * Set the refreshToken property: The refresh token obtained from Google for authorizing access to BigQuery for
     * UserAuthentication.
     * 
     * @param refreshToken the refreshToken value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setRefreshToken(SecretBase refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    /**
     * Get the clientId property: The client id of the google application used to acquire the refresh token. Type:
     * string (or Expression with resultType string).
     * 
     * @return the clientId value.
     */
    @Generated
    public Object getClientId() {
        return this.clientId;
    }

    /**
     * Set the clientId property: The client id of the google application used to acquire the refresh token. Type:
     * string (or Expression with resultType string).
     * 
     * @param clientId the clientId value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setClientId(Object clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get the clientSecret property: The client secret of the google application used to acquire the refresh token.
     * 
     * @return the clientSecret value.
     */
    @Generated
    public SecretBase getClientSecret() {
        return this.clientSecret;
    }

    /**
     * Set the clientSecret property: The client secret of the google application used to acquire the refresh token.
     * 
     * @param clientSecret the clientSecret value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setClientSecret(SecretBase clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Get the email property: The service account email ID that is used for ServiceAuthentication and can only be used
     * on self-hosted IR.
     * 
     * @return the email value.
     */
    @Generated
    public Object getEmail() {
        return this.email;
    }

    /**
     * Set the email property: The service account email ID that is used for ServiceAuthentication and can only be used
     * on self-hosted IR.
     * 
     * @param email the email value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setEmail(Object email) {
        this.email = email;
        return this;
    }

    /**
     * Get the keyFilePath property: The full path to the .p12 key file that is used to authenticate the service account
     * email address and can only be used on self-hosted IR.
     * 
     * @return the keyFilePath value.
     */
    @Generated
    public Object getKeyFilePath() {
        return this.keyFilePath;
    }

    /**
     * Set the keyFilePath property: The full path to the .p12 key file that is used to authenticate the service account
     * email address and can only be used on self-hosted IR.
     * 
     * @param keyFilePath the keyFilePath value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setKeyFilePath(Object keyFilePath) {
        this.keyFilePath = keyFilePath;
        return this;
    }

    /**
     * Get the trustedCertPath property: The full path of the .pem file containing trusted CA certificates for verifying
     * the server when connecting over SSL. This property can only be set when using SSL on self-hosted IR. The default
     * value is the cacerts.pem file installed with the IR.
     * 
     * @return the trustedCertPath value.
     */
    @Generated
    public Object getTrustedCertPath() {
        return this.trustedCertPath;
    }

    /**
     * Set the trustedCertPath property: The full path of the .pem file containing trusted CA certificates for verifying
     * the server when connecting over SSL. This property can only be set when using SSL on self-hosted IR. The default
     * value is the cacerts.pem file installed with the IR.
     * 
     * @param trustedCertPath the trustedCertPath value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setTrustedCertPath(Object trustedCertPath) {
        this.trustedCertPath = trustedCertPath;
        return this;
    }

    /**
     * Get the useSystemTrustStore property: Specifies whether to use a CA certificate from the system trust store or
     * from a specified PEM file. The default value is false.
     * 
     * @return the useSystemTrustStore value.
     */
    @Generated
    public Object getUseSystemTrustStore() {
        return this.useSystemTrustStore;
    }

    /**
     * Set the useSystemTrustStore property: Specifies whether to use a CA certificate from the system trust store or
     * from a specified PEM file. The default value is false.
     * 
     * @param useSystemTrustStore the useSystemTrustStore value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setUseSystemTrustStore(Object useSystemTrustStore) {
        this.useSystemTrustStore = useSystemTrustStore;
        return this;
    }

    /**
     * Get the encryptedCredential property: The encrypted credential used for authentication. Credentials are encrypted
     * using the integration runtime credential manager. Type: string (or Expression with resultType string).
     * 
     * @return the encryptedCredential value.
     */
    @Generated
    public Object getEncryptedCredential() {
        return this.encryptedCredential;
    }

    /**
     * Set the encryptedCredential property: The encrypted credential used for authentication. Credentials are encrypted
     * using the integration runtime credential manager. Type: string (or Expression with resultType string).
     * 
     * @param encryptedCredential the encryptedCredential value to set.
     * @return the GoogleBigQueryLinkedService object itself.
     */
    @Generated
    public GoogleBigQueryLinkedService setEncryptedCredential(Object encryptedCredential) {
        this.encryptedCredential = encryptedCredential;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public GoogleBigQueryLinkedService setVersion(String version) {
        super.setVersion(version);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public GoogleBigQueryLinkedService setConnectVia(IntegrationRuntimeReference connectVia) {
        super.setConnectVia(connectVia);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public GoogleBigQueryLinkedService setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public GoogleBigQueryLinkedService setParameters(Map<String, ParameterSpecification> parameters) {
        super.setParameters(parameters);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public GoogleBigQueryLinkedService setAnnotations(List<Object> annotations) {
        super.setAnnotations(annotations);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("version", getVersion());
        jsonWriter.writeJsonField("connectVia", getConnectVia());
        jsonWriter.writeStringField("description", getDescription());
        jsonWriter.writeMapField("parameters", getParameters(), (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("annotations", getAnnotations(), (writer, element) -> writer.writeUntyped(element));
        jsonWriter.writeStringField("type", this.type);
        if (project != null
            || additionalProjects != null
            || requestGoogleDriveScope != null
            || authenticationType != null
            || refreshToken != null
            || clientId != null
            || clientSecret != null
            || email != null
            || keyFilePath != null
            || trustedCertPath != null
            || useSystemTrustStore != null
            || encryptedCredential != null) {
            jsonWriter.writeStartObject("typeProperties");
            jsonWriter.writeUntypedField("project", this.project);
            if (this.additionalProjects != null) {
                jsonWriter.writeUntypedField("additionalProjects", this.additionalProjects);
            }
            if (this.requestGoogleDriveScope != null) {
                jsonWriter.writeUntypedField("requestGoogleDriveScope", this.requestGoogleDriveScope);
            }
            jsonWriter.writeStringField("authenticationType",
                this.authenticationType == null ? null : this.authenticationType.toString());
            jsonWriter.writeJsonField("refreshToken", this.refreshToken);
            if (this.clientId != null) {
                jsonWriter.writeUntypedField("clientId", this.clientId);
            }
            jsonWriter.writeJsonField("clientSecret", this.clientSecret);
            if (this.email != null) {
                jsonWriter.writeUntypedField("email", this.email);
            }
            if (this.keyFilePath != null) {
                jsonWriter.writeUntypedField("keyFilePath", this.keyFilePath);
            }
            if (this.trustedCertPath != null) {
                jsonWriter.writeUntypedField("trustedCertPath", this.trustedCertPath);
            }
            if (this.useSystemTrustStore != null) {
                jsonWriter.writeUntypedField("useSystemTrustStore", this.useSystemTrustStore);
            }
            if (this.encryptedCredential != null) {
                jsonWriter.writeUntypedField("encryptedCredential", this.encryptedCredential);
            }
            jsonWriter.writeEndObject();
        }
        if (getAdditionalProperties() != null) {
            for (Map.Entry<String, Object> additionalProperty : getAdditionalProperties().entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of GoogleBigQueryLinkedService from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of GoogleBigQueryLinkedService if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the GoogleBigQueryLinkedService.
     */
    @Generated
    public static GoogleBigQueryLinkedService fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            GoogleBigQueryLinkedService deserializedGoogleBigQueryLinkedService = new GoogleBigQueryLinkedService();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("version".equals(fieldName)) {
                    deserializedGoogleBigQueryLinkedService.setVersion(reader.getString());
                } else if ("connectVia".equals(fieldName)) {
                    deserializedGoogleBigQueryLinkedService.setConnectVia(IntegrationRuntimeReference.fromJson(reader));
                } else if ("description".equals(fieldName)) {
                    deserializedGoogleBigQueryLinkedService.setDescription(reader.getString());
                } else if ("parameters".equals(fieldName)) {
                    Map<String, ParameterSpecification> parameters
                        = reader.readMap(reader1 -> ParameterSpecification.fromJson(reader1));
                    deserializedGoogleBigQueryLinkedService.setParameters(parameters);
                } else if ("annotations".equals(fieldName)) {
                    List<Object> annotations = reader.readArray(reader1 -> reader1.readUntyped());
                    deserializedGoogleBigQueryLinkedService.setAnnotations(annotations);
                } else if ("type".equals(fieldName)) {
                    deserializedGoogleBigQueryLinkedService.type = reader.getString();
                } else if ("typeProperties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("project".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.project = reader.readUntyped();
                        } else if ("additionalProjects".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.additionalProjects = reader.readUntyped();
                        } else if ("requestGoogleDriveScope".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.requestGoogleDriveScope = reader.readUntyped();
                        } else if ("authenticationType".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.authenticationType
                                = GoogleBigQueryAuthenticationType.fromString(reader.getString());
                        } else if ("refreshToken".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.refreshToken = SecretBase.fromJson(reader);
                        } else if ("clientId".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.clientId = reader.readUntyped();
                        } else if ("clientSecret".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.clientSecret = SecretBase.fromJson(reader);
                        } else if ("email".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.email = reader.readUntyped();
                        } else if ("keyFilePath".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.keyFilePath = reader.readUntyped();
                        } else if ("trustedCertPath".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.trustedCertPath = reader.readUntyped();
                        } else if ("useSystemTrustStore".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.useSystemTrustStore = reader.readUntyped();
                        } else if ("encryptedCredential".equals(fieldName)) {
                            deserializedGoogleBigQueryLinkedService.encryptedCredential = reader.readUntyped();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            deserializedGoogleBigQueryLinkedService.setAdditionalProperties(additionalProperties);

            return deserializedGoogleBigQueryLinkedService;
        });
    }
}
