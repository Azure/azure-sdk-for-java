package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.util.Date;
import java.util.List;

import javax.xml.bind.Element;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlayReadyLicenseTemplate")
public class PlayReadyLicenseTemplate {
    
    @XmlElement(name = "AllowTestDevices")
    private boolean allowTestDevices;
    
    @XmlElement(name = "BeginDate")
    private Date beginDate;
    
    @XmlElement(name = "ContentKey")
    private PlayReadyContentKey contentKey;
    
    @XmlElement(name = "ExpirationDate")
    private Date expirationDate;
    
    @XmlElement(name = "GracePeriod")
    private Duration gracePeriod;
    
    @XmlElement(name = "LicenseType")
    private PlayReadyLicenseType licenseType; 
    
    @XmlElement(name = "PlayRight")
    private PlayReadyPlayRight playRight;    
    
    @XmlElement(name = "RelativeBeginDate")
    private Duration relativeBeginDate;
    
    @XmlElement(name = "RelativeExpirationDate")
    private Duration relativeExpirationDate;
    
    // mimics IExtensibleDataObject
    @XmlAnyElement
    private List<Element> extensionData;


    /**
     * @return the allowTestDevices
     */
    public boolean isAllowTestDevices() {
        return allowTestDevices;
    }

    /**
     * @param allowTestDevices the allowTestDevices to set
     */
    public void setAllowTestDevices(boolean allowTestDevices) {
        this.allowTestDevices = allowTestDevices;
    }

    /**
     * @return the beginDate
     */
    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * @param beginDate the beginDate to set
     */
    public void setBeginDate(Date beginDate) {
        //
        //  License template should not have both BeginDate and RelativeBeginDate set.
        //  Only one of these two values should be set.
        if (relativeBeginDate != null) {            
            throw new IllegalArgumentException(ErrorMessages.BEGIN_DATE_AND_RELATIVE_BEGIN_DATE_CANNOTBE_SET_SIMULTANEOUSLY_ERROR);
        }
        this.beginDate = beginDate;
    }

    /**
     * @return the expirationDate
     */
    public Date getExpirationDate() {
       return expirationDate;
    }

    /**
     * @param expirationDate the expirationDate to set
     */
    public void setExpirationDate(Date expirationDate) {
        //
        //  License template should not have both ExpirationDate and RelativeExpirationDate set.
        //  Only one of these two values should be set.
        if (relativeExpirationDate != null) {
            throw new IllegalArgumentException("Set ExpirationDate or RelativeExpirationDate but not both");
        }
        this.expirationDate = expirationDate;
    }

    /**
     * @return the relativeBeginDate
     */
    public Duration getRelativeBeginDate() {
        return relativeBeginDate;
    }

    /**
     * @param relativeBeginDate the relativeBeginDate to set
     */
    public void setRelativeBeginDate(Duration relativeBeginDate) {
        //
        //  License template should not have both BeginDate and RelativeBeginDate set.
        //  Only one of these two values should be set.
        if (beginDate != null) {
            throw new IllegalArgumentException("Set BeginDate or RelativeBeginDate but not both");
        }
        this.relativeBeginDate = relativeBeginDate;
    }

    /**
     * @return the relativeExpirationDate
     */
    public Duration getRelativeExpirationDate() {
        return relativeExpirationDate;
    }

    /**
     * @param relativeExpirationDate the relativeExpirationDate to set
     */
    public void setRelativeExpirationDate(Duration relativeExpirationDate) {
        //
        //  License template should not have both ExpirationDate and RelativeExpirationDate set.
        //  Only one of these two values should be set.
        if (expirationDate != null) {
            throw new IllegalArgumentException("Set ExpirationDate or RelativeExpirationDate but not both");
        }
        this.relativeExpirationDate = relativeExpirationDate;
    }

    /**
     * @return the gracePeriod
     */
    public Duration getGracePeriod() {
        return gracePeriod;
    }

    /**
     * @param gracePeriod the gracePeriod to set
     */
    public void setGracePeriod(Duration gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    /**
     * @return the playRight
     */
    public PlayReadyPlayRight getPlayRight() {
        return playRight;
    }

    /**
     * @param playRight the playRight to set
     */
    public void setPlayRight(PlayReadyPlayRight playRight) {
        this.playRight = playRight;
    }

    /**
     * @return the licenseType
     */
    public PlayReadyLicenseType getLicenseType() {
        return licenseType;
    }

    /**
     * @param licenseType the licenseType to set
     */
    public void setLicenseType(PlayReadyLicenseType licenseType) {
        this.licenseType = licenseType;
    }

    /**
     * @return the contentKey
     */
    public PlayReadyContentKey getContentKey() {
        return contentKey;
    }

    /**
     * @param contentKey the contentKey to set
     */
    public void setContentKey(PlayReadyContentKey contentKey) {
        this.contentKey = contentKey;
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
