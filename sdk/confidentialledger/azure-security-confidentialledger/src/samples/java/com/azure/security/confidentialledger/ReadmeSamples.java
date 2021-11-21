// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.experimental.http.DynamicResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.netty.handler.ssl.SslContextBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {
    /**
     * Sample for creating low level client.
     */
    public void createClient() throws Exception {
        ConfidentialLedgerIdentityServiceBaseClient identityServiceClient = new ConfidentialLedgerClientBuilder()
            .ledgerUri(new URL("<confidential-ledger-url>"))
            .identityServiceUri(new URL("<confidential-ledger-identity-service-url>"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildConfidentialLedgerIdentityServiceBaseClient();

        String ledgerId = "<confidential-ledger-url>"
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
            .ledgerUri(new URL("<confidential-ledger-url"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(httpClient)
            .buildConfidentialLedgerBaseClient();
    }
}
