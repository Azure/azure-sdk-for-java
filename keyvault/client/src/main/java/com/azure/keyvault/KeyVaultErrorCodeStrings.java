// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


class KeyVaultErrorCodeStrings {

    static final String CREDENTIALS_REQUIRED_PROPERTY = "credentials_required";
    static final String ERROR_STRINGS_FILE_NAME = "kvErrorStrings.properties";
    static final String VAULT_ENDPOINT_REQUIRED_PROPERTY = "vault_endpoint_required";
    private static Properties errorStrings;

    /**
     *  Gets the error String to indicate Azure key vault credentials requirement.
     *
     * @return The {@link String value} containing the error message for Azure key vault credentials requirement.
     */
    static String getCredentialsRequired() {
        loadProperties();
        return errorStrings.getProperty(CREDENTIALS_REQUIRED_PROPERTY);
    }

    /**
     *  Gets the error String to indicate Azure key vault end point url requirement.
     *
     * @return The {@link String value} containing the error message for Azure key vault end point url requirement.
     */
    static String getVaultEndPointRequired() {
        if(errorStrings == null){
            loadProperties();
        }
        return errorStrings.getProperty(VAULT_ENDPOINT_REQUIRED_PROPERTY);
    }

    private static void loadProperties(){
        if(errorStrings != null) {
            return;
        }
        try (InputStream fileInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ERROR_STRINGS_FILE_NAME)) {
            errorStrings = new Properties();
            errorStrings.load(fileInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}



