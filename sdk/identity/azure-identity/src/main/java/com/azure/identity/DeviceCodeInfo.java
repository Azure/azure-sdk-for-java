// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * Response returned from the STS device code endpoint containing information necessary for
 * device code flow.
 */
@Immutable
public class DeviceCodeInfo {
    /**
     * Creates an instance of a device code info.
     *
     * @param userCode code which user needs to provide when authenticating at the verification URL
     * @param deviceCode code which should be included in the request for the access token
     * @param verificationUrl URL where user can authenticate
     * @param expiresOn expiration time of device code in seconds
     * @param message message which should be displayed to the user
     */
    public DeviceCodeInfo(String userCode, String deviceCode, String verificationUrl, OffsetDateTime expiresOn,
                          String message) {
        this.userCode = userCode;
        this.deviceCode = deviceCode;
        this.verificationUrl = verificationUrl;
        this.expiresOn = expiresOn;
        this.message = message;
    }

    private final String userCode;

    private final String deviceCode;

    private final String verificationUrl;

    private final OffsetDateTime expiresOn;

    private final String message;

    /**
     * Gets the code which user needs to provide when authenticating at the verification URL.
     *
     * @return code which user needs to provide when authenticating at the verification URL.
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * Gets the code which should be included in the request for the access token.
     *
     * @return code which should be included in the request for the access token.
     */
    public String getDeviceCode() {
        return deviceCode;
    }

    /**
     * Gets the URL where user can authenticate.
     *
     * @return URL where user can authenticate.
     */
    public String getVerificationUrl() {
        return verificationUrl;
    }

    /**
     * Gets the expiration time of device code.
     *
     * @return expiration time of device code.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * Gets the message which should be displayed to the user.
     *
     * @return message which should be displayed to the user.
     */
    public String getMessage() {
        return message;
    }
}
