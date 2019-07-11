package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class StreamingEndpointAccessControlType {
    
    @XmlElement(name = "Akamai", namespace = Constants.ODATA_DATA_NS)
    private AkamaiAccessControlType akamai;
    
    @XmlElement(name = "IP", namespace = Constants.ODATA_DATA_NS)
    private IPAccessControlType iP;
    
    public AkamaiAccessControlType getAkamai() {
        return akamai;
    }

    public void setAkamai(AkamaiAccessControlType akamai) {
        this.akamai = akamai;
    }

    public IPAccessControlType getIP() {
        return iP;
    }

    public void setIP(IPAccessControlType iP) {
        this.iP = iP;
    }
}
