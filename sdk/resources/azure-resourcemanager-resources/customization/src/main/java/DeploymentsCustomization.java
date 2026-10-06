// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.Modifier;
import org.slf4j.Logger;

/**
 * Code customization after deployments code generation.
 */
public class DeploymentsCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeResourceReference(
            customization.getPackage("com.azure.resourcemanager.resources.models").getClass("ResourceReference"));
    }

    private static void customizeResourceReference(ClassCustomization customization) {
        customization.customizeAst(ast -> ast.getClassByName(customization.getClassName()).ifPresent(clazz -> {
            clazz.removeModifier(Modifier.Keyword.FINAL);
            clazz.getConstructors()
                .stream()
                .filter(constructor -> constructor.getParameters().isEmpty())
                .forEach(constructor -> constructor.setModifiers(Modifier.Keyword.PROTECTED));
        }));
    }
}
