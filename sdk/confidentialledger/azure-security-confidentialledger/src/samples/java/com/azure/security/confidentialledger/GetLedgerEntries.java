// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.experimental.http.DynamicResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.netty.handler.ssl.SslContextBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sample for getting ledger entries using the ConfidentialLedgerBaseClient.
 */
public class GetLedgerEntries {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) throws Exception {
        ConfidentialLedgerIdentityServiceBaseClient identityServiceClient = new ConfidentialLedgerClientBuilder()
            .identityServiceUri(new URL(System.getenv("CONFIDENTIALLEDGER_IDENTITY_URL")))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildConfidentialLedgerIdentityServiceBaseClient();

        System.out.println("Getting certificate from Confidential Ledger Identity service...");

        String ledgerId = System.getenv("CONFIDENTIALLEDGER_URL")
            .replaceAll("\\w+://", "")
            .replaceAll("\\..*", "");
        DynamicResponse response = identityServiceClient.getLedgerIdentity(ledgerId).send();
        JsonReader jsonReader = Json.createReader(new StringReader(response.getBody().toString()));
        JsonObject result = jsonReader.readObject();
        String tlsCert = result.getString("ledgerTlsCertificate");
        reactor.netty.http.client.HttpClient reactorClient = reactor.netty.http.client.HttpClient.create()
            .secure(sslContextSpec -> sslContextSpec.sslContext(SslContextBuilder.forClient()
                .trustManager(new ByteArrayInputStream(tlsCert.getBytes(StandardCharsets.UTF_8)))));
        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactorClient).wiretap(true).build();

        System.out.println("Creating Confidential Ledger client with the certificate...");

        ConfidentialLedgerBaseClient confidentialLedgerClient = new ConfidentialLedgerClientBuilder()
            .ledgerUri(new URL(System.getenv("CONFIDENTIALLEDGER_URL")))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(httpClient)
            .buildConfidentialLedgerBaseClient();

        System.out.println("Getting ledger entries:");

        response = confidentialLedgerClient.getLedgerEntries().send();

        jsonReader = Json.createReader(new StringReader(response.getBody().toString()));
        result = jsonReader.readObject();
        if (result.containsKey("entries")) {
            JsonArray entries = result.getJsonArray("entries");
            entries.forEach(e -> {
                JsonObject entry = e.asJsonObject();
                System.out.println("Sub leger " + entry.getString("subLedgerId") + ": " + entry.getString("contents"));
            });
        }
        while (result.containsKey("@nextLink")) {
            response = confidentialLedgerClient.getLedgerEntriesNext(result.getString("@nextLink").replaceAll("^/", "")).send();
            jsonReader = Json.createReader(new StringReader(response.getBody().toString()));
            result = jsonReader.readObject();
            if (result.containsKey("entries")) {
                JsonArray entries = result.getJsonArray("entries");
                entries.forEach(e -> {
                    JsonObject entry = e.asJsonObject();
                    System.out.println("Sub leger " + entry.getString("subLedgerId") + ": "
                        + entry.getString("contents"));
                });
            }
        }
    }
}
