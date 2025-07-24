// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for SIP routing configuration.
 */
public class SipRoutingCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeSipConfiguration(customization);
        customizeTrunkHealth(customization);
    }

    private void customizeTrunkHealth(LibraryCustomization customization) {
        ClassCustomization trunkHealth = customization.getPackage(BASE_PACKAGE + "models")
            .getClass("TrunkHealth");
        PropertyCustomization tls = trunkHealth.getProperty("tls");
        tls.rename("tlsHealth");
        PropertyCustomization ping = trunkHealth.getProperty("ping");
        ping.rename("pingHealth");
        PropertyCustomization overall = trunkHealth.getProperty("overall");
        overall.rename("overallHealth");
    }

    private void customizeSipConfiguration(LibraryCustomization customization) {
        ClassCustomization sipConfigurationClass = customization.getPackage(IMPLEMENTATION_MODELS_PACKAGE)
            .getClass(SIP_CONFIGURATION_CLASS_NAME);

        // azure-json doesn't write null values, by default. In the context of a collection type, null values should
        // be included.
        sipConfigurationClass.getMethod("toJson").replaceBody(
            "jsonWriter.writeStartObject();                               "
                + "jsonWriter.writeMapField(\"domains\", this.domains, (writer, element) -> { "
                + "    if (element == null) {                                            "
                + "        writer.writeNull();                                           "
                + "    } else {                                                          "
                + "        writer.writeJson(element);                                    "
                + "    }                                                                 "
                + "});                                                                   "    
                + "jsonWriter.writeMapField(\"trunks\", this.trunks, (writer, element) -> { "
                + "    if (element == null) {                                            "
                + "        writer.writeNull();                                           "
                + "    } else {                                                          "
                + "        writer.writeJson(element);                                    "
                + "    }                                                                 "
                + "});                                                                   "
                + "jsonWriter.writeArrayField(\"routes\", this.routes, (writer, element) -> {"
                + "    if (element == null) {                                            "
                + "        writer.writeNull();                                           "
                + "    } else {                                                          "
                + "        writer.writeJson(element);                                    "
                + "    }                                                                 "
                + "});                                                                   "
                + "return jsonWriter.writeEndObject();                                    "
        );
    }
}
