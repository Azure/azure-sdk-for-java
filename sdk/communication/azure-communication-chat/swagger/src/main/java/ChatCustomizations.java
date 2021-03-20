// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.models.Modifier;

public class ChatCustomizations extends Customization {
    private static final String MODELS = "com.azure.communication.chat.models";

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeModelsPackage(libraryCustomization.getPackage(MODELS));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        ClassCustomization communicationErrorCls =
            packageCustomization.getClass("CommunicationError")
            .rename("ChatError");

        communicationErrorCls
            .getMethod("setCode")
            .setModifier(Modifier.PRIVATE);

        communicationErrorCls
            .getMethod("setMessage")
            .setModifier(Modifier.PRIVATE);

        packageCustomization.getClass("CommunicationErrorResponse")
            .rename("ChatErrorResponse");

        ClassCustomization responseExceptionCls
            = packageCustomization.getClass("CommunicationErrorResponseException")
            .rename("ChatErrorResponseException");

        responseExceptionCls
            .addMethod(
            "/**\n" +
                " * Get error \n" +
                " * @return the error.\n" +
                " */\n" +
                "public ChatError getError() { \n" +
                "    return this.getValue() != null ? this.getValue().getError() : null; \n" +
                "}"
        );
    }
}
