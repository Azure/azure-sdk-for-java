// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Contains customizations for call automation configuration.
 */
public class CallautomationCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeCallAutomationEnumMembers(customization.getPackage("com.azure.communication.callautomation.implementation.models"));
    }

    private void customizeCallAutomationEnumMembers(PackageCustomization customization) {
        customization.getClass("RecordingStorageTypeInternal").customizeAst(ast -> ast.getClassByName(
                "RecordingStorageTypeInternal")
            .flatMap(clazz -> clazz.getFieldByName("AZURE_COMMUNICATION_SERVICES"))
            .ifPresent(field -> field.getVariable(0).setName("ACS")));

        // As per azure board review renaming the PCM16KMONO to PCM_16K_MONO,PCM24KMONO to PCM_24K_MONO
        customization.getClass("AudioFormatInternal").customizeAst(ast -> ast.getClassByName("AudioFormatInternal")
            .ifPresent(clazz -> {
                clazz.getFieldByName("PCM16KMONO").ifPresent(field -> field.getVariable(0).setName("PCM_16K_MONO"));
                clazz.getFieldByName("PCM24KMONO").ifPresent(field -> field.getVariable(0).setName("PCM_24K_MONO"));
            }));

        customization.getClass("MediaStreamingStatusDetails").customizeAst(ast -> ast.getClassByName(
            "MediaStreamingStatusDetails")
                .flatMap(clazz -> clazz.getFieldByName("INITIAL_WEB_SOCKET_CONNECTION_FAILED"))
                .ifPresent(field -> field.getVariable(0).setName("INITIAL_WEBSOCKET_CONNECTION_FAILED")));
    }
}