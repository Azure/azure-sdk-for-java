// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import java.lang.reflect.Modifier;
import org.slf4j.Logger;

public class CommunicationRelayCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization models = libraryCustomization.getPackage("com.azure.communication.networktraversal.models");
        String modelToModify = "CommunicationIceServer";

        models.getClass(modelToModify).getMethod("setUrls").setModifier(0);
        models.getClass(modelToModify).getMethod("setUsername").setModifier(0);
        models.getClass(modelToModify).getMethod("setRouteType").setModifier(0);
        models.getClass(modelToModify).getMethod("setCredential").setModifier(0);
        models.getClass(modelToModify).removeAnnotation("Fluent");

        modelToModify = "CommunicationRelayConfiguration";

        models.getClass(modelToModify).getMethod("setExpiresOn").setModifier(0);
        models.getClass(modelToModify).getMethod("setIceServers").setModifier(0);
        models.getClass(modelToModify).removeAnnotation("Fluent");
    }
}
