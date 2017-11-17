package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExplicitAnalogTelevisionRestriction")
public class ExplicitAnalogTelevisionRestriction {

    @XmlElement(name = "BestEffort")
    private boolean bestEffort;

    @XmlElement(name = "ConfigurationData")
    private byte configurationData;
    
    @SuppressWarnings("unused")
    private ExplicitAnalogTelevisionRestriction() {
    }

    public ExplicitAnalogTelevisionRestriction(boolean bestEffort, byte configurationData) {
        ScmsRestriction.verifyTwoBitConfigurationData(configurationData);
        this.bestEffort = bestEffort;
        this.configurationData = configurationData;
    }

    /**
     * @return the bestEffort
     */
    public boolean isBestEffort() {
        return bestEffort;
    }

    /**
     * @param bestEffort
     *            the bestEffort to set
     * @return this
     */
    public ExplicitAnalogTelevisionRestriction setBestEffort(boolean bestEffort) {
        this.bestEffort = bestEffort;
        return this;
    }

    /**
     * @return the configurationData
     */
    public byte getConfigurationData() {
        return configurationData;
    }   
}
