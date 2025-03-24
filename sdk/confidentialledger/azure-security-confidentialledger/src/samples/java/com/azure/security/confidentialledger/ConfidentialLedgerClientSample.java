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
import com.azure.security.confidentialledger.certificate.ConfidentialLedgerCertificateClient;
import com.azure.security.confidentialledger.certificate.ConfidentialLedgerCertificateClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class ConfidentialLedgerClientSample {
    public static void main(String[] args) {
        try {
            // BEGIN:readme-sample-createClient
            ConfidentialLedgerCertificateClientBuilder confidentialLedgerCertificateClientbuilder = new ConfidentialLedgerCertificateClientBuilder()
                .certificateEndpoint("https://identity.confidential-ledger.core.azure.com")
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpClient(HttpClient.createDefault());
        
            ConfidentialLedgerCertificateClient confidentialLedgerCertificateClient = confidentialLedgerCertificateClientbuilder.buildClient();

            String ledgerId = "java-tests";
            Response<BinaryData> ledgerCertificateWithResponse = confidentialLedgerCertificateClient
                .getLedgerIdentityWithResponse(ledgerId, null);
            BinaryData certificateResponse = ledgerCertificateWithResponse.getValue();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(certificateResponse.toBytes());
            String ledgerTlsCertificate = jsonNode.get("ledgerTlsCertificate").asText();


            SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(new ByteArrayInputStream(ledgerTlsCertificate.getBytes(StandardCharsets.UTF_8))).build();
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
