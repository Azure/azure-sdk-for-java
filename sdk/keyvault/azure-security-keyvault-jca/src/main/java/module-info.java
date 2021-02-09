// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.jca {
    requires transitive java.logging;
    requires transitive com.fasterxml.jackson.databind;

    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.httpcomponents.client5.httpclient5;

    exports com.azure.security.keyvault.jca;
    exports com.azure.security.keyvault.jca.model;
}
