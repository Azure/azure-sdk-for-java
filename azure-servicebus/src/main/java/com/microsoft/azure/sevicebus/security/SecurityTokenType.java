package com.microsoft.azure.sevicebus.security;

public enum SecurityTokenType {
    SAS(SecurityConstants.SAS_TOKEN_TYPE),
    JWT(SecurityConstants.JWT_TOKEN_TYPE);
    
    private final String tokenTypeString;
    SecurityTokenType(String tokenTypeString)
    {
        this.tokenTypeString = tokenTypeString;
    }
    
    @Override
    public String toString()
    {
        return this.tokenTypeString;
    }
}
