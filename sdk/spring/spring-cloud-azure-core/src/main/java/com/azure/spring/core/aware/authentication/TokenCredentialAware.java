// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware.authentication;


/**
 * Interface to be implemented by classes that wish to be aware of the token credential.
 */
public interface TokenCredentialAware {

    TokenCredential getCredential();

    /**
     * Interface to be implemented by classes that wish to describe the token credential related options.
     */
    interface TokenCredential {

        String getClientId();

        String getClientSecret();

        String getClientCertificatePath();

        String getClientCertificatePassword();

        String getUsername();

        String getPassword();

        String getManagedIdentityClientId();

    }

}
