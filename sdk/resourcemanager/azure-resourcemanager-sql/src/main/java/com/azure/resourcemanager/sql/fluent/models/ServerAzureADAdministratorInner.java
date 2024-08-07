// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.ProxyResource;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.sql.models.AdministratorType;
import java.io.IOException;
import java.util.UUID;

/**
 * Azure Active Directory administrator.
 */
@Fluent
public final class ServerAzureADAdministratorInner extends ProxyResource {
    /*
     * Resource properties.
     */
    private AdministratorProperties innerProperties;

    /*
     * The type of the resource.
     */
    private String type;

    /*
     * The name of the resource.
     */
    private String name;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /**
     * Creates an instance of ServerAzureADAdministratorInner class.
     */
    public ServerAzureADAdministratorInner() {
    }

    /**
     * Get the innerProperties property: Resource properties.
     * 
     * @return the innerProperties value.
     */
    private AdministratorProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the name property: The name of the resource.
     * 
     * @return the name value.
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * Get the administratorType property: Type of the sever administrator.
     * 
     * @return the administratorType value.
     */
    public AdministratorType administratorType() {
        return this.innerProperties() == null ? null : this.innerProperties().administratorType();
    }

    /**
     * Set the administratorType property: Type of the sever administrator.
     * 
     * @param administratorType the administratorType value to set.
     * @return the ServerAzureADAdministratorInner object itself.
     */
    public ServerAzureADAdministratorInner withAdministratorType(AdministratorType administratorType) {
        if (this.innerProperties() == null) {
            this.innerProperties = new AdministratorProperties();
        }
        this.innerProperties().withAdministratorType(administratorType);
        return this;
    }

    /**
     * Get the login property: Login name of the server administrator.
     * 
     * @return the login value.
     */
    public String login() {
        return this.innerProperties() == null ? null : this.innerProperties().login();
    }

    /**
     * Set the login property: Login name of the server administrator.
     * 
     * @param login the login value to set.
     * @return the ServerAzureADAdministratorInner object itself.
     */
    public ServerAzureADAdministratorInner withLogin(String login) {
        if (this.innerProperties() == null) {
            this.innerProperties = new AdministratorProperties();
        }
        this.innerProperties().withLogin(login);
        return this;
    }

    /**
     * Get the sid property: SID (object ID) of the server administrator.
     * 
     * @return the sid value.
     */
    public UUID sid() {
        return this.innerProperties() == null ? null : this.innerProperties().sid();
    }

    /**
     * Set the sid property: SID (object ID) of the server administrator.
     * 
     * @param sid the sid value to set.
     * @return the ServerAzureADAdministratorInner object itself.
     */
    public ServerAzureADAdministratorInner withSid(UUID sid) {
        if (this.innerProperties() == null) {
            this.innerProperties = new AdministratorProperties();
        }
        this.innerProperties().withSid(sid);
        return this;
    }

    /**
     * Get the tenantId property: Tenant ID of the administrator.
     * 
     * @return the tenantId value.
     */
    public UUID tenantId() {
        return this.innerProperties() == null ? null : this.innerProperties().tenantId();
    }

    /**
     * Set the tenantId property: Tenant ID of the administrator.
     * 
     * @param tenantId the tenantId value to set.
     * @return the ServerAzureADAdministratorInner object itself.
     */
    public ServerAzureADAdministratorInner withTenantId(UUID tenantId) {
        if (this.innerProperties() == null) {
            this.innerProperties = new AdministratorProperties();
        }
        this.innerProperties().withTenantId(tenantId);
        return this;
    }

    /**
     * Get the azureADOnlyAuthentication property: Azure Active Directory only Authentication enabled.
     * 
     * @return the azureADOnlyAuthentication value.
     */
    public Boolean azureADOnlyAuthentication() {
        return this.innerProperties() == null ? null : this.innerProperties().azureADOnlyAuthentication();
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() != null) {
            innerProperties().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("properties", this.innerProperties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ServerAzureADAdministratorInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ServerAzureADAdministratorInner if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ServerAzureADAdministratorInner.
     */
    public static ServerAzureADAdministratorInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ServerAzureADAdministratorInner deserializedServerAzureADAdministratorInner
                = new ServerAzureADAdministratorInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedServerAzureADAdministratorInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedServerAzureADAdministratorInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedServerAzureADAdministratorInner.type = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    deserializedServerAzureADAdministratorInner.innerProperties
                        = AdministratorProperties.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedServerAzureADAdministratorInner;
        });
    }
}
