// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider.authentication;


/**
 * Interface to be implemented by classes that wish to provide the token credential options.
 */
public interface TokenCredentialOptionsProvider {

    /**
     * Get the token credential
     * @return the token credential
     */
    TokenCredentialOptions getCredential();

    /**
     * Interface to be implemented by classes that wish to describe the token credential related options.
     */
    interface TokenCredentialOptions {

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
         * Whether to enable managed identity to authenticate with Azure.
         * @return Is managed identity enabled.
         */
        boolean isManagedIdentityEnabled();

    }

}
