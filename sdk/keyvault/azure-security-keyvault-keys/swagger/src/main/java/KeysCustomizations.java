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
        ClassCustomization keyCurveName = models.getClass("KeyCurveName");
        keyCurveName.renameEnumMember("P256K", "P_256K");
        keyCurveName.renameEnumMember("P256", "P_256");
        keyCurveName.renameEnumMember("P384", "P_384");
        keyCurveName.renameEnumMember("P521", "P_521");

        ClassCustomization keyType = models.getClass("KeyType");
        keyType.renameEnumMember("ECHSM", "EC_HSM");
        keyType.renameEnumMember("RSAHSM", "RSA_HSM");

        ClassCustomization keyExportEncryptionAlgorithm = models.getClass("KeyExportEncryptionAlgorithm");
        keyExportEncryptionAlgorithm.renameEnumMember("CKMRSAAESKEYWRAP", "CKM_RSA_AES_KEY_WRAP");
        keyExportEncryptionAlgorithm.renameEnumMember("RSAAESKEYWRAP256", "RSA_AES_KEY_WRAP_256");
        keyExportEncryptionAlgorithm.renameEnumMember("RSAAESKEYWRAP384", "RSA_AES_KEY_WRAP_384");
    }
}
