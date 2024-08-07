// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.sql.models.NetworkIsolationSettings;
import com.azure.resourcemanager.sql.models.OperationMode;
import com.azure.resourcemanager.sql.models.StorageKeyType;
import java.io.IOException;

/**
 * Contains the database information after a successful Import, Export, or PolybaseImport.
 */
@Fluent
public final class DatabaseExtensionsProperties implements JsonSerializable<DatabaseExtensionsProperties> {
    /*
     * Operation mode of the operation: Import, Export, or PolybaseImport.
     */
    private OperationMode operationMode;

    /*
     * Storage key type: StorageAccessKey or SharedAccessKey.
     */
    private StorageKeyType storageKeyType;

    /*
     * Storage key for the storage account.
     */
    private String storageKey;

    /*
     * Storage Uri for the storage account.
     */
    private String storageUri;

    /*
     * Administrator login name.
     */
    private String administratorLogin;

    /*
     * Administrator login password.
     */
    private String administratorLoginPassword;

    /*
     * Authentication type: SQL authentication or AD password.
     */
    private String authenticationType;

    /*
     * Database edition for the newly created database in the case of an import operation.
     */
    private String databaseEdition;

    /*
     * Database service level objective for the newly created database in the case of an import operation.
     */
    private String serviceObjectiveName;

    /*
     * Database max size in bytes for the newly created database in the case of an import operation.
     */
    private String maxSizeBytes;

    /*
     * Optional resource information to enable network isolation for request.
     */
    private NetworkIsolationSettings networkIsolation;

    /**
     * Creates an instance of DatabaseExtensionsProperties class.
     */
    public DatabaseExtensionsProperties() {
    }

    /**
     * Get the operationMode property: Operation mode of the operation: Import, Export, or PolybaseImport.
     * 
     * @return the operationMode value.
     */
    public OperationMode operationMode() {
        return this.operationMode;
    }

    /**
     * Set the operationMode property: Operation mode of the operation: Import, Export, or PolybaseImport.
     * 
     * @param operationMode the operationMode value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withOperationMode(OperationMode operationMode) {
        this.operationMode = operationMode;
        return this;
    }

    /**
     * Get the storageKeyType property: Storage key type: StorageAccessKey or SharedAccessKey.
     * 
     * @return the storageKeyType value.
     */
    public StorageKeyType storageKeyType() {
        return this.storageKeyType;
    }

    /**
     * Set the storageKeyType property: Storage key type: StorageAccessKey or SharedAccessKey.
     * 
     * @param storageKeyType the storageKeyType value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withStorageKeyType(StorageKeyType storageKeyType) {
        this.storageKeyType = storageKeyType;
        return this;
    }

    /**
     * Get the storageKey property: Storage key for the storage account.
     * 
     * @return the storageKey value.
     */
    public String storageKey() {
        return this.storageKey;
    }

    /**
     * Set the storageKey property: Storage key for the storage account.
     * 
     * @param storageKey the storageKey value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withStorageKey(String storageKey) {
        this.storageKey = storageKey;
        return this;
    }

    /**
     * Get the storageUri property: Storage Uri for the storage account.
     * 
     * @return the storageUri value.
     */
    public String storageUri() {
        return this.storageUri;
    }

    /**
     * Set the storageUri property: Storage Uri for the storage account.
     * 
     * @param storageUri the storageUri value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withStorageUri(String storageUri) {
        this.storageUri = storageUri;
        return this;
    }

    /**
     * Get the administratorLogin property: Administrator login name.
     * 
     * @return the administratorLogin value.
     */
    public String administratorLogin() {
        return this.administratorLogin;
    }

    /**
     * Set the administratorLogin property: Administrator login name.
     * 
     * @param administratorLogin the administratorLogin value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withAdministratorLogin(String administratorLogin) {
        this.administratorLogin = administratorLogin;
        return this;
    }

    /**
     * Get the administratorLoginPassword property: Administrator login password.
     * 
     * @return the administratorLoginPassword value.
     */
    public String administratorLoginPassword() {
        return this.administratorLoginPassword;
    }

    /**
     * Set the administratorLoginPassword property: Administrator login password.
     * 
     * @param administratorLoginPassword the administratorLoginPassword value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withAdministratorLoginPassword(String administratorLoginPassword) {
        this.administratorLoginPassword = administratorLoginPassword;
        return this;
    }

    /**
     * Get the authenticationType property: Authentication type: SQL authentication or AD password.
     * 
     * @return the authenticationType value.
     */
    public String authenticationType() {
        return this.authenticationType;
    }

    /**
     * Set the authenticationType property: Authentication type: SQL authentication or AD password.
     * 
     * @param authenticationType the authenticationType value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
        return this;
    }

    /**
     * Get the databaseEdition property: Database edition for the newly created database in the case of an import
     * operation.
     * 
     * @return the databaseEdition value.
     */
    public String databaseEdition() {
        return this.databaseEdition;
    }

    /**
     * Set the databaseEdition property: Database edition for the newly created database in the case of an import
     * operation.
     * 
     * @param databaseEdition the databaseEdition value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withDatabaseEdition(String databaseEdition) {
        this.databaseEdition = databaseEdition;
        return this;
    }

    /**
     * Get the serviceObjectiveName property: Database service level objective for the newly created database in the
     * case of an import operation.
     * 
     * @return the serviceObjectiveName value.
     */
    public String serviceObjectiveName() {
        return this.serviceObjectiveName;
    }

    /**
     * Set the serviceObjectiveName property: Database service level objective for the newly created database in the
     * case of an import operation.
     * 
     * @param serviceObjectiveName the serviceObjectiveName value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withServiceObjectiveName(String serviceObjectiveName) {
        this.serviceObjectiveName = serviceObjectiveName;
        return this;
    }

    /**
     * Get the maxSizeBytes property: Database max size in bytes for the newly created database in the case of an import
     * operation.
     * 
     * @return the maxSizeBytes value.
     */
    public String maxSizeBytes() {
        return this.maxSizeBytes;
    }

    /**
     * Set the maxSizeBytes property: Database max size in bytes for the newly created database in the case of an import
     * operation.
     * 
     * @param maxSizeBytes the maxSizeBytes value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withMaxSizeBytes(String maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
        return this;
    }

    /**
     * Get the networkIsolation property: Optional resource information to enable network isolation for request.
     * 
     * @return the networkIsolation value.
     */
    public NetworkIsolationSettings networkIsolation() {
        return this.networkIsolation;
    }

    /**
     * Set the networkIsolation property: Optional resource information to enable network isolation for request.
     * 
     * @param networkIsolation the networkIsolation value to set.
     * @return the DatabaseExtensionsProperties object itself.
     */
    public DatabaseExtensionsProperties withNetworkIsolation(NetworkIsolationSettings networkIsolation) {
        this.networkIsolation = networkIsolation;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (operationMode() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property operationMode in model DatabaseExtensionsProperties"));
        }
        if (storageKeyType() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property storageKeyType in model DatabaseExtensionsProperties"));
        }
        if (storageKey() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property storageKey in model DatabaseExtensionsProperties"));
        }
        if (storageUri() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property storageUri in model DatabaseExtensionsProperties"));
        }
        if (networkIsolation() != null) {
            networkIsolation().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(DatabaseExtensionsProperties.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("operationMode", this.operationMode == null ? null : this.operationMode.toString());
        jsonWriter.writeStringField("storageKeyType",
            this.storageKeyType == null ? null : this.storageKeyType.toString());
        jsonWriter.writeStringField("storageKey", this.storageKey);
        jsonWriter.writeStringField("storageUri", this.storageUri);
        jsonWriter.writeStringField("administratorLogin", this.administratorLogin);
        jsonWriter.writeStringField("administratorLoginPassword", this.administratorLoginPassword);
        jsonWriter.writeStringField("authenticationType", this.authenticationType);
        jsonWriter.writeStringField("databaseEdition", this.databaseEdition);
        jsonWriter.writeStringField("serviceObjectiveName", this.serviceObjectiveName);
        jsonWriter.writeStringField("maxSizeBytes", this.maxSizeBytes);
        jsonWriter.writeJsonField("networkIsolation", this.networkIsolation);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DatabaseExtensionsProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DatabaseExtensionsProperties if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DatabaseExtensionsProperties.
     */
    public static DatabaseExtensionsProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DatabaseExtensionsProperties deserializedDatabaseExtensionsProperties = new DatabaseExtensionsProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("operationMode".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.operationMode
                        = OperationMode.fromString(reader.getString());
                } else if ("storageKeyType".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.storageKeyType
                        = StorageKeyType.fromString(reader.getString());
                } else if ("storageKey".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.storageKey = reader.getString();
                } else if ("storageUri".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.storageUri = reader.getString();
                } else if ("administratorLogin".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.administratorLogin = reader.getString();
                } else if ("administratorLoginPassword".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.administratorLoginPassword = reader.getString();
                } else if ("authenticationType".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.authenticationType = reader.getString();
                } else if ("databaseEdition".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.databaseEdition = reader.getString();
                } else if ("serviceObjectiveName".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.serviceObjectiveName = reader.getString();
                } else if ("maxSizeBytes".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.maxSizeBytes = reader.getString();
                } else if ("networkIsolation".equals(fieldName)) {
                    deserializedDatabaseExtensionsProperties.networkIsolation
                        = NetworkIsolationSettings.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDatabaseExtensionsProperties;
        });
    }
}
