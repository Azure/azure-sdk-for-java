/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.msiAuthTokenProvider;


import java.util.Calendar;
import java.util.Date;

/**
 * Type representing response from the local MSI token provider.
 */
public class MSIToken {
    private String tokenType;

    private String accessToken;

    private Date expiresOn;

    public MSIToken(String accessToken, String tokenType, Date expiresOn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresOn = expiresOn;
    }

    public String accessToken() {
        return accessToken;
    }

    public String tokenType() {
        return tokenType;
    }

    public Date expiresOn() {
        return expiresOn;
    }

    public boolean isExpired() {
        //get time now
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        Date timeNow = today.getTime();

        return (timeNow.after(expiresOn));
    }
}