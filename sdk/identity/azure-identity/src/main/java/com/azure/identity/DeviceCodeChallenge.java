package com.azure.identity;

import com.microsoft.aad.msal4j.DeviceCode;

/**
 * Response returned from the STS device code endpoint containing information necessary for
 * device code flow
 */
public class DeviceCodeChallenge {
    DeviceCodeChallenge(DeviceCode deviceCode) {
        this.userCode = deviceCode.userCode();
        this.deviceCode = deviceCode.deviceCode();
        this.verificationUri = deviceCode.verificationUri();
        this.expiresIn = deviceCode.expiresIn();
        this.interval = deviceCode.interval();
        this.message = deviceCode.message();
    }

    /**
     * code which user needs to provide when authenticating at the verification URI
     */
    private String userCode;

    /**
     * @return code which user needs to provide when authenticating at the verification URI
     */
    public String userCode() {
        return userCode;
    }

    /**
     * code which should be included in the request for the access token
     */
    private String deviceCode;

    /**
     * @return code which should be included in the request for the access token
     */
    public String deviceCode() {
        return deviceCode;
    }

    /**
     * URI where user can authenticate
     */
    private String verificationUri;

    /**
     * @return URI where user can authenticate
     */
    public String verificationUri() {
        return verificationUri;
    }

    /**
     * expiration time of device code in seconds.
     */
    private long expiresIn;

    /**
     * @return expiration time of device code in seconds.
     */
    public long expiresIn() {
        return expiresIn;
    }

    /**
     * interval at which the STS should be polled at
     */
    private long interval;

    /**
     * @return interval at which the STS should be polled at
     */
    public long interval() {
        return interval;
    }

    /**
     * message which should be displayed to the user.
     */
    private String message;

    /**
     * @return message which should be displayed to the user.
     */
    public String message() {
        return message;
    }
}
