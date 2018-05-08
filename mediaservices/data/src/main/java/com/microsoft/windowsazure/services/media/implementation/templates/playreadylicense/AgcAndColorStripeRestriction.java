package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgcAndColorStripeRestriction")
public class AgcAndColorStripeRestriction {

    @XmlElement(name = "ConfigurationData")
    private byte configurationData;
    
    @SuppressWarnings("unused")
    private AgcAndColorStripeRestriction() {        
    }

    public AgcAndColorStripeRestriction(byte configurationData) {
        ScmsRestriction.verifyTwoBitConfigurationData(configurationData);
        this.configurationData = configurationData;
    }

    /**
     * @return the configurationData
     */
    public byte getConfigurationData() {
        return configurationData;
    }    
}
