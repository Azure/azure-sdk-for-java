package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ContentKeyAuthorizationPolicyRestrictionType implements MediaServiceDTO {

    /** The name. */
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    /** The KeyRestrictionType. */
    @XmlElement(name = "KeyRestrictionType", namespace = Constants.ODATA_DATA_NS)
    private int keyRestrictionType;

    /** The KeyRestrictionType. */
    @XmlElement(name = "Requirements", namespace = Constants.ODATA_DATA_NS)
    private String requirements;

    public String getName() {
        return name;
    }

    public ContentKeyAuthorizationPolicyRestrictionType setName(String name) {
        this.name = name;
        return this;
    }

    public int getKeyRestrictionType() {
        return keyRestrictionType;
    }

    public ContentKeyAuthorizationPolicyRestrictionType setKeyRestrictionType(int keyRestrictionType) {
        this.keyRestrictionType = keyRestrictionType;
        return this;
    }

    public String getRequirements() {
        return requirements;
    }

    public ContentKeyAuthorizationPolicyRestrictionType setRequirements(String requirements) {
        this.requirements = requirements;
        return this;
    }

}
