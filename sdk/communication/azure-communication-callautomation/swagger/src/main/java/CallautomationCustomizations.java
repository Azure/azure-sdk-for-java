// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
        } catch (IllegalArgumentException e) {
            logger.info("Package Not Found: " + e.getMessage());
        } catch (Exception e) {
            logger.info("Exception Thrown: " + e.getMessage());
        }
        logger.info("Customization pass");
    }

    private void customizeCallAutomationEnumMembers(LibraryCustomization customization) {
        customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE).getClass(RECORDING_STORAGE_TYPE_INTERNAL_CLASS)
            .renameEnumMember("AZURE_COMMUNICATION_SERVICES", "ACS");

        // As per azure board review renaming the PCM16KMONO to PCM_16K_MONO,PCM24KMONO to PCM_24K_MONO
        customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE).getClass(AUDIO_FORMAT_INTERNAL_CLASS)
            .customizeAst(ast -> ast.getClassByName(AUDIO_FORMAT_INTERNAL_CLASS).ifPresent(clazz -> {
                clazz.getFieldByName("PCM16KMONO").ifPresent(field -> field.getVariable(0).setName("PCM_16K_MONO"));
                clazz.getFieldByName("PCM24KMONO").ifPresent(field -> field.getVariable(0).setName("PCM_24K_MONO"));
            }));

        customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE).getClass(MEDIA_STREAMING_STATUS_DETAILS_CLASS)
            .renameEnumMember("INITIAL_WEB_SOCKET_CONNECTION_FAILED", "INITIAL_WEBSOCKET_CONNECTION_FAILED");
    }
}
