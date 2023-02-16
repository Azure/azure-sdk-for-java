// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;
import java.lang.reflect.Modifier;

/**
 * Customizations for return type swagger code generation.
 */
public class JobRouterCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {

        // Implementation models customizations
        PackageCustomization implementationModels = customization.getPackage("com.azure.communication.jobrouter.implementation.models");
        implementationModels.getClass("DistributionMode").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("ExceptionAction").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("RouterRule").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("WorkerSelectorAttachment").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("QueueSelectorAttachment").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);

        // Models customizations
        PackageCustomization models = customization.getPackage("com.azure.communication.jobrouter.models");
    }
}
