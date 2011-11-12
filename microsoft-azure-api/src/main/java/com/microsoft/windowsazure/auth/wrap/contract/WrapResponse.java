package com.microsoft.windowsazure.auth.wrap.contract;

public class WrapResponse {
    String accessToken;
    long expiresIn;

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken
     *            the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return the expiresIn
     */
    public long getExpiresIn() {
        return expiresIn;
    }

    /**
     * @param expiresIn
     *            the expiresIn to set
     */
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
