package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Class AsymmetricTokenVerificationKey represents asymmetric keys which are used in token verification scenarios.
 */
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public abstract class AsymmetricTokenVerificationKey extends TokenVerificationKey {

    /**
     * the raw body of a key.
     */
    private byte[] rawBody;

    /**
     * Gets the raw body of a key.
     * @return the rawBody
     */
    @XmlElement(name = "RawBody", required = true, nillable = true)
    public byte[] getRawBody() {
        return rawBody;
    }

    /**
     * Sets the raw body of a key.
     * @param rawBody the rawBody to set
     */
    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
    }
}
