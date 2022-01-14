// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware.authentication;

/**
 * Interface to be implemented by classes that wish to be aware of the sas token.
 */
public interface SasTokenAware {

    /**
     * Get the SAS token
     * @return the SAS token
     */
    String getSasToken();

}
