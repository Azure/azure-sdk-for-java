// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoftwareUpdateCustomizationTest {
    private static final String ROOT_PATH = "src/main/java/com/azure/iot/deviceregistry/softwareupdate/";
    private static final String IMPL_PATH = ROOT_PATH + "implementation/SoftwareUpdatesImpl.java";

    @Test
    public void restoresProtocolDeleteFinalResultType() {
        Map<String, String> customized = new SoftwareUpdateCustomization().run(createGeneratedFiles(),
            LoggerFactory.getLogger(SoftwareUpdateCustomizationTest.class));

        MethodDeclaration syncClientMethod
            = findMethod(customized.get(ROOT_PATH + "SoftwareUpdateClient.java"), "beginDeleteUpdate", 4);
        MethodDeclaration asyncClientMethod
            = findMethod(customized.get(ROOT_PATH + "SoftwareUpdateAsyncClient.java"), "beginDeleteUpdate", 4);
        MethodDeclaration syncImplMethod = findMethod(customized.get(IMPL_PATH), "beginDeleteUpdate", 4);
        MethodDeclaration asyncImplMethod = findMethod(customized.get(IMPL_PATH), "beginDeleteUpdateAsync", 4);

        assertEquals("SyncPoller<BinaryData,BinaryData>", normalizedType(syncClientMethod));
        assertEquals("PollerFlux<BinaryData,BinaryData>", normalizedType(asyncClientMethod));
        assertEquals("SyncPoller<BinaryData,BinaryData>", normalizedType(syncImplMethod));
        assertEquals("PollerFlux<BinaryData,BinaryData>", normalizedType(asyncImplMethod));

        assertFinalResultTypeReferenceWasRewritten(syncImplMethod);
        assertFinalResultTypeReferenceWasRewritten(asyncImplMethod);
    }

    @Test
    public void leavesModelConvenienceOverloadUnchanged() {
        Map<String, String> customized = new SoftwareUpdateCustomization().run(createGeneratedFiles(),
            LoggerFactory.getLogger(SoftwareUpdateCustomizationTest.class));

        MethodDeclaration modelMethod
            = findMethod(customized.get(ROOT_PATH + "SoftwareUpdateClient.java"), "beginDeleteUpdate", 3);

        assertEquals("SyncPoller<UpdateOperation,Void>", normalizedType(modelMethod));
    }

    private static String normalizedType(MethodDeclaration method) {
        return method.getTypeAsString().replace(" ", "");
    }

    private static void assertFinalResultTypeReferenceWasRewritten(MethodDeclaration method) {
        String body = method.getBody().orElseThrow(IllegalStateException::new).toString();
        assertTrue(body.contains("TypeReference.createInstance(BinaryData.class)"));
        assertFalse(body.contains("TypeReference.createInstance(Void.class)"));
    }

    private static MethodDeclaration findMethod(String source, String methodName, int parameterCount) {
        return StaticJavaParser.parse(source)
            .findAll(MethodDeclaration.class)
            .stream()
            .filter(method -> method.getNameAsString().equals(methodName))
            .filter(method -> method.getParameters().size() == parameterCount)
            .findFirst()
            .orElseThrow(IllegalStateException::new);
    }

    private static Map<String, String> createGeneratedFiles() {
        Map<String, String> files = new HashMap<>();
        files.put(ROOT_PATH + "SoftwareUpdateClient.java", clientSource("SoftwareUpdateClient", "SyncPoller"));
        files.put(ROOT_PATH + "SoftwareUpdateAsyncClient.java",
            clientSource("SoftwareUpdateAsyncClient", "PollerFlux"));
        files.put(IMPL_PATH, implementationSource());
        return files;
    }

    private static String clientSource(String className, String pollerType) {
        return "package com.azure.iot.deviceregistry.softwareupdate;\n" + "public final class " + className + " {\n"
            + "    public " + pollerType + "<BinaryData, Void> beginDeleteUpdate("
            + "String provider, String name, String version, RequestOptions options) { return null; }\n" + "    public "
            + pollerType + "<UpdateOperation, Void> beginDeleteUpdate("
            + "String provider, String name, String version) { return null; }\n" + "}\n";
    }

    private static String implementationSource() {
        return "package com.azure.iot.deviceregistry.softwareupdate.implementation;\n"
            + "public final class SoftwareUpdatesImpl {\n"
            + protocolImplementationMethod("SyncPoller", "beginDeleteUpdate")
            + protocolImplementationMethod("PollerFlux", "beginDeleteUpdateAsync") + "}\n";
    }

    private static String protocolImplementationMethod(String pollerType, String methodName) {
        return "    public " + pollerType + "<BinaryData, Void> " + methodName + "(String provider, String name, "
            + "String version, RequestOptions options) {\n"
            + "        return create(TypeReference.createInstance(BinaryData.class), "
            + "TypeReference.createInstance(Void.class));\n" + "    }\n";
    }
}
