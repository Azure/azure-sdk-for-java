// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation;

public class MockKeyVaultClient extends KeyVaultClient {
    public MockKeyVaultClient() {
        super("https://accountname.vault.azure.net", "tenant-id", "client-id", "client-secret");
    }
}
