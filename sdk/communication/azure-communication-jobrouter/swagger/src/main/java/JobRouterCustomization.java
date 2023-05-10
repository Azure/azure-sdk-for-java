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
        implementationModels.getClass("DistributionModeInternal").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("ExceptionActionInternal").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("RouterRuleInternal").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("WorkerSelectorAttachmentInternal").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
        implementationModels.getClass("QueueSelectorAttachmentInternal").setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);

        // Models customizations
        PackageCustomization models = customization.getPackage("com.azure.communication.jobrouter.models");
    }

//    @Override
//    public void customize(LibraryCustomization customization) {
//    }
}
