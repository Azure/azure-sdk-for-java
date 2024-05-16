// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


/**
 * Because of maven-shade-plugin, we have some special requirement of module-info:
 * 1. When compile current module by java 9.
 *    module-info.java must have content like "requires org.apache.httpcomponents.httpclient;".
 * 2. When used by other java 9 modules,
 *    module-info.java must NOT have content like "requires org.apache.httpcomponents.httpclient;".
 *
 * To achieve this, we need these steps:
 * 1. Avoid compile error in other module by deleting contents like "requires org.apache.httpcomponents.httpclient;".
 * 2. Avoid compile error in current module by moving module-info.java out of src/main/java/.
 * 3. Package module-info.class into xxx.jar by configuring moditect-maven-plugin.
 * 4. Package module-info.java into xxx-source.jar by putting module-info.java in src/main/resources/.
 * 5. Exclude module-info.java into xxx.jar by configuring maven-jar-plugin.
 */
module com.azure.security.keyvault.jca {
    requires java.logging;

    exports com.azure.security.keyvault.jca;
}
