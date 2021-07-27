// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


/**
 * When compile current module by java 9.
 * module-info.java must have content like "requires org.apache.httpcomponents.httpclient;",
 * otherwise it will have compile error.
 *
 * When used by other java 9 modules,
 * module-info.java must NOT have content like "requires org.apache.httpcomponents.httpclient;".
 *
 * To achieve this, we delete src/main/java/module-info.java,
 * and add src/main/resources/module-info.java into jar by moditect-maven-plugin
 */
module com.azure.security.keyvault.jca {
    requires java.logging;

    exports com.azure.security.keyvault.jca;
    exports com.azure.security.keyvault.jca.implementation.model;
}
