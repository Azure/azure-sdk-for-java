// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.implementation.annotation.Fluent;
import com.azure.core.implementation.annotation.Immutable;
import com.microsoft.aad.msal4j.DeviceCode;

import java.util.Objects;

/**
 * Response returned from the STS device code endpoint containing information necessary for
 * device code flow
 */
@Fluent
@Immutable
public class DeviceCodeChallenge {
    /**
     * Creates an instance of a device code challenge.
     *
     * @param userCode code which user needs to provide when authenticating at the verification URI
     * @param deviceCode code which should be included in the request for the access token
     * @param verificationUri URI where user can authenticate
     * @param expiresIn expiration time of device code in seconds
     * @param interval interval at which the STS should be polled at
     * @param message message which should be displayed to the user
     */
    public DeviceCodeChallenge(String userCode, String deviceCode, String verificationUri, long expiresIn, long interval, String message) {
        this.userCode = userCode;
        this.deviceCode = deviceCode;
        this.verificationUri = verificationUri;
        this.expiresIn = expiresIn;
        this.interval = interval;
        this.message = message;
    }

    private String userCode;

    private String deviceCode;

    private String verificationUri;

    private long expiresIn;

    private long interval;

    private String message;


    /**
     * @return code which user needs to provide when authenticating at the verification URI.
     */
    public String userCode() {
        return userCode;
    }

    /**
     * @return code which should be included in the request for the access token.
     */
    public String deviceCode() {
        return deviceCode;
    }

    /**
     * @return URI where user can authenticate.
     */
    public String verificationUri() {
        return verificationUri;
    }

    /**
     * @return expiration time of device code in seconds.
     */
    public long expiresIn() {
        return expiresIn;
    }

    /**
     * @return interval at which the STS should be polled at.
     */
    public long interval() {
        return interval;
    }

    /**
     * @return message which should be displayed to the user.
     */
    public String message() {
        return message;
    }
}
