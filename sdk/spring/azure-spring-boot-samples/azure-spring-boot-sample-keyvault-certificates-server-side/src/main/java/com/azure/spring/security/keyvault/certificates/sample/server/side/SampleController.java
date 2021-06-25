// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.security.keyvault.certificates.sample.server.side;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/")
    public String helloWorld() {
        /*
         * 1. Add the dependency of `azure-security-keyvault-jca` in the pom file.
         * <dependency>
         *  <groupId>com.azure</groupId>
         *  <artifactId>azure-security-keyvault-jca</artifactId>
         *  <version>1.0.0-beta.8</version>
         * </dependency>
         *
         * 2. To enable KeyVaultJcaProvider, add Command line arguments:
         *    -Dsecurity.overridePropertiesFile=true
         *    Note:
         *    2.1. The default java.security file can be found in ${JAVA_HOME}/conf/security/java.security
         *    2.2. Copy file java.security, add one line in new java.security file: security.provider.14=com.azure
         * .security.keyvault.jca.KeyVaultJcaProvider
         *    2.3. In "security.provider.14=com.azure.security.keyvault.jca.KeyVaultJcaProvider", I use "14" because
         * there is already 13 items in original file.
         *         "security.provider.1=com.azure.security.keyvault.jca.KeyVaultJcaProvider" will make
         * JreCertificates throw exception, so please don't use order "1".
         *
         * 3. To use AzureKeyVault keystore as trust store, add command line argument:
         *     -Djavax.net.ssl.trustStoreType=AzureKeyVault
         *     -Dserver.ssl.key-store=The absolute path of your java.security file.
         * 4. To configure four parameters of Azure Key Vault in portal.
         *     -Dazure.keyvault.uri=${KEY_VAULT_URI}
         *     -Dazure.keyvault.tenant-id=${SERVICE_PRINCIPAL_TETANT}
         *     -Dazure.keyvault.client-id=${SERVICE_PRINCIPAL_ID}
         *     -Dazure.keyvault.client-secret=${SERVICE_PRINCIPAL_SECRET}
         *
         */
        return "Hello World";
    }
}
