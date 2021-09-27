// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

/**
 * Throw runtime exception for configuration.
 *
 * @deprecated All Azure AD B2C features supported by Spring security, please refer to https://github.com/zhichengliu12581/azure-spring-boot-samples/blob/add-samples-for-aad-b2c-with-only-spring-security/aad/aad-b2c-with-spring-security/README.adoc
 */
@Deprecated
public class AADB2CConfigurationException extends RuntimeException {

    public AADB2CConfigurationException(String message) {
        super(message);
    }

    public AADB2CConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
