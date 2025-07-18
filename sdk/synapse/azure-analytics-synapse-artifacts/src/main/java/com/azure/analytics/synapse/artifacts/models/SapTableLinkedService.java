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
 * SAP Table Linked Service.
 */
@Fluent
public class SapTableLinkedService extends LinkedService {
    /*
     * Type of linked service.
     */
    @Generated
    private String type = "SapTable";

    /*
     * Host name of the SAP instance where the table is located. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object server;

    /*
     * System number of the SAP system where the table is located. (Usually a two-digit decimal number represented as a
     * string.) Type: string (or Expression with resultType string).
     */
    @Generated
    private Object systemNumber;

    /*
     * Client ID of the client on the SAP system where the table is located. (Usually a three-digit decimal number
     * represented as a string) Type: string (or Expression with resultType string).
     */
    @Generated
    private Object clientId;

    /*
     * Language of the SAP system where the table is located. The default value is EN. Type: string (or Expression with
     * resultType string).
     */
    @Generated
    private Object language;

    /*
     * SystemID of the SAP system where the table is located. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object systemId;

    /*
     * Username to access the SAP server where the table is located. Type: string (or Expression with resultType
     * string).
     */
    @Generated
    private Object userName;

    /*
     * Password to access the SAP server where the table is located.
     */
    @Generated
    private SecretBase password;

    /*
     * The hostname of the SAP Message Server. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object messageServer;

    /*
     * The service name or port number of the Message Server. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object messageServerService;

    /*
     * SNC activation flag (Boolean) to access the SAP server where the table is located. Type: boolean (or Expression
     * with resultType boolean).
     */
    @Generated
    private Object sncMode;

    /*
     * Initiator's SNC name to access the SAP server where the table is located. Type: string (or Expression with
     * resultType string).
     */
    @Generated
    private Object sncMyName;

    /*
     * Communication partner's SNC name to access the SAP server where the table is located. Type: string (or Expression
     * with resultType string).
     */
    @Generated
    private Object sncPartnerName;

    /*
     * External security product's library to access the SAP server where the table is located. Type: string (or
     * Expression with resultType string).
     */
    @Generated
    private Object sncLibraryPath;

    /*
     * SNC Quality of Protection. Allowed value include: 1, 2, 3, 8, 9. Type: string (or Expression with resultType
     * string).
     */
    @Generated
    private Object sncQop;

    /*
     * The Logon Group for the SAP System. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object logonGroup;

    /*
     * The encrypted credential used for authentication. Credentials are encrypted using the integration runtime
     * credential manager. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object encryptedCredential;

    /**
     * Creates an instance of SapTableLinkedService class.
     */
    @Generated
    public SapTableLinkedService() {
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
     * Get the server property: Host name of the SAP instance where the table is located. Type: string (or Expression
     * with resultType string).
     * 
     * @return the server value.
     */
    @Generated
    public Object getServer() {
        return this.server;
    }

    /**
     * Set the server property: Host name of the SAP instance where the table is located. Type: string (or Expression
     * with resultType string).
     * 
     * @param server the server value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setServer(Object server) {
        this.server = server;
        return this;
    }

    /**
     * Get the systemNumber property: System number of the SAP system where the table is located. (Usually a two-digit
     * decimal number represented as a string.) Type: string (or Expression with resultType string).
     * 
     * @return the systemNumber value.
     */
    @Generated
    public Object getSystemNumber() {
        return this.systemNumber;
    }

    /**
     * Set the systemNumber property: System number of the SAP system where the table is located. (Usually a two-digit
     * decimal number represented as a string.) Type: string (or Expression with resultType string).
     * 
     * @param systemNumber the systemNumber value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setSystemNumber(Object systemNumber) {
        this.systemNumber = systemNumber;
        return this;
    }

    /**
     * Get the clientId property: Client ID of the client on the SAP system where the table is located. (Usually a
     * three-digit decimal number represented as a string) Type: string (or Expression with resultType string).
     * 
     * @return the clientId value.
     */
    @Generated
    public Object getClientId() {
        return this.clientId;
    }

    /**
     * Set the clientId property: Client ID of the client on the SAP system where the table is located. (Usually a
     * three-digit decimal number represented as a string) Type: string (or Expression with resultType string).
     * 
     * @param clientId the clientId value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setClientId(Object clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get the language property: Language of the SAP system where the table is located. The default value is EN. Type:
     * string (or Expression with resultType string).
     * 
     * @return the language value.
     */
    @Generated
    public Object getLanguage() {
        return this.language;
    }

    /**
     * Set the language property: Language of the SAP system where the table is located. The default value is EN. Type:
     * string (or Expression with resultType string).
     * 
     * @param language the language value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setLanguage(Object language) {
        this.language = language;
        return this;
    }

    /**
     * Get the systemId property: SystemID of the SAP system where the table is located. Type: string (or Expression
     * with resultType string).
     * 
     * @return the systemId value.
     */
    @Generated
    public Object getSystemId() {
        return this.systemId;
    }

    /**
     * Set the systemId property: SystemID of the SAP system where the table is located. Type: string (or Expression
     * with resultType string).
     * 
     * @param systemId the systemId value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setSystemId(Object systemId) {
        this.systemId = systemId;
        return this;
    }

    /**
     * Get the userName property: Username to access the SAP server where the table is located. Type: string (or
     * Expression with resultType string).
     * 
     * @return the userName value.
     */
    @Generated
    public Object getUserName() {
        return this.userName;
    }

    /**
     * Set the userName property: Username to access the SAP server where the table is located. Type: string (or
     * Expression with resultType string).
     * 
     * @param userName the userName value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setUserName(Object userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Get the password property: Password to access the SAP server where the table is located.
     * 
     * @return the password value.
     */
    @Generated
    public SecretBase getPassword() {
        return this.password;
    }

    /**
     * Set the password property: Password to access the SAP server where the table is located.
     * 
     * @param password the password value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setPassword(SecretBase password) {
        this.password = password;
        return this;
    }

    /**
     * Get the messageServer property: The hostname of the SAP Message Server. Type: string (or Expression with
     * resultType string).
     * 
     * @return the messageServer value.
     */
    @Generated
    public Object getMessageServer() {
        return this.messageServer;
    }

    /**
     * Set the messageServer property: The hostname of the SAP Message Server. Type: string (or Expression with
     * resultType string).
     * 
     * @param messageServer the messageServer value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setMessageServer(Object messageServer) {
        this.messageServer = messageServer;
        return this;
    }

    /**
     * Get the messageServerService property: The service name or port number of the Message Server. Type: string (or
     * Expression with resultType string).
     * 
     * @return the messageServerService value.
     */
    @Generated
    public Object getMessageServerService() {
        return this.messageServerService;
    }

    /**
     * Set the messageServerService property: The service name or port number of the Message Server. Type: string (or
     * Expression with resultType string).
     * 
     * @param messageServerService the messageServerService value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setMessageServerService(Object messageServerService) {
        this.messageServerService = messageServerService;
        return this;
    }

    /**
     * Get the sncMode property: SNC activation flag (Boolean) to access the SAP server where the table is located.
     * Type: boolean (or Expression with resultType boolean).
     * 
     * @return the sncMode value.
     */
    @Generated
    public Object getSncMode() {
        return this.sncMode;
    }

    /**
     * Set the sncMode property: SNC activation flag (Boolean) to access the SAP server where the table is located.
     * Type: boolean (or Expression with resultType boolean).
     * 
     * @param sncMode the sncMode value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setSncMode(Object sncMode) {
        this.sncMode = sncMode;
        return this;
    }

    /**
     * Get the sncMyName property: Initiator's SNC name to access the SAP server where the table is located. Type:
     * string (or Expression with resultType string).
     * 
     * @return the sncMyName value.
     */
    @Generated
    public Object getSncMyName() {
        return this.sncMyName;
    }

    /**
     * Set the sncMyName property: Initiator's SNC name to access the SAP server where the table is located. Type:
     * string (or Expression with resultType string).
     * 
     * @param sncMyName the sncMyName value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setSncMyName(Object sncMyName) {
        this.sncMyName = sncMyName;
        return this;
    }

    /**
     * Get the sncPartnerName property: Communication partner's SNC name to access the SAP server where the table is
     * located. Type: string (or Expression with resultType string).
     * 
     * @return the sncPartnerName value.
     */
    @Generated
    public Object getSncPartnerName() {
        return this.sncPartnerName;
    }

    /**
     * Set the sncPartnerName property: Communication partner's SNC name to access the SAP server where the table is
     * located. Type: string (or Expression with resultType string).
     * 
     * @param sncPartnerName the sncPartnerName value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setSncPartnerName(Object sncPartnerName) {
        this.sncPartnerName = sncPartnerName;
        return this;
    }

    /**
     * Get the sncLibraryPath property: External security product's library to access the SAP server where the table is
     * located. Type: string (or Expression with resultType string).
     * 
     * @return the sncLibraryPath value.
     */
    @Generated
    public Object getSncLibraryPath() {
        return this.sncLibraryPath;
    }

    /**
     * Set the sncLibraryPath property: External security product's library to access the SAP server where the table is
     * located. Type: string (or Expression with resultType string).
     * 
     * @param sncLibraryPath the sncLibraryPath value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setSncLibraryPath(Object sncLibraryPath) {
        this.sncLibraryPath = sncLibraryPath;
        return this;
    }

    /**
     * Get the sncQop property: SNC Quality of Protection. Allowed value include: 1, 2, 3, 8, 9. Type: string (or
     * Expression with resultType string).
     * 
     * @return the sncQop value.
     */
    @Generated
    public Object getSncQop() {
        return this.sncQop;
    }

    /**
     * Set the sncQop property: SNC Quality of Protection. Allowed value include: 1, 2, 3, 8, 9. Type: string (or
     * Expression with resultType string).
     * 
     * @param sncQop the sncQop value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setSncQop(Object sncQop) {
        this.sncQop = sncQop;
        return this;
    }

    /**
     * Get the logonGroup property: The Logon Group for the SAP System. Type: string (or Expression with resultType
     * string).
     * 
     * @return the logonGroup value.
     */
    @Generated
    public Object getLogonGroup() {
        return this.logonGroup;
    }

    /**
     * Set the logonGroup property: The Logon Group for the SAP System. Type: string (or Expression with resultType
     * string).
     * 
     * @param logonGroup the logonGroup value to set.
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setLogonGroup(Object logonGroup) {
        this.logonGroup = logonGroup;
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
     * @return the SapTableLinkedService object itself.
     */
    @Generated
    public SapTableLinkedService setEncryptedCredential(Object encryptedCredential) {
        this.encryptedCredential = encryptedCredential;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public SapTableLinkedService setVersion(String version) {
        super.setVersion(version);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public SapTableLinkedService setConnectVia(IntegrationRuntimeReference connectVia) {
        super.setConnectVia(connectVia);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public SapTableLinkedService setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public SapTableLinkedService setParameters(Map<String, ParameterSpecification> parameters) {
        super.setParameters(parameters);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public SapTableLinkedService setAnnotations(List<Object> annotations) {
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
        if (server != null
            || systemNumber != null
            || clientId != null
            || language != null
            || systemId != null
            || userName != null
            || password != null
            || messageServer != null
            || messageServerService != null
            || sncMode != null
            || sncMyName != null
            || sncPartnerName != null
            || sncLibraryPath != null
            || sncQop != null
            || logonGroup != null
            || encryptedCredential != null) {
            jsonWriter.writeStartObject("typeProperties");
            if (this.server != null) {
                jsonWriter.writeUntypedField("server", this.server);
            }
            if (this.systemNumber != null) {
                jsonWriter.writeUntypedField("systemNumber", this.systemNumber);
            }
            if (this.clientId != null) {
                jsonWriter.writeUntypedField("clientId", this.clientId);
            }
            if (this.language != null) {
                jsonWriter.writeUntypedField("language", this.language);
            }
            if (this.systemId != null) {
                jsonWriter.writeUntypedField("systemId", this.systemId);
            }
            if (this.userName != null) {
                jsonWriter.writeUntypedField("userName", this.userName);
            }
            jsonWriter.writeJsonField("password", this.password);
            if (this.messageServer != null) {
                jsonWriter.writeUntypedField("messageServer", this.messageServer);
            }
            if (this.messageServerService != null) {
                jsonWriter.writeUntypedField("messageServerService", this.messageServerService);
            }
            if (this.sncMode != null) {
                jsonWriter.writeUntypedField("sncMode", this.sncMode);
            }
            if (this.sncMyName != null) {
                jsonWriter.writeUntypedField("sncMyName", this.sncMyName);
            }
            if (this.sncPartnerName != null) {
                jsonWriter.writeUntypedField("sncPartnerName", this.sncPartnerName);
            }
            if (this.sncLibraryPath != null) {
                jsonWriter.writeUntypedField("sncLibraryPath", this.sncLibraryPath);
            }
            if (this.sncQop != null) {
                jsonWriter.writeUntypedField("sncQop", this.sncQop);
            }
            if (this.logonGroup != null) {
                jsonWriter.writeUntypedField("logonGroup", this.logonGroup);
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
     * Reads an instance of SapTableLinkedService from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SapTableLinkedService if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SapTableLinkedService.
     */
    @Generated
    public static SapTableLinkedService fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SapTableLinkedService deserializedSapTableLinkedService = new SapTableLinkedService();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("version".equals(fieldName)) {
                    deserializedSapTableLinkedService.setVersion(reader.getString());
                } else if ("connectVia".equals(fieldName)) {
                    deserializedSapTableLinkedService.setConnectVia(IntegrationRuntimeReference.fromJson(reader));
                } else if ("description".equals(fieldName)) {
                    deserializedSapTableLinkedService.setDescription(reader.getString());
                } else if ("parameters".equals(fieldName)) {
                    Map<String, ParameterSpecification> parameters
                        = reader.readMap(reader1 -> ParameterSpecification.fromJson(reader1));
                    deserializedSapTableLinkedService.setParameters(parameters);
                } else if ("annotations".equals(fieldName)) {
                    List<Object> annotations = reader.readArray(reader1 -> reader1.readUntyped());
                    deserializedSapTableLinkedService.setAnnotations(annotations);
                } else if ("type".equals(fieldName)) {
                    deserializedSapTableLinkedService.type = reader.getString();
                } else if ("typeProperties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("server".equals(fieldName)) {
                            deserializedSapTableLinkedService.server = reader.readUntyped();
                        } else if ("systemNumber".equals(fieldName)) {
                            deserializedSapTableLinkedService.systemNumber = reader.readUntyped();
                        } else if ("clientId".equals(fieldName)) {
                            deserializedSapTableLinkedService.clientId = reader.readUntyped();
                        } else if ("language".equals(fieldName)) {
                            deserializedSapTableLinkedService.language = reader.readUntyped();
                        } else if ("systemId".equals(fieldName)) {
                            deserializedSapTableLinkedService.systemId = reader.readUntyped();
                        } else if ("userName".equals(fieldName)) {
                            deserializedSapTableLinkedService.userName = reader.readUntyped();
                        } else if ("password".equals(fieldName)) {
                            deserializedSapTableLinkedService.password = SecretBase.fromJson(reader);
                        } else if ("messageServer".equals(fieldName)) {
                            deserializedSapTableLinkedService.messageServer = reader.readUntyped();
                        } else if ("messageServerService".equals(fieldName)) {
                            deserializedSapTableLinkedService.messageServerService = reader.readUntyped();
                        } else if ("sncMode".equals(fieldName)) {
                            deserializedSapTableLinkedService.sncMode = reader.readUntyped();
                        } else if ("sncMyName".equals(fieldName)) {
                            deserializedSapTableLinkedService.sncMyName = reader.readUntyped();
                        } else if ("sncPartnerName".equals(fieldName)) {
                            deserializedSapTableLinkedService.sncPartnerName = reader.readUntyped();
                        } else if ("sncLibraryPath".equals(fieldName)) {
                            deserializedSapTableLinkedService.sncLibraryPath = reader.readUntyped();
                        } else if ("sncQop".equals(fieldName)) {
                            deserializedSapTableLinkedService.sncQop = reader.readUntyped();
                        } else if ("logonGroup".equals(fieldName)) {
                            deserializedSapTableLinkedService.logonGroup = reader.readUntyped();
                        } else if ("encryptedCredential".equals(fieldName)) {
                            deserializedSapTableLinkedService.encryptedCredential = reader.readUntyped();
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
            deserializedSapTableLinkedService.setAdditionalProperties(additionalProperties);

            return deserializedSapTableLinkedService;
        });
    }
}
