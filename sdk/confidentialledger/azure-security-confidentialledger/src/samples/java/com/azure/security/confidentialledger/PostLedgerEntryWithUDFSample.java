package com.azure.security.confidentialledger;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class PostLedgerEntryWithUDFSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient client = new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .ledgerEndpoint(System.getenv("LEDGERENDPOINT"))
            .buildClient();

        BinaryData entry = BinaryData.fromString(
            "{\"contents\":\"Entry with UDF.\",\"postHooks\":[{\"functionId\":\"myFunctionId\"}]}"
        );
        client.createLedgerEntry(entry);
    }
}
