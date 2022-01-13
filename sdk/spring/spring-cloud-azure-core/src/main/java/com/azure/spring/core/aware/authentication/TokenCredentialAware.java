// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware.authentication;


/**
 * Interface to be implemented by classes that wish to be aware of the token credential.
 */
public interface TokenCredentialAware {

    /**
     * Get the token credential
     * @return the token credential
     */
    TokenCredential getCredential();

    /**
     * Interface to be implemented by classes that wish to describe the token credential related options.
     */
    interface TokenCredential {

        /**
         * Get the client id
         * @return the client id
         */
        String getClientId();

        /**
         * Get the client secret
         * @return the client secret
         */
        String getClientSecret();

        /**
         * Get the client certificate path
         * @return the client certificate path
         */
        String getClientCertificatePath();

        /**
         * Get the client certificate password
         * @return the client certificate password
         */
        String getClientCertificatePassword();

        /**
         * Get the username
         * @return the username
         */
        String getUsername();

        /**
         * Get the password
         * @return the password
         */
        String getPassword();

        /**
         * Get the managed identity client id
         * @return the managed identity client id
         */
        String getManagedIdentityClientId();

    }

}
