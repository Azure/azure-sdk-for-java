// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * This class contains the customization code to customize the AutoRest generated code for OpenAI.
 */
public class FaceCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        ClassCustomization classCustomization = customization.getClass(
                "com.azure.ai.vision.face.administration",
                "FaceAdministrationClientBuilder");

        classCustomization.removeAnnotation("@ServiceClientBuilder");
        classCustomization.addAnnotation("@ServiceClientBuilder(\n" +
                "    serviceClients = {\n" +
                "        FaceAdministrationClient.class,\n" +
                "        FaceAdministrationAsyncClient.class,\n" +
                "        LargeFaceListClient.class,\n" +
                "        LargePersonGroupClient.class,\n" +
                "        LargeFaceListAsyncClient.class,\n" +
                "        LargePersonGroupAsyncClient.class })");
    }
}
