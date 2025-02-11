// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for call automation configuration.
 */
public class CallautomationCustomizations extends Customization {
    private static final String BASE_PACKAGE = "com.azure.communication.callautomation.";
    private static final String IMPLEMENTATION_MODELS_PACKAGE = BASE_PACKAGE + "implementation.models";
    
    // Enum class names
    public static final String RECORDING_STORAGE_TYPE_INTERNAL_CLASS = "RecordingStorageTypeInternal";
    public static final String AUDIO_FORMAT_INTERNAL_CLASS = "AudioFormatInternal";
    public static final String MEDIA_STREAMING_STATUS_DETAILS_CLASS = "MediaStreamingStatusDetails";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        try {
            customizeCallAutomationEnumMembers(customization);
            encodePathParam(customization, logger);
        } catch (IllegalArgumentException e) {
            logger.info("Package Not Found: " + e.getMessage());
        } catch (Exception e) {
            logger.info("Exception Thrown: " + e.getMessage());
        }
        logger.info("Customization pass");
    }

    // getParticipant participantRawId is not encoded which results HMAC failures on the backend,
    // to fix the issue, currently overriding the getParticipant signature and sending the participantRawId as encoded
    // This needs to be fixed in the GA release
    private void encodePathParam(LibraryCustomization customization, Logger logger) {
        String replace = customization.getRawEditor().getFileContent("src/main/java/com/azure/communication/callautomation/implementation/CallConnectionsImpl.java")
            .replace("@PathParam(\"participantRawId\") String participantRawId,", "@PathParam(value = \"participantRawId\", encoded = true) String participantRawId,");
        customization.getRawEditor().replaceFile("src/main/java/com/azure/communication/callautomation/implementation/CallConnectionsImpl.java", replace);
    }

    private void customizeCallAutomationEnumMembers(LibraryCustomization customization) {
        ClassCustomization recordingStorageTypeInternalConfigurationClass = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE)
            .getClass(RECORDING_STORAGE_TYPE_INTERNAL_CLASS);
        recordingStorageTypeInternalConfigurationClass
            .renameEnumMember("AZURE_COMMUNICATION_SERVICES", "ACS");

        // As per azure board review renaming the PCM16KMONO to PCM_16K_MONO,PCM24KMONO to PCM_24K_MONO
        ClassCustomization audioFormatInternalClass = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE)
            .getClass(AUDIO_FORMAT_INTERNAL_CLASS);
        audioFormatInternalClass
            .renameEnumMember("PCM16KMONO", "PCM_16K_MONO")
            .renameEnumMember("PCM24KMONO", "PCM_24K_MONO");

        ClassCustomization mediaStreamingStatusDetailsClass = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE)
            .getClass(MEDIA_STREAMING_STATUS_DETAILS_CLASS);
        mediaStreamingStatusDetailsClass
            .renameEnumMember("INITIAL_WEB_SOCKET_CONNECTION_FAILED", "INITIAL_WEBSOCKET_CONNECTION_FAILED");
    }
}