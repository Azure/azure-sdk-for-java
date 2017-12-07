package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configures the Explicit Analog Television Output Restriction in the license.
 * This is a form of video output protection. For further details see the PlayReady Compliance Rules.
 */
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
     * Controls whether the Explicit Analog Television Output Restriction is enforced on a Best Effort basis or not.
     * If true, then the PlayReady client must make its best effort to enforce the restriction but can allow video
     * content to flow to Analog Television Outputs if it cannot support the restriction.
     * If false, the PlayReady client must enforce the restriction.
     * For further details see the PlayReady Compliance Rules.
     *
     * @return the bestEffort
     */
    public boolean isBestEffort() {
        return bestEffort;
    }

    /**
     * Controls whether the Explicit Analog Television Output Restriction is enforced on a Best Effort basis or not.
     * If true, then the PlayReady client must make its best effort to enforce the restriction but can allow video
     * content to flow to Analog Television Outputs if it cannot support the restriction.
     * If false, the PlayReady client must enforce the restriction.
     * For further details see the PlayReady Compliance Rules.
     *
     * @param bestEffort
     *            the bestEffort to set
     * @return this
     */
    public ExplicitAnalogTelevisionRestriction setBestEffort(boolean bestEffort) {
        this.bestEffort = bestEffort;
        return this;
    }

    /**
     * Configures the Explicit Analog Television Output Restriction control bits.
     * For further details see the PlayReady Compliance Rules.
     *
     * @return the configurationData
     */
    public byte getConfigurationData() {
        return configurationData;
    }   
}
