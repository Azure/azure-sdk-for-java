// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetLedgerIdentity {
    public static void main(String[] args) {
        ConfidentialLedgerIdentityClient confidentialLedgerIdentityClient =
                new ConfidentialLedgerIdentityClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .identityServiceUri("identity.accledger.azure.com")
                        .buildClient();
        // BEGIN:com.azure.security.confidentialledger.generated.getledgeridentity.getledgeridentity
        RequestOptions requestOptions = new RequestOptions();
        String ledgerId = "your_ledger_name";
        Response<BinaryData> response =
                confidentialLedgerIdentityClient.getLedgerIdentityWithResponse(ledgerId, requestOptions);
        // END:com.azure.security.confidentialledger.generated.getledgeridentity.getledgeridentity
    }
}
