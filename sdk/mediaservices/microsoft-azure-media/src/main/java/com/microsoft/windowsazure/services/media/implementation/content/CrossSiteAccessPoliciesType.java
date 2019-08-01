package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class CrossSiteAccessPoliciesType {

    @XmlElement(name = "ClientAccessPolicy", namespace = Constants.ODATA_DATA_NS)
    private String clientAccessPolicy;
    
    @XmlElement(name = "CrossDomainPolicy", namespace = Constants.ODATA_DATA_NS)
    private String crossDomainPolicy;

    public String getClientAccessPolicy() {
        return clientAccessPolicy;
    }

    public void setClientAccessPolicy(String clientAccessPolicy) {
        this.clientAccessPolicy = clientAccessPolicy;
    }

    public String getCrossDomainPolicy() {
        return crossDomainPolicy;
    }

    public void setCrossDomainPolicy(String crossDomainPolicy) {
        this.crossDomainPolicy = crossDomainPolicy;
    }
}
