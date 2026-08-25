// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import org.slf4j.Logger;

/**
 * Customization that preserves the {@code beginDeleteUpdate} protocol API copied from
 * {@code azure-iot-deviceupdate}.
 *
 * <p>The predecessor library returns {@code BinaryData} as the final result of the protocol LRO overloads.
 * The TypeSpec emitter instead derives {@code Void} because the delete operation has an empty {@code 204}
 * response. This customization keeps the copied protocol API consistent while leaving the model convenience
 * overloads unchanged.</p>
 */
public class SoftwareUpdateCustomization extends Customization {

    private static final String ROOT_PACKAGE = "com.azure.iot.deviceregistry.softwareupdate";
    private static final String IMPL_PACKAGE = "com.azure.iot.deviceregistry.softwareupdate.implementation";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        logger.info("Restoring 'beginDeleteUpdate' protocol overload final-result type to BinaryData.");

        restoreBinaryDataFinalResult(libraryCustomization.getClass(ROOT_PACKAGE, "SoftwareUpdateClient"),
            "beginDeleteUpdate", false);
        restoreBinaryDataFinalResult(libraryCustomization.getClass(ROOT_PACKAGE, "SoftwareUpdateAsyncClient"),
            "beginDeleteUpdate", false);

        ClassCustomization impl = libraryCustomization.getClass(IMPL_PACKAGE, "SoftwareUpdatesImpl");
        restoreBinaryDataFinalResult(impl, "beginDeleteUpdate", true);
        restoreBinaryDataFinalResult(impl, "beginDeleteUpdateAsync", true);
    }

    private static void restoreBinaryDataFinalResult(ClassCustomization classCustomization, String methodName,
        boolean rewriteFinalResultTypeReference) {
        classCustomization.customizeAst(ast -> ast.getClassByName(classCustomization.getClassName())
            .ifPresent(clazz -> clazz.getMethodsByName(methodName).forEach(method -> {
                String returnType = method.getTypeAsString().replace(" ", "");
                if (!returnType.equals("SyncPoller<BinaryData,Void>")
                    && !returnType.equals("PollerFlux<BinaryData,Void>")) {
                    return;
                }

                method.setType(StaticJavaParser.parseType(method.getTypeAsString().replace("Void", "BinaryData")));

                if (rewriteFinalResultTypeReference) {
                    method.getBody().ifPresent(body -> method.setBody(StaticJavaParser.parseBlock(
                        body.toString().replace("TypeReference.createInstance(Void.class)",
                            "TypeReference.createInstance(BinaryData.class)"))));
                }
            })));
    }
}