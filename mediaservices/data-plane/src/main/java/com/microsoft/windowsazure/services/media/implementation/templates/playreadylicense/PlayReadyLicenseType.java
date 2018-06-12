package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.security.InvalidParameterException;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum PlayReadyLicenseType {
    
    @XmlEnumValue("Nonpersistent") Nonpersistent(0),
    @XmlEnumValue("Persistent") Persistent(1);
    
    private int playReadyLicenseType;
    
    private PlayReadyLicenseType(int playReadyLicenseType) {
        this.playReadyLicenseType = playReadyLicenseType;
    }
    
    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return playReadyLicenseType;
    }
    
    /**
     * From code.
     * 
     * @param code
     *            the code
     * @return the content key type
     */
    public static PlayReadyLicenseType fromCode(int code) {
        switch (code) {
        case 0:
            return PlayReadyLicenseType.Nonpersistent;
        case 1:
            return PlayReadyLicenseType.Persistent;
        default:
            throw new InvalidParameterException("code");
        }
    }
}
