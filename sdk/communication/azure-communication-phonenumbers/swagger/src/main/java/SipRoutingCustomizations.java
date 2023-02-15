// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;


/**
 * Contains customizations for SIP routing configuration.
 */
public class SipRoutingCustomizations extends Customization {
    private static final String BASE_PACKAGE = "com.azure.communication.phonenumbers.siprouting.";
    private static final String IMPLEMENTATION_MODELS_PACKAGE = BASE_PACKAGE + "implementation.models";

    public static final String SIP_CONFIGURATION_CLASS_NAME = "SipConfiguration";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeSipConfiguration(customization);
    }

    private void customizeSipConfiguration(LibraryCustomization customization) {
        ClassCustomization sipConfigurationClass = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE)
            .getClass(SIP_CONFIGURATION_CLASS_NAME);
        sipConfigurationClass
            .getProperty("trunks")
            .addAnnotation("@JsonInclude(value = JsonInclude.Include.NON_ABSENT)");
        sipConfigurationClass
            .getProperty("routes")
            .addAnnotation("@JsonInclude(value = JsonInclude.Include.NON_ABSENT)");
    }

}
