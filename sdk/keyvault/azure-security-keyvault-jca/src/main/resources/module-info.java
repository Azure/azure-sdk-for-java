// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


/**
 * Dependencies are relocated by the maven-shade-plugin, so this descriptor is kept outside src/main/java and added
 * to the shaded JAR by the moditect-maven-plugin. Keeping it in src/main/resources also includes the descriptor in
 * the sources JAR, while the maven-jar-plugin excludes the uncompiled source from the binary JAR.
 */
module com.azure.security.keyvault.jca {
    requires java.logging;
    requires java.xml;

    exports com.azure.security.keyvault.jca;
    exports com.azure.security.keyvault.jca.implementation.signature to java.base;

    provides java.security.Provider with com.azure.security.keyvault.jca.KeyVaultJcaProvider;
    uses com.azure.security.keyvault.jca.implementation.shaded.com.azure.json.JsonProvider;
}
