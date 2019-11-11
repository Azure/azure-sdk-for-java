package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

public final class ErrorMessages {

    public static final String UNCOMPRESSED_DIGITAL_AUDIO_OPL_VALUE_ERROR 
        = "The value can only be set to null, 100, 150, 200, 250, or 300.";
    public static final String UNCOMPRESSED_DIGITAL_VIDEO_OPL_VALUE_ERROR 
        = "The value can only be set to null, 100, 250, 270, or 300.";
    public static final String BEGIN_DATE_AND_RELATIVE_BEGIN_DATE_CANNOTBE_SET_SIMULTANEOUSLY_ERROR 
        = "Set BeginDate or RelativeBeginDate but not both";    
    public static final String EXPIRATION_DATE_AND_RELATIVE_EXPIRATION_DATE_CANNOTBE_SET_SIMULTANEOUSLY_ERROR
        = "Set ExpirationDate or RelativeExpirationDate but not both";    
    public static final String PLAY_READY_PLAY_RIGHT_REQUIRED 
        = "Each PlayReadyLicenseTemplate in the PlayReadyLicenseResponseTemplate must have a PlayReadyPlayRight";    
    public static final String PLAY_READY_CONTENT_KEY_REQUIRED 
        = "Each PlayReadyLicenseTemplate in the PlayReadyLicenseResponseTemplate must have either a ContentEncryptionKeyFromHeader or a ContentEncryptionKeyFromKeyIdentifier";
    public static final String INVALID_TWO_BIT_CONFIGURATION_DATA 
        = "ConfigurationData must be 0, 1, 2, or 3";
    public static final String GRACE_PERIOD_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE 
        = "GracePeriod cannot be set on Non Persistent licenses.";
    public static final String FIRST_PLAY_EXPIRATION_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE 
        = "FirstPlayExpiration cannot be set on the PlayRight of a Non Persistent license.";
    public static final String EXPIRATION_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE 
        = "ExpirationDate cannot be set on Non Persistent licenses.";
    public static final String DIGITAL_VIDEO_ONLY_MUTUALLY_EXCLUSIVE_WITH_PASSING_TO_UNKNOWN_OUTPUT_ERROR 
        = "PlayReady does not allow passing to unknown outputs if the DigitalVideoOnlyContentRestriction is enabled.";
    public static final String COMPRESSED_DIGITAL_VIDEO_OPL_VALUE_ERROR 
        = "The value can only be set to null, 400, or 500.";
    public static final String COMPRESSED_DIGITAL_AUDIO_OPL_VALUE_ERROR 
        = "The value can only be set to null, 100, 150, 200, 250, or 300.";
    public static final String BEGIN_DATE_CANNOT_BE_SET_ON_NON_PERSISTENT_LICENSE 
        = "BeginDate cannot be set on Non Persistent licenses.";
    public static final String AT_LEAST_ONE_LICENSE_TEMPLATE_REQUIRED 
        = "A PlayReadyLicenseResponseTemplate must have at least one PlayReadyLicenseTemplate";
    public static final String ANALOG_VIDEO_OPL_VALUE_ERROR 
        = "The value can only be set to null, 100, 150, or 200.";

    private ErrorMessages() {
    }

}
