package com.microsoft.windowsazure.services.media.implementation.content;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ContentKeyAuthorizationPolicyOptionType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "KeyDeliveryType", namespace = Constants.ODATA_DATA_NS)
    private int keyDeliveryType;

    @XmlElement(name = "KeyDeliveryConfiguration", namespace = Constants.ODATA_DATA_NS)
    private String keyDeliveryConfiguration;

    @XmlElementWrapper(name = "Restrictions", namespace = Constants.ODATA_DATA_NS)
    @XmlElement(name = "element", namespace = Constants.ODATA_DATA_NS)
    private List<ContentKeyAuthorizationPolicyRestrictionType> restrictions;

    public String getId() {
        return id;
    }

    public ContentKeyAuthorizationPolicyOptionType setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ContentKeyAuthorizationPolicyOptionType setName(String name) {
        this.name = name;
        return this;
    }

    public int getKeyDeliveryType() {
        return keyDeliveryType;
    }

    public ContentKeyAuthorizationPolicyOptionType setKeyDeliveryType(int keyDeliveryType) {
        this.keyDeliveryType = keyDeliveryType;
        return this;
    }

    public String getKeyDeliveryConfiguration() {
        return keyDeliveryConfiguration;
    }

    public ContentKeyAuthorizationPolicyOptionType setKeyDeliveryConfiguration(String keyDeliveryConfiguration) {
        this.keyDeliveryConfiguration = keyDeliveryConfiguration;
        return this;
    }

    public List<ContentKeyAuthorizationPolicyRestrictionType> getRestrictions() {
        return restrictions;
    }

    public ContentKeyAuthorizationPolicyOptionType setRestrictions(
            List<ContentKeyAuthorizationPolicyRestrictionType> restrictions) {
        this.restrictions = restrictions;
        return this;
    }
}
