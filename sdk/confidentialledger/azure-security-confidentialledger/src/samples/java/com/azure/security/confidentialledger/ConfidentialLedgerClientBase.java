// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class ConfidentialLedgerClientBase {
    public ConfidentialLedgerClientBase() {
        try {
            // BEGIN:readme-sample-createClient
            ConfidentialLedgerIdentityClientBuilder confidentialLedgerIdentityClientbuilder = new ConfidentialLedgerIdentityClientBuilder()
                .identityServiceUri("https://identity.confidential-ledger.core.azure.com")
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpClient(HttpClient.createDefault());
        
            ConfidentialLedgerIdentityClient confidentialLedgerIdentityClient = confidentialLedgerIdentityClientbuilder.buildClient();

            String ledgerId = "java-tests";
            // this is a built in test of getLedgerIdentity
            Response<BinaryData> ledgerIdentityWithResponse = confidentialLedgerIdentityClient
                .getLedgerIdentityWithResponse(ledgerId, null);
            BinaryData identityResponse = ledgerIdentityWithResponse.getValue();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(identityResponse.toBytes());
            String ledgerTslCertificate = jsonNode.get("ledgerTlsCertificate").asText();


            SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(new ByteArrayInputStream(ledgerTslCertificate.getBytes(StandardCharsets.UTF_8))).build();
            reactor.netty.http.client.HttpClient reactorClient = reactor.netty.http.client.HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactorClient).wiretap(true).build();

            ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .httpClient(httpClient)
                        .ledgerEndpoint("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
            // END:readme-sample-createClient
        } catch (Exception ex) {
            System.out.println("Caught exception" + ex);
        }
    }
}
