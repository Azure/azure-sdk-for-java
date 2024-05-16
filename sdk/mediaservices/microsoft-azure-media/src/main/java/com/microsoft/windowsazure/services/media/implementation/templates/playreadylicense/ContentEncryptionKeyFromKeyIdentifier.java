package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.security.InvalidParameterException;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
     * @return the keyIdentifier
     */
    public UUID getKeyIdentifier() {
        return keyIdentifier;
    }

    /**
     * @param keyIdentifier
     *            the keyIdentifier to set
     */
    public void setKeyIdentifier(UUID keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

}
