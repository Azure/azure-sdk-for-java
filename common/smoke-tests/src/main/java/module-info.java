// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.smoketest {
    requires transitive com.azure.messaging.eventhubs;
    requires transitive com.azure.security.keyvault.secrets;
    requires transitive com.azure.storage.blob;
    requires transitive com.azure.identity;
}
