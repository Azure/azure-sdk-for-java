package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TokenClaim")
public class TokenClaim {
    
    private static final String CONTENT_KEY_ID_CLAIM_TYPE = "urn:microsoft:azure:mediaservices:contentkeyidentifier";
    
    private static final TokenClaim CONTENT_KEY_ID_CLAIM = new TokenClaim(CONTENT_KEY_ID_CLAIM_TYPE, null);

    @XmlElement(name = "ClaimType", required = true)
    private String claimType;
    
    @XmlElement(name = "ClaimValue", required = true, nillable = true)
    private String claimValue;
    
    public TokenClaim() {
        
    }
     
    public TokenClaim(String claimType, String claimValue) {
        if (claimType == null) {
            throw new NullPointerException("claimType");
        }
        setClaimType(claimType);
        setClaimValue(claimValue);
    }

    /**
     * @return the claimType
     */
    public String getClaimType() {
        return claimType;
    }

    /**
     * @param claimType the claimType to set
     * @return this
     */
    public TokenClaim setClaimType(String claimType) {
        this.claimType = claimType;
        return this;
    }

    /**
     * @return the claimValue
     */
    public String getClaimValue() {
        return claimValue;
    }

    /**
     * @param claimValue the claimValue to set
     * @return this
     */
    public TokenClaim setClaimValue(String claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    /**
     * @return the contentKeyIdentifierClaimType
     */
    public static String getContentKeyIdentifierClaimType() {
        return CONTENT_KEY_ID_CLAIM_TYPE;
    }

    /**
     * @return the contentkeyidentifierclaim
     */
    public static TokenClaim getContentKeyIdentifierClaim() {
        return CONTENT_KEY_ID_CLAIM;
    }
}
