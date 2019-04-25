package com.microsoft.azure.servicebus.security;

/**
 * Enumeration representing security token types supported by Azure Service Bus.
 * @since 1.2.0
 *
 */
public enum SecurityTokenType {
    /**
     * Shared Access Signature token type
     */
    SAS(SecurityConstants.SAS_TOKEN_TYPE),
    /**
     * JSON web token type
     */
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
