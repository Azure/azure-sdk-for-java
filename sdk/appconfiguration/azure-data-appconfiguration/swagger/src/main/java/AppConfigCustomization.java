// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for TextAnalytics.
 */
public class AppConfigCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.data.appconfiguration.implementation.models");
        ClassCustomization keyValueFields = models.getClass("KeyValueFields");
        // Rename LOCKED to IS_READ_ONLY
        keyValueFields.renameEnumMember("LOCKED", "IS_READ_ONLY");
        // Change class name to SettingFields
        final ClassCustomization settingFields = keyValueFields.rename("SettingFields");
        // Add toStringMapper static new method to SettingFields
        final ClassCustomization classCustomization = addToStringMapper(settingFields);
    }

    private ClassCustomization addToStringMapper(ClassCustomization classCustomization) {
        classCustomization.addImports("java.util.Locale;");
        return classCustomization.customizeAst(ast -> {
            String className = classCustomization.getClassName();
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(className).get();

            clazz.addMethod("toStringMapper", Modifier.Keyword.STATIC, Modifier.Keyword.PUBLIC).setType("String")
                .addParameter("SettingFields", "field")
                .setBody(new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement("return field.toString().toLowerCase(Locale.US);"))))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    " * Converts the SettingFields to a string that is usable for HTTP requests and logging.",
                    " * @param field SettingFields to map.",
                    " * @return SettingFields as a lowercase string in the US locale."
                )));
        });
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
