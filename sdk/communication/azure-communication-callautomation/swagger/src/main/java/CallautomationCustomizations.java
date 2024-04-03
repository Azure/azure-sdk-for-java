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

    public static final String CALL_AUTOMATION_CLASS_NAME = "RecordingStorageTypeInternal";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        try {
            customizeCallAutomation(customization);
        } catch (IllegalArgumentException e) {
            logger.info("Yes failed " + e.getMessage());
        } catch (Exception e) {
            logger.info("No terrible " + e.getMessage());
        }
        logger.info("Customization pass");
    }

    private void customizeCallAutomation(LibraryCustomization customization) {
        ClassCustomization recordingStorageTypeInternalConfigurationClass = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE)
            .getClass(CALL_AUTOMATION_CLASS_NAME);
        // recordingStorageTypeInternalConfigurationClass
        //     .getProperty("AZURE_COMMUNICATION_SERVICES")
        //     .rename("ACS");
    }

}
