package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import java.security.InvalidParameterException;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum TokenType {
    
    @XmlEnumValue("Undefined") Undefined(0),
    @XmlEnumValue("SWT") SWT(1),
    @XmlEnumValue("JWT") JWT(2);
    
    private int tokenType;
    
    private TokenType(int tokenType) {
        this.tokenType = tokenType;
    }
    
    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return tokenType;
    }
    
    /**
     * From code.
     * 
     * @param code
     *            the code
     * @return the content key type
     */
    public static TokenType fromCode(int code) {
        switch (code) {
        case 0:
            return TokenType.Undefined;
        case 1:
            return TokenType.SWT;
        case 2:
            return TokenType.JWT;
        default:
            throw new InvalidParameterException("code");
        }
    }
}
