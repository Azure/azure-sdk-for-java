package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import java.util.List;

import javax.xml.bind.Element;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;

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
     * @return the firstPlayExpiration
     */
    public Duration getFirstPlayExpiration() {
        return firstPlayExpiration;
    }

    /**
     * @param firstPlayExpiration the firstPlayExpiration to set
     */
    public void setFirstPlayExpiration(Duration firstPlayExpiration) {
        this.firstPlayExpiration = firstPlayExpiration;
    }

    /**
     * @return the scmsRestriction
     */
    public ScmsRestriction getScmsRestriction() {
        return scmsRestriction;
    }

    /**
     * @param scmsRestriction the scmsRestriction to set
     */
    public void setScmsRestriction(ScmsRestriction scmsRestriction) {
        this.scmsRestriction = scmsRestriction;
    }

    /**
     * @return the agcAndColorStripeRestriction
     */
    public AgcAndColorStripeRestriction getAgcAndColorStripeRestriction() {
        return agcAndColorStripeRestriction;
    }

    /**
     * @param agcAndColorStripeRestriction the agcAndColorStripeRestriction to set
     */
    public void setAgcAndColorStripeRestriction(AgcAndColorStripeRestriction agcAndColorStripeRestriction) {
        this.agcAndColorStripeRestriction = agcAndColorStripeRestriction;
    }

    /**
     * @return the explicitAnalogTelevisionOutputRestriction
     */
    public ExplicitAnalogTelevisionRestriction getExplicitAnalogTelevisionOutputRestriction() {
        return explicitAnalogTelevisionOutputRestriction;
    }

    /**
     * @param explicitAnalogTelevisionOutputRestriction the explicitAnalogTelevisionOutputRestriction to set
     */
    public void setExplicitAnalogTelevisionOutputRestriction(ExplicitAnalogTelevisionRestriction explicitAnalogTelevisionOutputRestriction) {
        this.explicitAnalogTelevisionOutputRestriction = explicitAnalogTelevisionOutputRestriction;
    }

    /**
     * @return the digitalVideoOnlyContentRestriction
     */
    public boolean isDigitalVideoOnlyContentRestriction() {
        return digitalVideoOnlyContentRestriction;
    }

    /**
     * @param digitalVideoOnlyContentRestriction the digitalVideoOnlyContentRestriction to set
     */
    public void setDigitalVideoOnlyContentRestriction(boolean digitalVideoOnlyContentRestriction) {
        this.digitalVideoOnlyContentRestriction = digitalVideoOnlyContentRestriction;
    }

    /**
     * @return the imageConstraintForAnalogComponentVideoRestriction
     */
    public boolean isImageConstraintForAnalogComponentVideoRestriction() {
        return imageConstraintForAnalogComponentVideoRestriction;
    }

    /**
     * @param imageConstraintForAnalogComponentVideoRestriction the imageConstraintForAnalogComponentVideoRestriction to set
     */
    public void setImageConstraintForAnalogComponentVideoRestriction(
            boolean imageConstraintForAnalogComponentVideoRestriction) {
        this.imageConstraintForAnalogComponentVideoRestriction = imageConstraintForAnalogComponentVideoRestriction;
    }

    /**
     * @return the allowPassingVideoContentToUnknownOutput
     */
    public UnknownOutputPassingOption getAllowPassingVideoContentToUnknownOutput() {
        return allowPassingVideoContentToUnknownOutput;
    }

    /**
     * @param allowPassingVideoContentToUnknownOutput the allowPassingVideoContentToUnknownOutput to set
     */
    public void setAllowPassingVideoContentToUnknownOutput(UnknownOutputPassingOption allowPassingVideoContentToUnknownOutput) {
        this.allowPassingVideoContentToUnknownOutput = allowPassingVideoContentToUnknownOutput;
    }

    /**
     * @return the uncompressedDigitalVideoOpl
     */
    public Integer getUncompressedDigitalVideoOpl() {
        return uncompressedDigitalVideoOpl;
    }

    /**
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
     * @return the compressedDigitalVideoOpl
     */
    public Integer getCompressedDigitalVideoOpl() {
        return compressedDigitalVideoOpl;
    }

    /**
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
     * @return the analogVideoOpl
     */
    public Integer getAnalogVideoOpl() {
        return analogVideoOpl;
    }

    /**
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
     * @return the compressedDigitalAudioOpl
     */
    public Integer getCompressedDigitalAudioOpl() {
        return compressedDigitalAudioOpl;
    }

    /**
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
     * @return the uncompressedDigitalAudioOpl
     */
    public Integer getUncompressedDigitalAudioOpl() {
        return uncompressedDigitalAudioOpl;
    }

    /**
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
     * @param imageConstraintForAnalogComputerMonitorRestriction the imageConstraintForAnalogComputerMonitorRestriction to set
     */
    public void setImageConstraintForAnalogComputerMonitorRestriction(
            boolean imageConstraintForAnalogComputerMonitorRestriction) {
        this.imageConstraintForAnalogComputerMonitorRestriction = imageConstraintForAnalogComputerMonitorRestriction;
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
