package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Element;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PlayReadyLicenseResponseTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlayReadyLicenseResponseTemplate {
    
    @XmlElementWrapper(name = "LicenseTemplates")
    @XmlElement(name = "PlayReadyLicenseTemplate")
    private List<PlayReadyLicenseTemplate> licenseTemplates;
    
    @XmlElement(name = "ResponseCustomData")
    private String responseCustomData;
    
    // mimics IExtensibleDataObject
    @XmlAnyElement
    private List<Element> extensionData;
    
    public PlayReadyLicenseResponseTemplate() {
        internalConstruct();
    }

    private void internalConstruct() {
        setLicenseTemplates(new ArrayList<PlayReadyLicenseTemplate>());
        setExtensionData(new ArrayList<Element>());
    }

    /**
     * @return the licenseTemplates
     */
    public List<PlayReadyLicenseTemplate> getLicenseTemplates() {
        return licenseTemplates;
    }

    /**
     * @param licenseTemplates the licenseTemplates to set
     */
    private void setLicenseTemplates(List<PlayReadyLicenseTemplate> licenseTemplates) {
        this.licenseTemplates = licenseTemplates;
    }
    
    /**
     * @return the responseCustomData
     */
    public String getResponseCustomData() {
        return responseCustomData;
    }

    /**
     * @param responseCustomData the responseCustomData to set
     */
    public void setResponseCustomData(String responseCustomData) {
        this.responseCustomData = responseCustomData;
    }

    /**
     * @return the extensionData
     */
    public List<Element> getExtensionData() {
        return extensionData;
    }

    /**
     * @param extensionData the extensionData to set
     */
    public void setExtensionData(List<Element> extensionData) {
        this.extensionData = extensionData;
    }
    
}
