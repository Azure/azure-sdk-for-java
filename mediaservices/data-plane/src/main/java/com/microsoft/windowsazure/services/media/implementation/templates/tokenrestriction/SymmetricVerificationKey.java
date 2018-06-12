package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.microsoft.windowsazure.services.media.EncryptionUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SymmetricVerificationKey")
public class SymmetricVerificationKey extends TokenVerificationKey {
    
    @XmlElement(name = "KeyValue")
    private byte[] keyValue;
   
    /**
     * Constructs a SymmetricVerificationKey using a randomly generated key value.
     * The key value generated is 64 bytes long.
     */
    public SymmetricVerificationKey() {
        keyValue = new byte[64];
        EncryptionUtils.eraseKey(keyValue);
    }
    
    /**
     * Constructs a SymmetricVerificationKey using the provided key value.
     * @param keyValue the provided key value
     */
    public SymmetricVerificationKey(byte[] keyValue) {
        this.keyValue = keyValue;
    }

    /**
     * @return the keyValue
     */
    public byte[] getKeyValue() {
        return keyValue;
    }

    /**
     * @param keyValue the keyValue to set
     */
    public void setKeyValue(byte[] keyValue) {
        this.keyValue = keyValue;
    }
    
    
}
