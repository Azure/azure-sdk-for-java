package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

public final class ErrorMessages {

    public static final String PRIMARY_VERIFICATIONKEY_AND_OPENIDCONNECTDISCOVERYDOCUMENT_ARE_NULL 
        = "Both PrimaryVerificationKey and OpenIdConnectDiscoveryDocument are null.";

    public static final String OPENIDDISCOVERYURI_STRING_IS_NULL_OR_EMPTY 
        = "OpenIdConnectDiscoveryDocument.OpenIdDiscoveryUri string value is null or empty.";

    public static final String OPENIDDISCOVERYURI_STRING_IS_NOT_ABSOLUTE_URI 
        = "String representation of OpenIdConnectDiscoveryDocument.OpenIdDiscoveryUri is not valid absolute Uri.";

    private ErrorMessages() {
    }
}
