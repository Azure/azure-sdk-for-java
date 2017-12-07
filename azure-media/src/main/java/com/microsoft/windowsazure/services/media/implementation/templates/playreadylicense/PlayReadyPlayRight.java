package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.util.List;

import javax.xml.bind.Element;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;

/**
 * Configures the Play Right in the PlayReady license.  This right allows the client to play back the content
 * and is required in license templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlayRight")
public class PlayReadyPlayRight {
    
    @XmlElement(name = "AgcAndColorStripeRestriction")
    private AgcAndColorStripeRestriction agcAndColorStripeRestriction;
    
    @XmlElement(name = "AllowPassingVideoContentToUnknownOutput")
    private UnknownOutputPassingOption allowPassingVideoContentToUnknownOutput;
    
    @XmlElement(name = "AnalogVideoOpl")
    private Integer analogVideoOpl;
    
    @XmlElement(name = "CompressedDigitalAudioOpl")
    private Integer compressedDigitalAudioOpl;
    
    @XmlElement(name = "CompressedDigitalVideoOpl")
    private Integer compressedDigitalVideoOpl;
    
    @XmlElement(name = "DigitalVideoOnlyContentRestriction")
    private boolean digitalVideoOnlyContentRestriction;
    
    @XmlElement(name = "ExplicitAnalogTelevisionOutputRestriction")
    private ExplicitAnalogTelevisionRestriction explicitAnalogTelevisionOutputRestriction;
    
    @XmlElement(name = "FirstPlayExpiration")
    private Duration firstPlayExpiration;
  
    @XmlElement(name = "ImageConstraintForAnalogComponentVideoRestriction")
    private boolean imageConstraintForAnalogComponentVideoRestriction;
    
    @XmlElement(name = "ImageConstraintForAnalogComputerMonitorRestriction")
    private boolean imageConstraintForAnalogComputerMonitorRestriction;    
    
    @XmlElement(name = "ScmsRestriction")
    private ScmsRestriction scmsRestriction;    
    
    @XmlElement(name = "UncompressedDigitalAudioOpl")
    private Integer uncompressedDigitalAudioOpl;
    
    @XmlElement(name = "UncompressedDigitalVideoOpl")
    private Integer uncompressedDigitalVideoOpl;
    
    // mimics IExtensibleDataObject
    @XmlAnyElement
    private List<Element> extensionData;

    /**
     * Specifies the amount of time that the license is valid after the license is first used to play content.
     *
     * @return the firstPlayExpiration
     */
    public Duration getFirstPlayExpiration() {
        return firstPlayExpiration;
    }

    /**
     * Specifies the amount of time that the license is valid after the license is first used to play content.
     *
     * @param firstPlayExpiration the firstPlayExpiration to set
     */
    public void setFirstPlayExpiration(Duration firstPlayExpiration) {
        this.firstPlayExpiration = firstPlayExpiration;
    }

    /**
     * Configures the Serial Copy Management System (SCMS) in the license.  SCMS is a form of audio output protection.
     * For further details see the PlayReady Compliance Rules.
     *
     * @return the scmsRestriction
     */
    public ScmsRestriction getScmsRestriction() {
        return scmsRestriction;
    }

    /**
     * Configures the Serial Copy Management System (SCMS) in the license.  SCMS is a form of audio output protection.
     * For further details see the PlayReady Compliance Rules.
     * 
     * @param scmsRestriction the scmsRestriction to set
     */
    public void setScmsRestriction(ScmsRestriction scmsRestriction) {
        this.scmsRestriction = scmsRestriction;
    }

    /**
     * Configures Automatic Gain Control (AGC) and Color Stripe in the license. These are a form of video output
     * protection. For further details see the PlayReady Compliance Rules.
     *
     * @return the agcAndColorStripeRestriction
     */
    public AgcAndColorStripeRestriction getAgcAndColorStripeRestriction() {
        return agcAndColorStripeRestriction;
    }

    /**
     * Configures Automatic Gain Control (AGC) and Color Stripe in the license. These are a form of video output
     * protection. For further details see the PlayReady Compliance Rules.
     *
     * @param agcAndColorStripeRestriction the agcAndColorStripeRestriction to set
     */
    public void setAgcAndColorStripeRestriction(AgcAndColorStripeRestriction agcAndColorStripeRestriction) {
        this.agcAndColorStripeRestriction = agcAndColorStripeRestriction;
    }

    /**
     * Configures the Explicit Analog Television Output Restriction in the license. This is a form of video output
     * protection. For further details see the PlayReady Compliance Rules.
     *
     * @return the explicitAnalogTelevisionOutputRestriction
     */
    public ExplicitAnalogTelevisionRestriction getExplicitAnalogTelevisionOutputRestriction() {
        return explicitAnalogTelevisionOutputRestriction;
    }

    /**
     * Configures the Explicit Analog Television Output Restriction in the license. This is a form of video output
     * protection. For further details see the PlayReady Compliance Rules.
     *
     * @param explicitAnalogTelevisionOutputRestriction the explicitAnalogTelevisionOutputRestriction to set
     */
    public void setExplicitAnalogTelevisionOutputRestriction(ExplicitAnalogTelevisionRestriction explicitAnalogTelevisionOutputRestriction) {
        this.explicitAnalogTelevisionOutputRestriction = explicitAnalogTelevisionOutputRestriction;
    }

    /**
     * Enables the Digital Video Only Content Restriction in the license.  This is a form of video output protection
     * which requires the player to output the video portion of the content over Digital Video Outputs.  For further
     * details see the PlayReady Compliance Rules.
     *
     * @return the digitalVideoOnlyContentRestriction
     */
    public boolean isDigitalVideoOnlyContentRestriction() {
        return digitalVideoOnlyContentRestriction;
    }

    /**
     * Enables the Digital Video Only Content Restriction in the license.  This is a form of video output protection
     * which requires the player to output the video portion of the content over Digital Video Outputs.  For further
     * details see the PlayReady Compliance Rules.
     *
     * @param digitalVideoOnlyContentRestriction the digitalVideoOnlyContentRestriction to set
     */
    public void setDigitalVideoOnlyContentRestriction(boolean digitalVideoOnlyContentRestriction) {
        this.digitalVideoOnlyContentRestriction = digitalVideoOnlyContentRestriction;
    }

    /**
     * Enables the Image Constraint For Analog Component Video Restriction in the license.
     * This is a form of video output protection which requires the player constrain the resolution of the video portion
     * of the content when outputting it over an Analog Component Video Output.
     * For further details see the PlayReady Compliance Rules.
     *
     * @return the imageConstraintForAnalogComponentVideoRestriction
     */
    public boolean isImageConstraintForAnalogComponentVideoRestriction() {
        return imageConstraintForAnalogComponentVideoRestriction;
    }

    /**
     * Enables the Image Constraint For Analog Component Video Restriction in the license.
     * This is a form of video output protection which requires the player constrain the resolution of the video portion
     * of the content when outputting it over an Analog Component Video Output.
     * For further details see the PlayReady Compliance Rules.
     *
     * @param imageConstraintForAnalogComponentVideoRestriction the imageConstraintForAnalogComponentVideoRestriction to set
     */
    public void setImageConstraintForAnalogComponentVideoRestriction(
            boolean imageConstraintForAnalogComponentVideoRestriction) {
        this.imageConstraintForAnalogComponentVideoRestriction = imageConstraintForAnalogComponentVideoRestriction;
    }

    /**
     * This property configures Unknown output handling settings of the license. These settings tell the PlayReady DRM
     * runtime how it should handle unknown video outputs. For further details see the PlayReady Compliance Rules.
     *
     * @return the allowPassingVideoContentToUnknownOutput
     */
    public UnknownOutputPassingOption getAllowPassingVideoContentToUnknownOutput() {
        return allowPassingVideoContentToUnknownOutput;
    }

    /**
     * This property configures Unknown output handling settings of the license. These settings tell the PlayReady DRM
     * runtime how it should handle unknown video outputs. For further details see the PlayReady Compliance Rules.
     *
     * @param allowPassingVideoContentToUnknownOutput the allowPassingVideoContentToUnknownOutput to set
     */
    public void setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption allowPassingVideoContentToUnknownOutput) {
        this.allowPassingVideoContentToUnknownOutput = allowPassingVideoContentToUnknownOutput;
    }

    /**
     * Specifies the output protection level for uncompressed digital video.  Valid values are null, 100, 250, 270, and
     * 300. When the property is set to null, the output protection level is not set in the license. For further details
     * on the meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @return the uncompressedDigitalVideoOpl
     */
    public Integer getUncompressedDigitalVideoOpl() {
        return uncompressedDigitalVideoOpl;
    }

    /**
     * Specifies the output protection level for uncompressed digital video.  Valid values are null, 100, 250, 270, and
     * 300. When the property is set to null, the output protection level is not set in the license. For further details
     * on the meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @param uncompressedDigitalVideoOpl the uncompressedDigitalVideoOpl to set
     */
    public void setUncompressedDigitalVideoOpl(Integer uncompressedDigitalVideoOpl) {
        int value = uncompressedDigitalVideoOpl != null ? uncompressedDigitalVideoOpl.intValue() : -1;
        if ((uncompressedDigitalVideoOpl != null) && (value != 100) && (value != 250) && (value != 270) && (value != 300)) {
            throw new IllegalArgumentException(ErrorMessages.UNCOMPRESSED_DIGITAL_VIDEO_OPL_VALUE_ERROR);
        }
        this.uncompressedDigitalVideoOpl = uncompressedDigitalVideoOpl;
    }

    /**
     * Specifies the output protection level for compressed digital video. Valid values are null, 400, and 500. When the
     * property is set to null, the output protection level is not set in the license. For further details on the
     * meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @return the compressedDigitalVideoOpl
     */
    public Integer getCompressedDigitalVideoOpl() {
        return compressedDigitalVideoOpl;
    }

    /**
     * Specifies the output protection level for compressed digital video. Valid values are null, 400, and 500. When the
     * property is set to null, the output protection level is not set in the license. For further details on the
     * meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @param compressedDigitalVideoOpl the compressedDigitalVideoOpl to set
     */
    public void setCompressedDigitalVideoOpl(Integer compressedDigitalVideoOpl) {
        int value = compressedDigitalVideoOpl != null ? compressedDigitalVideoOpl.intValue() : -1;
        if ((compressedDigitalVideoOpl != null) && (value != 400) && (value != 500)) {
            throw new IllegalArgumentException(ErrorMessages.COMPRESSED_DIGITAL_VIDEO_OPL_VALUE_ERROR);
        }
        this.compressedDigitalVideoOpl = compressedDigitalVideoOpl;
    }

    /**
     * Specifies the output protection level for analog video. Valid values are null, 100, 150, and 200. When the
     * property is set to null, the output protection level is not set in the license.  For further details on the
     * meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @return the analogVideoOpl
     */
    public Integer getAnalogVideoOpl() {
        return analogVideoOpl;
    }

    /**
     * Specifies the output protection level for analog video. Valid values are null, 100, 150, and 200. When the
     * property is set to null, the output protection level is not set in the license.  For further details on the
     * meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @param analogVideoOpl the analogVideoOpl to set
     */
    public void setAnalogVideoOpl(Integer analogVideoOpl) {
        int value = analogVideoOpl != null ? analogVideoOpl.intValue() : -1;
        if ((analogVideoOpl != null) && (value != 100) && (value != 150) && (value != 200)) {
            throw new IllegalArgumentException(ErrorMessages.ANALOG_VIDEO_OPL_VALUE_ERROR);
        }
        this.analogVideoOpl = analogVideoOpl;
    }

    /**
     * Specifies the output protection level for compressed digital audio. Valid values are null, 100, 150, 200, 250,
     * and 300. When the property is set to null, the output protection level is not set in the license. For further
     * details on the meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @return the compressedDigitalAudioOpl
     */
    public Integer getCompressedDigitalAudioOpl() {
        return compressedDigitalAudioOpl;
    }

    /**
     * Specifies the output protection level for compressed digital audio. Valid values are null, 100, 150, 200, 250,
     * and 300. When the property is set to null, the output protection level is not set in the license. For further
     * details on the meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @param compressedDigitalAudioOpl the compressedDigitalAudioOpl to set
     */
    public void setCompressedDigitalAudioOpl(Integer compressedDigitalAudioOpl) {
        int value = compressedDigitalAudioOpl != null ? compressedDigitalAudioOpl.intValue() : -1;
        if ((compressedDigitalAudioOpl != null) && (value != 100) && (value != 150) && (value != 200) && (value != 250) && (value != 300)) {
            throw new IllegalArgumentException(ErrorMessages.COMPRESSED_DIGITAL_AUDIO_OPL_VALUE_ERROR);
        }
        this.compressedDigitalAudioOpl = compressedDigitalAudioOpl;
    }

    /**
     * Specifies the output protection level for uncompressed digital audio. Valid values are 100, 150, 200, 250, and
     * 300. When the property is set to null, the output protection level is not set in the license. For further details
     * on the meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @return the uncompressedDigitalAudioOpl
     */
    public Integer getUncompressedDigitalAudioOpl() {
        return uncompressedDigitalAudioOpl;
    }

    /**
     * Specifies the output protection level for uncompressed digital audio. Valid values are 100, 150, 200, 250, and
     * 300. When the property is set to null, the output protection level is not set in the license. For further details
     * on the meaning of the specific value see the PlayReady Compliance Rules.
     *
     * @param uncompressedDigitalAudioOpl the uncompressedDigitalAudioOpl to set
     */
    public void setUncompressedDigitalAudioOpl(Integer uncompressedDigitalAudioOpl) {
        int value = uncompressedDigitalAudioOpl != null ? uncompressedDigitalAudioOpl.intValue() : -1;
        if ((uncompressedDigitalAudioOpl != null) && (value != 100) && (value != 150) && (value != 200) && (value != 250) && (value != 300)) {
            throw new IllegalArgumentException(ErrorMessages.UNCOMPRESSED_DIGITAL_AUDIO_OPL_VALUE_ERROR);
        }
        this.uncompressedDigitalAudioOpl = uncompressedDigitalAudioOpl;
    }

    /**
     * @return the imageConstraintForAnalogComputerMonitorRestriction
     */
    public boolean isImageConstraintForAnalogComputerMonitorRestriction() {
        return imageConstraintForAnalogComputerMonitorRestriction;
    }

    /**
     * Enables the Image Constraint For Analog Computer Monitor Restriction in the license. This is a form of video
     * output protection which requires the player constrain the resolution of the video portion of the content when
     * outputting it over an Analog Computer Monitor Output. For further details see the PlayReady Compliance Rules.
     *
     * @param imageConstraintForAnalogComputerMonitorRestriction the imageConstraintForAnalogComputerMonitorRestriction to set
     */
    public void setImageConstraintForAnalogComputerMonitorRestriction(
            boolean imageConstraintForAnalogComputerMonitorRestriction) {
        this.imageConstraintForAnalogComputerMonitorRestriction = imageConstraintForAnalogComputerMonitorRestriction;
    }

    /**
     * Enables the Image Constraint For Analog Computer Monitor Restriction in the license. This is a form of video
     * output protection which requires the player constrain the resolution of the video portion of the content when
     * outputting it over an Analog Computer Monitor Output. For further details see the PlayReady Compliance Rules.
     *
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
