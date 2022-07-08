// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import org.junit.jupiter.api.Assertions;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DeleteUser {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerUri("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        RequestOptions requestOptions = new RequestOptions();
        
        // you can retrieve your object id by going to Azure Active Directory and finding your profile
        String aadObjectId = "<YOUR AAD ID>";
        Response<Void> response = confidentialLedgerClient.deleteUserWithResponse(aadObjectId, requestOptions);
    }
}
