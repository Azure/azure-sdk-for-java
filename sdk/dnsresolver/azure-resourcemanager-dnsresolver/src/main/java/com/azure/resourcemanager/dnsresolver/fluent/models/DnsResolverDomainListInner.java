// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.dnsresolver.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Resource;
import com.azure.core.management.SystemData;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.dnsresolver.models.ProvisioningState;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Describes a DNS resolver domain list.
 */
@Fluent
public final class DnsResolverDomainListInner extends Resource {
    /*
     * ETag of the DNS resolver domain list.
     */
    private String etag;

    /*
     * Properties of the DNS resolver domain list.
     */
    private DnsResolverDomainListProperties innerProperties;

    /*
     * Azure Resource Manager metadata containing createdBy and modifiedBy information.
     */
    private SystemData systemData;

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
     * Creates an instance of DnsResolverDomainListInner class.
     */
    public DnsResolverDomainListInner() {
    }

    /**
     * Get the etag property: ETag of the DNS resolver domain list.
     * 
     * @return the etag value.
     */
    public String etag() {
        return this.etag;
    }

    /**
     * Get the innerProperties property: Properties of the DNS resolver domain list.
     * 
     * @return the innerProperties value.
     */
    private DnsResolverDomainListProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the systemData property: Azure Resource Manager metadata containing createdBy and modifiedBy information.
     * 
     * @return the systemData value.
     */
    public SystemData systemData() {
        return this.systemData;
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
     * {@inheritDoc}
     */
    @Override
    public DnsResolverDomainListInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DnsResolverDomainListInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Get the domains property: The domains in the domain list. Will be null if user is using large domain list.
     * 
     * @return the domains value.
     */
    public List<String> domains() {
        return this.innerProperties() == null ? null : this.innerProperties().domains();
    }

    /**
     * Set the domains property: The domains in the domain list. Will be null if user is using large domain list.
     * 
     * @param domains the domains value to set.
     * @return the DnsResolverDomainListInner object itself.
     */
    public DnsResolverDomainListInner withDomains(List<String> domains) {
        if (this.innerProperties() == null) {
            this.innerProperties = new DnsResolverDomainListProperties();
        }
        this.innerProperties().withDomains(domains);
        return this;
    }

    /**
     * Get the domainsUrl property: The URL for bulk upload or download for domain lists containing larger set of
     * domains. This will be populated if domains is empty or null.
     * 
     * @return the domainsUrl value.
     */
    public String domainsUrl() {
        return this.innerProperties() == null ? null : this.innerProperties().domainsUrl();
    }

    /**
     * Get the provisioningState property: The current provisioning state of the DNS resolver domain list. This is a
     * read-only property and any attempt to set this value will be ignored.
     * 
     * @return the provisioningState value.
     */
    public ProvisioningState provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the resourceGuid property: The resourceGuid property of the DNS resolver domain list resource.
     * 
     * @return the resourceGuid value.
     */
    public String resourceGuid() {
        return this.innerProperties() == null ? null : this.innerProperties().resourceGuid();
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
        jsonWriter.writeStringField("location", location());
        jsonWriter.writeMapField("tags", tags(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("properties", this.innerProperties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DnsResolverDomainListInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DnsResolverDomainListInner if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DnsResolverDomainListInner.
     */
    public static DnsResolverDomainListInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DnsResolverDomainListInner deserializedDnsResolverDomainListInner = new DnsResolverDomainListInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedDnsResolverDomainListInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedDnsResolverDomainListInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedDnsResolverDomainListInner.type = reader.getString();
                } else if ("location".equals(fieldName)) {
                    deserializedDnsResolverDomainListInner.withLocation(reader.getString());
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedDnsResolverDomainListInner.withTags(tags);
                } else if ("etag".equals(fieldName)) {
                    deserializedDnsResolverDomainListInner.etag = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    deserializedDnsResolverDomainListInner.innerProperties
                        = DnsResolverDomainListProperties.fromJson(reader);
                } else if ("systemData".equals(fieldName)) {
                    deserializedDnsResolverDomainListInner.systemData = SystemData.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDnsResolverDomainListInner;
        });
    }
}
