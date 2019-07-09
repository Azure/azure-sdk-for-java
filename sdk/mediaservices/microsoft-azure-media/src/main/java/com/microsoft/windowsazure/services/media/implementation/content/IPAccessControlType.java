package com.microsoft.windowsazure.services.media.implementation.content;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class IPAccessControlType {

    @XmlElementWrapper(name = "Allow", namespace = Constants.ODATA_DATA_NS)
    @XmlElement(name = "element", namespace = Constants.ODATA_DATA_NS)
    private List<IPRangeType> ipRange;

    public List<IPRangeType> getIpRange() {
        return ipRange;
    }

    public void setIpRange(List<IPRangeType> ipRange) {
        this.ipRange = ipRange;
    }
}
