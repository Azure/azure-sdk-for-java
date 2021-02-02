package com.azure.core.experimental.implementation;

public class AuthenticationChallenge {
    private String scheme;
    private String challengeParameters;

    public AuthenticationChallenge(String scheme, String challengeParameters) {
        this.scheme = scheme;
        this.challengeParameters = challengeParameters;
    }

    public String getChallengeParameters() {
        return challengeParameters;
    }

    public String getScheme() {
        return scheme;
    }
}

