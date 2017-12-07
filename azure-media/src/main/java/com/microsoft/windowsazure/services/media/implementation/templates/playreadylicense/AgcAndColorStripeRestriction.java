package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configures Automatic Gain Control (AGC) and Color Stripe in the license. These are a form of video output protection.
 * For further details see the PlayReady Compliance Rules.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgcAndColorStripeRestriction")
public class AgcAndColorStripeRestriction {

    @XmlElement(name = "ConfigurationData")
    private byte configurationData;
    
    @SuppressWarnings("unused")
    private AgcAndColorStripeRestriction() {        
    }

    /**
     * Configures the Automatic Gain Control (AGC) and Color Stripe control bits.
     * For further details see the PlayReady Compliance Rules.
     *
     * @param configurationData
     */
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
