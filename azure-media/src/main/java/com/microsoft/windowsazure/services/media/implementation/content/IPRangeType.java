package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class IPRangeType {
    
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "Address", namespace = Constants.ODATA_DATA_NS)
    private String address;

    @XmlElement(name = "SubnetPrefixLength", namespace = Constants.ODATA_DATA_NS)
    private int subnetPrefixLength;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSubnetPrefixLength() {
        return subnetPrefixLength;
    }

    public void setSubnetPrefixLength(int subnetPrefixLength) {
        this.subnetPrefixLength = subnetPrefixLength;
    }
    
}
