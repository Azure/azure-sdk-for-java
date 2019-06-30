// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

<<<<<<< HEAD:keyvault/client/keys/src/main/java/com/azure/security/keyvault/keys/KeyVaultErrorCodeStrings.java
package com.azure.security.keyvault.keys;
=======
package com.azure.security.keyvault.secrets;
>>>>>>> 1978d61afc9b62348d25bb011dc0986fb110a867:keyvault/client/secrets/src/main/java/com/azure/security/keyvault/secrets/KeyVaultErrorCodeStrings.java

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class KeyVaultErrorCodeStrings {
    static final String ERROR_STRINGS_FILE_NAME = "kvErrorStrings.properties";
    private static Properties errorStrings;

    /**
     * The property name of Azure Key Vault Credentials required error string.
     */
    static final String CREDENTIAL_REQUIRED = "credential_required";

    /**
     * The property name of Azure Key Vault Endpoint required error string.
     */
    static final String VAULT_END_POINT_REQUIRED = "vault_endpoint_required";

    /**
     *  Gets the error String for the specified property.
     *
     * @param propertyName the property name for which error string is required.
     * @return The {@link String value} containing the error message.
     */
    static String getErrorString(String propertyName) {
        loadProperties();
        return errorStrings.getProperty(propertyName);
    }

    private static synchronized void loadProperties() {
        if (errorStrings == null) {
            try (InputStream fileInputStream = KeyVaultErrorCodeStrings.class.getClassLoader().getResource((ERROR_STRINGS_FILE_NAME)).openStream()) {
                errorStrings = new Properties();
                errorStrings.load(fileInputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}



