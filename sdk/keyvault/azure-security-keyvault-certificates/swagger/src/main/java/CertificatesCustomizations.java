// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class CertificatesCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization.getPackage("com.azure.security.keyvault.certificates.models")
            .getClass("CertificateKeyCurveName")
            .customizeAst(ast -> ast.getClassByName("CertificateKeyCurveName").ifPresent(clazz -> {
                clazz.getFieldByName("P256K").ifPresent(field -> field.getVariable(0).setName("P_256K"));
                clazz.getFieldByName("P256").ifPresent(field -> field.getVariable(0).setName("P_256"));
                clazz.getFieldByName("P384").ifPresent(field -> field.getVariable(0).setName("P_384"));
                clazz.getFieldByName("P521").ifPresent(field -> field.getVariable(0).setName("P_521"));
            }));
    }
}
