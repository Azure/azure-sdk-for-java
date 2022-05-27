// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider.authentication;

/**
 * Interface to be implemented by classes that wish to provide the sas token.
 */
public interface SasTokenProvider {

    /**
     * Get the SAS token
     * @return the SAS token
     */
    String getSasToken();

}
