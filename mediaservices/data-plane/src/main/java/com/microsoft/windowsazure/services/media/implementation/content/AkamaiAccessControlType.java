package com.microsoft.windowsazure.services.media.implementation.content;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class AkamaiAccessControlType {
    
    @XmlElementWrapper(name = "AkamaiSignatureHeaderAuthenticationKeyList", namespace = Constants.ODATA_DATA_NS)
    @XmlElement(name = "element", namespace = Constants.ODATA_DATA_NS)
    private List<AkamaiSignatureHeaderAuthenticationKey> akamaiSignatureHeaderAuthenticationKeyList;

    public List<AkamaiSignatureHeaderAuthenticationKey> getAkamaiSignatureHeaderAuthenticationKeyList() {
        return akamaiSignatureHeaderAuthenticationKeyList;
    }

    public void setAkamaiSignatureHeaderAuthenticationKeyList(List<AkamaiSignatureHeaderAuthenticationKey> akamaiSignatureHeaderAuthenticationKeyList) {
        this.akamaiSignatureHeaderAuthenticationKeyList = akamaiSignatureHeaderAuthenticationKeyList;
    }

}
