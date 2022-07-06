// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.confidentialledger.ConfidentialLedgerClient;
import com.azure.security.confidentialledger.ConfidentialLedgerClientBuilder;
import com.azure.security.confidentialledger.ConfidentialLedgerIdentityClient;
import com.azure.security.confidentialledger.ConfidentialLedgerIdentityClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.core.publisher.Mono;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

public class ConfidentialLedgerClientBase {
    public ConfidentialLedgerClientBase() {
        // for example, https://identity.confidential-ledger.core.azure.com
        String identityServiceUri = null;

        // for example, https://my-ledger.confidential-ledger.azure.com
        String ledgerUri = null;

        try {
            ConfidentialLedgerIdentityClientBuilder confidentialLedgerIdentityClientbuilder = new ConfidentialLedgerIdentityClientBuilder()
                .identityServiceUri(identityServiceUri)
                .httpClient(HttpClient.createDefault())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

            ConfidentialLedgerIdentityClient confidentialLedgerIdentityClient = confidentialLedgerIdentityClientbuilder
                    .buildClient();
            String ledgerId = ledgerUri
                    .replaceAll("\\w+://", "")
                    .replaceAll("\\..*", "");

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

            ConfidentialLedgerClientBuilder confidentialLedgerClientbuilder = new ConfidentialLedgerClientBuilder()
                    .ledgerUri(ledgerUri)
                    .httpClient(httpClient)
                    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

            ConfidentialLedgerClient confidentialLedgerClient = confidentialLedgerClientbuilder.buildClient();
        } catch (Exception ex) {
            System.out.println("Error thrown from ConfidentialLedgerClientBase:" + ex);
        }
    }
}
