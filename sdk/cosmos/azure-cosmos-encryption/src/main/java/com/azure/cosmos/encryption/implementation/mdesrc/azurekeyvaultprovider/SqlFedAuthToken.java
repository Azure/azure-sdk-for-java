/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.azurekeyvaultprovider;

import java.io.Serializable;
import java.util.Date;


class SqlFedAuthToken implements Serializable {
    /**
     * Always update serialVersionUID when prompted
     */
    private static final long serialVersionUID = -1343105491285383937L;

    final Date expiresOn;
    final String accessToken;

    SqlFedAuthToken(String accessToken, long expiresIn) {
        this.accessToken = accessToken;

        Date now = new Date();
        now.setTime(now.getTime() + (expiresIn * 1000));
        this.expiresOn = now;
    }

    SqlFedAuthToken(String accessToken, Date expiresOn) {
        this.accessToken = accessToken;
        this.expiresOn = expiresOn;
    }
}
