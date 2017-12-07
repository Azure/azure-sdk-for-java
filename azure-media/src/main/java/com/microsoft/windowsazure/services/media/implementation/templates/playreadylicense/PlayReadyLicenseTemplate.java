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

/**
 * Represents a license template for creating PlayReady licenses to return to clients.
 */
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
     * Controls whether test devices can use the license or not. If true, the MinimumSecurityLevel property of the
     * license is set to 150. If false (the default), the MinimumSecurityLevel property of the license is set to 2000.
     *
     * @return the allowTestDevices
     */
    public boolean isAllowTestDevices() {
        return allowTestDevices;
    }

    /**
     * Controls whether test devices can use the license or not. If true, the MinimumSecurityLevel property of the
     * license is set to 150. If false (the default), the MinimumSecurityLevel property of the license is set to 2000.
     *
     * @param allowTestDevices the allowTestDevices to set
     */
    public void setAllowTestDevices(boolean allowTestDevices) {
        this.allowTestDevices = allowTestDevices;
    }

    /**
     * Configures the starting DateTime that the license is valid. Attempts to use the license before this date and
     * time will result in an error on the client.
     *
     * @return the beginDate
     */
    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * Configures the starting DateTime that the license is valid. Attempts to use the license before this date and
     * time will result in an error on the client.
     *
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
     * Configures the DateTime value when the the license expires. Attempts to use the license after this date and time
     * will result in an error on the client.
     *
     * @return the expirationDate
     */
    public Date getExpirationDate() {
       return expirationDate;
    }

    /**
     * Configures the DateTime value when the the license expires. Attempts to use the license after this date and time
     * will result in an error on the client.
     *
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
     * Configures starting DateTime value when the license is valid. Attempts to use the license before this date and
     * time will result in an error on the client. The DateTime value is calculated as
     * DateTime.UtcNow + RelativeBeginDate when the license is issued.
     *
     * @return the relativeBeginDate
     */
    public Duration getRelativeBeginDate() {
        return relativeBeginDate;
    }

    /**
     * Configures starting DateTime value when the license is valid. Attempts to use the license before this date and
     * time will result in an error on the client. The DateTime value is calculated as
     * DateTime.UtcNow + RelativeBeginDate when the license is issued.
     *
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
     * Configures the DateTime value when the license expires. Attempts to use the license after this date and time
     * will result in an error on the client. The DateTime value is calculated as
     * DateTime.UtcNow + RelativeExpirationDate when the license is issued.
     *
     * @return the relativeExpirationDate
     */
    public Duration getRelativeExpirationDate() {
        return relativeExpirationDate;
    }

    /**
     * Configures the DateTime value when the license expires. Attempts to use the license after this date and time
     * will result in an error on the client. The DateTime value is calculated as
     * DateTime.UtcNow + RelativeExpirationDate when the license is issued.
     *
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
     * Configures the Grace Period setting of the PlayReady license. This setting affects how DateTime based
     * restrictions are evaluated on certain devices in the situation that the devices secure clock becomes unset.
     *
     * @return the gracePeriod
     */
    public Duration getGracePeriod() {
        return gracePeriod;
    }

    /**
     * Configures the Grace Period setting of the PlayReady license. This setting affects how DateTime based
     * restrictions are evaluated on certain devices in the situation that the devices secure clock becomes unset.
     *
     * @param gracePeriod the gracePeriod to set
     */
    public void setGracePeriod(Duration gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    /**
     * Configures the PlayRight of the PlayReady license. This Right gives the client the ability to play back the
     * content. The PlayRight also allows configuring restrictions specific to playback. This Right is required.
     *
     * @return the playRight
     */
    public PlayReadyPlayRight getPlayRight() {
        return playRight;
    }

    /**
     * Configures the PlayRight of the PlayReady license. This Right gives the client the ability to play back the
     * content. The PlayRight also allows configuring restrictions specific to playback. This Right is required.
     *
     * @param playRight the playRight to set
     */
    public void setPlayRight(PlayReadyPlayRight playRight) {
        this.playRight = playRight;
    }

    /**
     * Configures whether the license is persistent (saved in persistent storage on the client) or non-persistent (only
     * held in memory while the player is using the license). Persistent licenses are typically used to allow offline
     * playback of the content.
     *
     * @return the licenseType
     */
    public PlayReadyLicenseType getLicenseType() {
        return licenseType;
    }

    /**
     * Configures whether the license is persistent (saved in persistent storage on the client) or non-persistent (only
     * held in memory while the player is using the license). Persistent licenses are typically used to allow offline
     * playback of the content.
     *
     * @param licenseType the licenseType to set
     */
    public void setLicenseType(PlayReadyLicenseType licenseType) {
        this.licenseType = licenseType;
    }

    /**
     * Specifies the content key in the license. This is typically set to an instance of the
     * ContentEncryptionKeyFromHeader object to allow the template to be applied to multiple content keys and have the
     * content header tell the license server the exact key to embed in the license issued to the client.
     *
     * @return the contentKey
     */
    public PlayReadyContentKey getContentKey() {
        return contentKey;
    }

    /**
     * Specifies the content key in the license. This is typically set to an instance of the
     * ContentEncryptionKeyFromHeader object to allow the template to be applied to multiple content keys and have the
     * content header tell the license server the exact key to embed in the license issued to the client.
     *
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
