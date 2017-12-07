package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.security.InvalidParameterException;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configures the license server to embed the content key identified by the KeyIdentifier property of the
 * ContentEncryptionKeyFromKeyIdentifier in the returned license.
 * This is not typcially used but does allow a specific content key identifier to be put in the license template.
 * Note that if the content key returned in the license does not match the content key needed to play the content
 * (which is configured in the header) the player will be unable to play the content.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContentEncryptionKeyFromKeyIdentifier")
public class ContentEncryptionKeyFromKeyIdentifier extends PlayReadyContentKey {

    @XmlElement(name = "KeyIdentifier")
    private UUID keyIdentifier;
    
    public ContentEncryptionKeyFromKeyIdentifier() {
        
    }

    public ContentEncryptionKeyFromKeyIdentifier(UUID keyIdentifier) {
        if (keyIdentifier.equals(new UUID(0L, 0L))) {
            throw new InvalidParameterException("keyIdentifier");
        }

        this.keyIdentifier = keyIdentifier;
    }

    /**
     * Identifier of the content key to embed in the license.
     *
     * @return the keyIdentifier
     */
    public UUID getKeyIdentifier() {
        return keyIdentifier;
    }

    /**
     * Identifier of the content key to embed in the license.
     *
     * @param keyIdentifier
     *            the keyIdentifier to set
     */
    public void setKeyIdentifier(UUID keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

}
