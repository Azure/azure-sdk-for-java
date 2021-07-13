// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 *  This module info is only for development purpose. Maven shade plugin will ignore this file. The one actually deployed is src/main/module-info.java
 */
module azure.security.keyvault.jca {
    requires java.logging;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires com.fasterxml.jackson.databind;

    exports com.azure.security.keyvault.jca;
    exports com.azure.security.keyvault.jca.model;
}
