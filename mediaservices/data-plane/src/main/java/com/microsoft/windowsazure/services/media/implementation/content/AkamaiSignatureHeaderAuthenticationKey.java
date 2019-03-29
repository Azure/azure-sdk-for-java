package com.microsoft.windowsazure.services.media.implementation.content;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class AkamaiSignatureHeaderAuthenticationKey {

    @XmlElement(name = "Identifier", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "Expiration", namespace = Constants.ODATA_DATA_NS)
    private Date expiration;

    @XmlElement(name = "Base64Key", namespace = Constants.ODATA_DATA_NS)
    private String base64Key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getBase64Key() {
        return base64Key;
    }

    public void setBase64Key(String base64Key) {
        this.base64Key = base64Key;
    }
}
