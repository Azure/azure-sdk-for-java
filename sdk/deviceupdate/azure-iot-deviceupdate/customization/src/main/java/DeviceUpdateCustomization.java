// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import org.slf4j.Logger;

/**
 * Customization that preserves backward compatibility of the {@code beginDeleteUpdate} long-running
 * operation's protocol overloads.
 *
 * <p>The prior GA library ({@code azure-iot-deviceupdate} 1.0.x) shipped these protocol LRO overloads
 * with a final-result type of {@code BinaryData}. After migrating to TypeSpec, the emitter derives the
 * final-result type as {@code Void} (the delete operation has an empty {@code 204} response), which is a
 * binary breaking change: {@code SyncPoller<BinaryData, BinaryData>} became {@code SyncPoller<BinaryData, Void>}
 * (and the async {@code PollerFlux} equivalent).</p>
 *
 * <p>This cannot be expressed in {@code client.tsp}/{@code tspconfig.yaml}, so this customization restores the
 * final-result type to {@code BinaryData} on the public sync/async clients and on the underlying implementation
 * client (both the method return type and the poller's final-result {@link com.azure.core.util.serializer.TypeReference}).</p>
 */
public class DeviceUpdateCustomization extends Customization {

    private static final String ROOT_PACKAGE = "com.azure.iot.deviceupdate";
    private static final String IMPL_PACKAGE = "com.azure.iot.deviceupdate.implementation";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        logger.info("Restoring 'beginDeleteUpdate' protocol overload final-result type to BinaryData for backward compatibility.");

        // Public clients: only the method return type needs to change; the body simply delegates to the
        // implementation client, whose return type is retyped below.
        restoreBinaryDataFinalResult(libraryCustomization.getClass(ROOT_PACKAGE, "DeviceUpdateClient"),
            "beginDeleteUpdate", false);
        restoreBinaryDataFinalResult(libraryCustomization.getClass(ROOT_PACKAGE, "DeviceUpdateAsyncClient"),
            "beginDeleteUpdate", false);

        // Implementation client: retype the method and rewrite the poller's final-result TypeReference.
        ClassCustomization impl = libraryCustomization.getClass(IMPL_PACKAGE, "DeviceUpdateClientImpl");
        restoreBinaryDataFinalResult(impl, "beginDeleteUpdate", true);
        restoreBinaryDataFinalResult(impl, "beginDeleteUpdateAsync", true);
    }

    /**
     * Retypes the protocol overload of {@code methodName} from {@code <BinaryData, Void>} back to
     * {@code <BinaryData, BinaryData>}. Only the protocol overload (whose return type is exactly
     * {@code SyncPoller<BinaryData, Void>} or {@code PollerFlux<BinaryData, Void>}) is affected; the model
     * convenience overloads ({@code <UpdateOperation, Void>}) are left untouched.
     *
     * @param classCustomization the class to customize.
     * @param methodName the method name to retype.
     * @param rewriteFinalResultTypeReference when {@code true}, also rewrites
     * {@code TypeReference.createInstance(Void.class)} to {@code TypeReference.createInstance(BinaryData.class)}
     * in the method body (needed for the implementation client that constructs the poller).
     */
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
