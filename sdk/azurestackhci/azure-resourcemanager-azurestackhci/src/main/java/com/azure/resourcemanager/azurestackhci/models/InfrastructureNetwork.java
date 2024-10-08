// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The InfrastructureNetwork of a AzureStackHCI Cluster.
 */
@Fluent
public final class InfrastructureNetwork implements JsonSerializable<InfrastructureNetwork> {
    /*
     * Subnet mask that matches the provided IP address space.
     */
    private String subnetMask;

    /*
     * Default gateway that should be used for the provided IP address space.
     */
    private String gateway;

    /*
     * Range of IP addresses from which addresses are allocated for nodes within a subnet.
     */
    private List<IpPools> ipPools;

    /*
     * IPv4 address of the DNS servers in your environment.
     */
    private List<String> dnsServers;

    /*
     * Allows customers to use DHCP for Hosts and Cluster IPs. If not declared, the deployment will default to static
     * IPs. When true, GW and DNS servers are not required
     */
    private Boolean useDhcp;

    /**
     * Creates an instance of InfrastructureNetwork class.
     */
    public InfrastructureNetwork() {
    }

    /**
     * Get the subnetMask property: Subnet mask that matches the provided IP address space.
     * 
     * @return the subnetMask value.
     */
    public String subnetMask() {
        return this.subnetMask;
    }

    /**
     * Set the subnetMask property: Subnet mask that matches the provided IP address space.
     * 
     * @param subnetMask the subnetMask value to set.
     * @return the InfrastructureNetwork object itself.
     */
    public InfrastructureNetwork withSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
        return this;
    }

    /**
     * Get the gateway property: Default gateway that should be used for the provided IP address space.
     * 
     * @return the gateway value.
     */
    public String gateway() {
        return this.gateway;
    }

    /**
     * Set the gateway property: Default gateway that should be used for the provided IP address space.
     * 
     * @param gateway the gateway value to set.
     * @return the InfrastructureNetwork object itself.
     */
    public InfrastructureNetwork withGateway(String gateway) {
        this.gateway = gateway;
        return this;
    }

    /**
     * Get the ipPools property: Range of IP addresses from which addresses are allocated for nodes within a subnet.
     * 
     * @return the ipPools value.
     */
    public List<IpPools> ipPools() {
        return this.ipPools;
    }

    /**
     * Set the ipPools property: Range of IP addresses from which addresses are allocated for nodes within a subnet.
     * 
     * @param ipPools the ipPools value to set.
     * @return the InfrastructureNetwork object itself.
     */
    public InfrastructureNetwork withIpPools(List<IpPools> ipPools) {
        this.ipPools = ipPools;
        return this;
    }

    /**
     * Get the dnsServers property: IPv4 address of the DNS servers in your environment.
     * 
     * @return the dnsServers value.
     */
    public List<String> dnsServers() {
        return this.dnsServers;
    }

    /**
     * Set the dnsServers property: IPv4 address of the DNS servers in your environment.
     * 
     * @param dnsServers the dnsServers value to set.
     * @return the InfrastructureNetwork object itself.
     */
    public InfrastructureNetwork withDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
        return this;
    }

    /**
     * Get the useDhcp property: Allows customers to use DHCP for Hosts and Cluster IPs. If not declared, the deployment
     * will default to static IPs. When true, GW and DNS servers are not required.
     * 
     * @return the useDhcp value.
     */
    public Boolean useDhcp() {
        return this.useDhcp;
    }

    /**
     * Set the useDhcp property: Allows customers to use DHCP for Hosts and Cluster IPs. If not declared, the deployment
     * will default to static IPs. When true, GW and DNS servers are not required.
     * 
     * @param useDhcp the useDhcp value to set.
     * @return the InfrastructureNetwork object itself.
     */
    public InfrastructureNetwork withUseDhcp(Boolean useDhcp) {
        this.useDhcp = useDhcp;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (ipPools() != null) {
            ipPools().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("subnetMask", this.subnetMask);
        jsonWriter.writeStringField("gateway", this.gateway);
        jsonWriter.writeArrayField("ipPools", this.ipPools, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("dnsServers", this.dnsServers, (writer, element) -> writer.writeString(element));
        jsonWriter.writeBooleanField("useDhcp", this.useDhcp);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of InfrastructureNetwork from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of InfrastructureNetwork if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the InfrastructureNetwork.
     */
    public static InfrastructureNetwork fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            InfrastructureNetwork deserializedInfrastructureNetwork = new InfrastructureNetwork();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("subnetMask".equals(fieldName)) {
                    deserializedInfrastructureNetwork.subnetMask = reader.getString();
                } else if ("gateway".equals(fieldName)) {
                    deserializedInfrastructureNetwork.gateway = reader.getString();
                } else if ("ipPools".equals(fieldName)) {
                    List<IpPools> ipPools = reader.readArray(reader1 -> IpPools.fromJson(reader1));
                    deserializedInfrastructureNetwork.ipPools = ipPools;
                } else if ("dnsServers".equals(fieldName)) {
                    List<String> dnsServers = reader.readArray(reader1 -> reader1.getString());
                    deserializedInfrastructureNetwork.dnsServers = dnsServers;
                } else if ("useDhcp".equals(fieldName)) {
                    deserializedInfrastructureNetwork.useDhcp = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedInfrastructureNetwork;
        });
    }
}
