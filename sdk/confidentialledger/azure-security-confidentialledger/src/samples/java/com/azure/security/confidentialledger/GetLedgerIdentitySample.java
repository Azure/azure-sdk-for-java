// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.confidentialledger.certificate.ConfidentialLedgerCertificateClient;
import com.azure.security.confidentialledger.certificate.ConfidentialLedgerCertificateClientBuilder;

public class GetLedgerIdentitySample {
    public static void main(String[] args) {
        ConfidentialLedgerCertificateClient confidentialLedgerCertificateClient =
                new ConfidentialLedgerCertificateClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .certificateEndpoint("identity.accledger.azure.com")
                        .buildClient();
        RequestOptions requestOptions = new RequestOptions();
        String ledgerId = "your_ledger_name";
        Response<BinaryData> response =
                confidentialLedgerCertificateClient.getLedgerIdentityWithResponse(ledgerId, requestOptions);
    }
}
