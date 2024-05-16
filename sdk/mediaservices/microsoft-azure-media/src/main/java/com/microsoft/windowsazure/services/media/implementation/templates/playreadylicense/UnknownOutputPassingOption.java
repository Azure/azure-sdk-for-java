package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.security.InvalidParameterException;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum UnknownOutputPassingOption {
    
    @XmlEnumValue("NotAllowed") NotAllowed(0),
    @XmlEnumValue("Allowed") Allowed(1),
    @XmlEnumValue("AllowedWithVideoConstriction") AllowedWithVideoConstriction(2);
    
    private int unknownOutputPassingOption;
    
    private UnknownOutputPassingOption(int unknownOutputPassingOption) {
        this.unknownOutputPassingOption = unknownOutputPassingOption;
    }
    
    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return unknownOutputPassingOption;
    }
    
    /**
     * From code.
     * 
     * @param code
     *            the code
     * @return the content key type
     */
    public static UnknownOutputPassingOption fromCode(int code) {
        switch (code) {
        case 0:
            return UnknownOutputPassingOption.NotAllowed;
        case 1:
            return UnknownOutputPassingOption.Allowed;
        case 2:
            return UnknownOutputPassingOption.AllowedWithVideoConstriction;
        default:
            throw new InvalidParameterException("code");
        }
    }
}
