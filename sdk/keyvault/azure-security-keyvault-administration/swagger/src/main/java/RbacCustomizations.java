// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Contains customizations for Azure KeyVault's RBAC swagger code generation.
 */
public class RbacCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization.getPackage("com.azure.security.keyvault.administration.models")
            .getClass("KeyVaultRoleScope")
            .customizeAst(ast -> {
                ast.addImport(IllegalArgumentException.class)
                    .addImport(URL.class)
                    .addImport(MalformedURLException.class);

                ClassOrInterfaceDeclaration clazz = ast.getClassByName("KeyVaultRoleScope").get();

                clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                    .setType("KeyVaultRoleScope")
                    .addParameter("String", "url")
                    .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                        "/**",
                        " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.",
                        " *",
                        " * @param url A string representing a URL containing the name of the scope to look for.",
                        " * @return The corresponding {@link KeyVaultRoleScope}.",
                        " * @throws IllegalArgumentException If the given {@code url} is malformed.",
                        " */"
                    )))
                    .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                        "{",
                        "try {",
                        "    return fromString(new URL(url).getPath());",
                        "} catch (MalformedURLException e) {",
                        "    throw new IllegalArgumentException(e);",
                        "}",
                        "}"
                    )));

                clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                    .setType("KeyVaultRoleScope")
                    .addParameter("URL", "url")
                    .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                        "/**",
                        " * Creates of finds a {@link KeyVaultRoleScope} from its string representation.",
                        " *",
                        " * @param url A URL containing the name of the scope to look for.",
                        " * @return The corresponding {@link KeyVaultRoleScope}.",
                        " */"
                    )))
                    .setBody(StaticJavaParser.parseBlock("{return fromString(url.getPath());}"));
            });
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
