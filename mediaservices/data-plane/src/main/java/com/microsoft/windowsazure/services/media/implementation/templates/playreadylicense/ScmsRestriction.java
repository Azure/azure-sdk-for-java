package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScmsRestriction")
public class ScmsRestriction {

    @XmlElement(name = "ConfigurationData")
    private byte configurationData;
    
    @SuppressWarnings("unused")
    private ScmsRestriction() {
    }

    public ScmsRestriction(byte configurationData) {
        verifyTwoBitConfigurationData(configurationData);
        this.configurationData = configurationData;
    }

    /**
     * @return the configurationData
     */
    public byte getConfigurationData() {
        return configurationData;
    }
    
    public static void verifyTwoBitConfigurationData(byte configurationData) {
       if ((configurationData & 0x3) != configurationData) {
           throw new IllegalArgumentException(ErrorMessages.INVALID_TWO_BIT_CONFIGURATION_DATA);
       }
    }
}
