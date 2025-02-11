// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure KeyVault's Keys swagger code generation.
 */
public class KeysCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        modelsCustomizations(libraryCustomization.getPackage("com.azure.security.keyvault.keys.models"));
    }

    private static void modelsCustomizations(PackageCustomization models) {
        models.getClass("KeyCurveName").customizeAst(ast -> ast.getClassByName("KeyCurveName").ifPresent(clazz -> {
            clazz.getFieldByName("P256K").ifPresent(field -> field.getVariable(0).setName("P_256K"));
            clazz.getFieldByName("P256").ifPresent(field -> field.getVariable(0).setName("P_256"));
            clazz.getFieldByName("P384").ifPresent(field -> field.getVariable(0).setName("P_384"));
            clazz.getFieldByName("P521").ifPresent(field -> field.getVariable(0).setName("P_521"));
        }));

        models.getClass("KeyType").customizeAst(ast -> ast.getClassByName("KeyType").ifPresent(clazz -> {
            clazz.getFieldByName("ECHSM").ifPresent(field -> field.getVariable(0).setName("EC_HSM"));
            clazz.getFieldByName("RSAHSM").ifPresent(field -> field.getVariable(0).setName("RSA_HSM"));
        }));

        models.getClass("KeyExportEncryptionAlgorithm").customizeAst(ast -> ast.getClassByName("KeyExportEncryptionAlgorithm").ifPresent(clazz -> {
            clazz.getFieldByName("CKMRSAAESKEYWRAP").ifPresent(field -> field.getVariable(0).setName("CKM_RSA_AES_KEY_WRAP"));
            clazz.getFieldByName("RSAAESKEYWRAP256").ifPresent(field -> field.getVariable(0).setName("RSA_AES_KEY_WRAP_256"));
            clazz.getFieldByName("RSAAESKEYWRAP384").ifPresent(field -> field.getVariable(0).setName("RSA_AES_KEY_WRAP_384"));
        }));
    }
}
