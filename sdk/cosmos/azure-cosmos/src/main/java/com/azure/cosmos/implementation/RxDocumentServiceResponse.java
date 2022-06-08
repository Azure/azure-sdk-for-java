// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.Address;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * This is core Transport/Connection agnostic response for the Azure Cosmos DB database service.
 */
public class RxDocumentServiceResponse {
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final int statusCode;
    private final Map<String, String> headersMap;
    private final StoreResponse storeResponse;
    private RequestTimeline gatewayHttpRequestTimeline;
    private CosmosDiagnostics cosmosDiagnostics;

    public RxDocumentServiceResponse(DiagnosticsClientContext diagnosticsClientContext, StoreResponse response) {
        this.headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.headersMap.putAll(response.getResponseHeaders());

        // Gets status code.
        this.statusCode = response.getStatus();

        this.storeResponse = response;
        this.diagnosticsClientContext = diagnosticsClientContext;
    }

    public RxDocumentServiceResponse(DiagnosticsClientContext diagnosticsClientContext, StoreResponse response,
                                     RequestTimeline gatewayHttpRequestTimeline) {
        this(diagnosticsClientContext, response);
        this.gatewayHttpRequestTimeline = gatewayHttpRequestTimeline;
    }

    private static <T> String getResourceKey(Class<T> c) {
        if (c.equals(Conflict.class)) {
            return InternalConstants.ResourceKeys.CONFLICTS;
        } else if (c.equals(Database.class)) {
            return InternalConstants.ResourceKeys.DATABASES;
        } else if (Document.class.isAssignableFrom(c)) {
            return InternalConstants.ResourceKeys.DOCUMENTS;
        } else if (c.equals(DocumentCollection.class)) {
            return InternalConstants.ResourceKeys.DOCUMENT_COLLECTIONS;
        } else if (c.equals(Offer.class)) {
            return InternalConstants.ResourceKeys.OFFERS;
        } else if (c.equals(Permission.class)) {
            return InternalConstants.ResourceKeys.PERMISSIONS;
        } else if (c.equals(Trigger.class)) {
            return InternalConstants.ResourceKeys.TRIGGERS;
        } else if (c.equals(StoredProcedure.class)) {
            return InternalConstants.ResourceKeys.STOREDPROCEDURES;
        } else if (c.equals(User.class)) {
            return InternalConstants.ResourceKeys.USERS;
        } else if (c.equals(UserDefinedFunction.class)) {
            return InternalConstants.ResourceKeys.USER_DEFINED_FUNCTIONS;
        } else if (c.equals(Address.class)) {
            return InternalConstants.ResourceKeys.ADDRESSES;
        } else if (c.equals(PartitionKeyRange.class)) {
            return InternalConstants.ResourceKeys.PARTITION_KEY_RANGES;
        } else if (c.equals(ClientEncryptionKey.class)) {
            return InternalConstants.ResourceKeys.CLIENT_ENCRYPTION_KEYS;
        }

        return InternalConstants.ResourceKeys.DOCUMENTS;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public Map<String, String> getResponseHeaders() {
        return this.headersMap;
    }

    public byte[] getResponseBodyAsByteArray() {
        return this.storeResponse.getResponseBody();
    }

    public String getResponseBodyAsString() {
        return Utils.utf8StringFromOrNull(this.getResponseBodyAsByteArray());
    }

    public RequestTimeline getGatewayHttpRequestTimeline() {
        return gatewayHttpRequestTimeline;
    }

    public <T extends Resource> T getResource(Class<T> c) {
        String responseBody = this.getResponseBodyAsString();
        if (StringUtils.isEmpty(responseBody))
            return null;

        T resource = null;
        try {
            resource =  c.getConstructor(String.class).newInstance(responseBody);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Failed to instantiate class object.", e);
        }
        if(PathsHelper.isPublicResource(resource)) {
            BridgeInternal.setAltLink(resource, PathsHelper.generatePathForNameBased(resource, this.getOwnerFullName(),resource.getId()));
        }

        return resource;
    }

    private ArrayNode extractQueryResponseNodes(String resourceKey) {
        byte[] responseBody = this.getResponseBodyAsByteArray();
        if (responseBody == null) {
            return null;
        }

        JsonNode jobject = fromJson(responseBody);
        ArrayNode jTokenArray = (ArrayNode) jobject.get(resourceKey);

        // Aggregate queries may return a nested array
        ArrayNode innerArray;
        while (jTokenArray != null && jTokenArray.size() == 1 && (innerArray = toArrayNode(jTokenArray.get(0))) != null) {
            jTokenArray = innerArray;
        }

        return jTokenArray;
    }

    @SuppressWarnings("unchecked")
    // Given cls (where cls == Class<T>), objectNode is first decoded to cls and then casted to T.
    public <T> List<T> getQueryResponse(
        Function<JsonNode, T> factoryMethod,
        Class<T> c) {

        String resourceKey = RxDocumentServiceResponse.getResourceKey(c);
        ArrayNode jTokenArray = this.extractQueryResponseNodes(resourceKey);
        if (jTokenArray == null) {
            return new ArrayList<T>();
        }

        List<T> queryResults = new ArrayList<T>();

        for (int i = 0; i < jTokenArray.size(); ++i) {
            JsonNode jToken = jTokenArray.get(i);
            // Aggregate on single partition collection may return the aggregated value only
            // In that case it needs to encapsulated in a special document

            ObjectNode resourceJson = jToken.isValueNode() || jToken.isArray()// to add nulls, arrays, objects
                ? (ObjectNode) fromJson(String.format("{\"%s\": %s}", Constants.Properties.VALUE, jToken))
                : (ObjectNode) jToken;

            T resource = factoryMethod == null ?
                (T) JsonSerializable.instantiateFromObjectNodeAndType(resourceJson, c):
                factoryMethod.apply(resourceJson);

            queryResults.add(resource);
        }

        return queryResults;
    }

    private ArrayNode toArrayNode(JsonNode n) {
        if (n.isArray()) {
            return (ArrayNode) n;
        } else {
            return null;
        }
    }

    private static JsonNode fromJson(String json){
        try {
            return Utils.getSimpleObjectMapper().readTree(json);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to parse JSON %s", json), e);
        }
    }

    private static JsonNode fromJson(byte[] json){
        try {
            return Utils.getSimpleObjectMapper().readTree(json);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to parse JSON %s", Arrays.toString(json)), e);
        }
    }

    private String getOwnerFullName() {
        if (this.headersMap != null) {
            return this.headersMap.get(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
        }
        return null;
    }

    public CosmosDiagnostics getCosmosDiagnostics() {
        return this.cosmosDiagnostics;
    }

    public void setCosmosDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        this.cosmosDiagnostics = cosmosDiagnostics;
    }

    public DiagnosticsClientContext getDiagnosticsClientContext() {
        return diagnosticsClientContext;
    }

    /**
     * Gets the request charge as request units (RU) consumed by the operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
        String value = this.getResponseHeaders().get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value);
    }
}
