package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "TokenRestrictionTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class TokenRestrictionTemplate {
    
    @XmlElementWrapper(name = "AlternateVerificationKeys")
    @XmlElement(name = "TokenVerificationKey")
    private List<TokenVerificationKey> alternateVerificationKeys;
    
    @XmlElement(name = "Audience", required = true)
    private URI audience;
    
    @XmlElement(name = "Issuer", required = true)
    private URI issuer;
    
    @XmlElement(name = "PrimaryVerificationKey")
    private TokenVerificationKey primaryVerificationKey;
    
    @XmlElementWrapper(name = "RequiredClaims")
    @XmlElement(name = "TokenClaim")
    private List<TokenClaim> requiredClaims;
    
    @XmlElement(name = "TokenType")
    private TokenType tokenType;

    @XmlElement(name = "OpenIdConnectDiscoveryDocument")
    private OpenIdConnectDiscoveryDocument openIdConnectDiscoveryDocument;
    
    @SuppressWarnings("unused")
    private TokenRestrictionTemplate() {
        this.setTokenType(TokenType.SWT);
        initCollections();
    }
    
    public TokenRestrictionTemplate(TokenType tokenType) {
        this.setTokenType(tokenType);
        initCollections();
    }
    
    private void initCollections() {
        setRequiredClaims(new ArrayList<TokenClaim>());
        setAlternateVerificationKeys(new ArrayList<TokenVerificationKey>());
    }
    
    /**
     * @return the audience
     */
    public URI getAudience() {
        return audience;
    }

    /**
     * @param audience the audience to set
     * @return this
     */
    public TokenRestrictionTemplate setAudience(URI audience) {
        this.audience = audience;
        return this;
    }

    /**
     * @return the issuer
     */
    public URI getIssuer() {
        return issuer;
    }

    /**
     * @param issuer the issuer to set
     * @return this
     */
    public TokenRestrictionTemplate setIssuer(URI issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * @return the tokenType
     */
    public TokenType getTokenType() {
        return tokenType;
    }

    /**
     * @param tokenType the tokenType to set
     * @return this
     */
    public TokenRestrictionTemplate setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    /**
     * @return the primaryVerificationKey
     */
    public TokenVerificationKey getPrimaryVerificationKey() {
        return primaryVerificationKey;
    }

    /**
     * @param primaryVerificationKey the primaryVerificationKey to set
     * @return this
     */
    public TokenRestrictionTemplate setPrimaryVerificationKey(TokenVerificationKey primaryVerificationKey) {
        this.primaryVerificationKey = primaryVerificationKey;
        return this;
    }

    /**
     * @return the requiredClaims
     */
    public List<TokenClaim> getRequiredClaims() {
        return requiredClaims;
    }

    /**
     * @param requiredClaims the requiredClaims to set
     * @return this
     */
    public TokenRestrictionTemplate setRequiredClaims(List<TokenClaim> requiredClaims) {
        this.requiredClaims = requiredClaims;
        return this;
    }

    /**
     * @return the alternateVerificationKeys
     */
    public List<TokenVerificationKey> getAlternateVerificationKeys() {
        return alternateVerificationKeys;
    }

    /**
     * @param alternateVerificationKeys the alternateVerificationKeys to set
     * @return this
     */
    public TokenRestrictionTemplate setAlternateVerificationKeys(List<TokenVerificationKey> alternateVerificationKeys) {
        this.alternateVerificationKeys = alternateVerificationKeys;
        return this;
    }
    
    /**
     * @return the alternateVerificationKeys
     */
    public OpenIdConnectDiscoveryDocument getOpenIdConnectDiscoveryDocument() {
        return openIdConnectDiscoveryDocument;
    }

    /**
     * @param alternateVerificationKeys the alternateVerificationKeys to set
     * @return this
     */
    public TokenRestrictionTemplate setOpenIdConnectDiscoveryDocument(OpenIdConnectDiscoveryDocument openIdConnectDiscoveryDocument) {
        this.openIdConnectDiscoveryDocument = openIdConnectDiscoveryDocument;
        return this;
    }
    
}
