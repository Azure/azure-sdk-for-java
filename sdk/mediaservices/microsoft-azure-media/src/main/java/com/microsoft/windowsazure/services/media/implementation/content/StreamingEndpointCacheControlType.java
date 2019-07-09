package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class StreamingEndpointCacheControlType {

    @XmlElement(name = "MaxAge", namespace = Constants.ODATA_DATA_NS)
    private Integer maxAge;

    public int getMaxAge() {
        return maxAge == null ? 0 : maxAge.intValue();
    }

    public void setMaxAge(int maxRange) {
        this.maxAge = maxRange;
    }
}
