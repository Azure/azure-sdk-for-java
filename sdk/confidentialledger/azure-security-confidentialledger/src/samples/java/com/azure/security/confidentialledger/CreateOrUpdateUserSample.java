// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class CreateOrUpdateUserSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerEndpoint("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        BinaryData userDetails = BinaryData.fromString("{\"assignedRole\":\"Reader\"}");
        RequestOptions requestOptions = new RequestOptions();
        String aadObjectId = "AAD OBJECT ID";
        Response<BinaryData> response =
                confidentialLedgerClient.createOrUpdateUserWithResponse(aadObjectId, userDetails, requestOptions);
    }
}
