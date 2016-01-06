package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenIdConnectDiscoveryDocument")
public class OpenIdConnectDiscoveryDocument {

    @XmlElement(name = "OpenIdDiscoveryUri")
    private String openIdDiscoveryUri;

    /**
     * @return the openIdDiscoveryUri
     */
    public String getOpenIdDiscoveryUri() {
        return openIdDiscoveryUri;
    }

    /**
     * @param openIdDiscoveryUri the openIdDiscoveryUri to set
     */
    public void setOpenIdDiscoveryUri(String openIdDiscoveryUri) {
        this.openIdDiscoveryUri = openIdDiscoveryUri;
    }
    
}
