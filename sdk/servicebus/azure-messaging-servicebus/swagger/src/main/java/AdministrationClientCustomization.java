// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;

/**
 * Customization class for Administration client for Service Bus
 */
public class AdministrationClientCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Fix value serialization in KeyValue
        customization.getClass("com.azure.messaging.servicebus.administration.implementation.models", "KeyValue")
            .customizeAst(ast -> ast.getClassByName("KeyValue").ifPresent(clazz ->
                clazz.getMethodsByName("toXml").forEach(method -> method.getBody().ifPresent(body -> {
                    String bodyString = body.toString().replace(
                        "xmlWriter.writeStringElement(SCHEMAS_MICROSOFT_COM_SERVICEBUS_CONNECT, \"Value\", this.value);",
                        "if (this.value != null) {"
                            + "xmlWriter.writeStartElement(\"Value\");"
                            + "xmlWriter.writeNamespace(\"d6p1\", \"http://www.w3.org/2001/XMLSchema\");"
                            + "xmlWriter.writeStringAttribute(\"http://www.w3.org/2001/XMLSchema-instance\", \"type\", \"d6p1:string\");"
                            + "xmlWriter.writeString(this.value);"
                            + "xmlWriter.writeEndElement();"
                            + "}");
                    method.setBody(StaticJavaParser.parseBlock(bodyString));
                }))));

        // Change getCreatedTime modifier on NamespaceProperties.
        customization.getClass("com.azure.messaging.servicebus.administration.models", "NamespaceProperties")
            .customizeAst(ast -> ast.getClassByName("NamespaceProperties").ifPresent(clazz ->
                clazz.getMethodsByName("setCreatedTime").forEach(MethodDeclaration::setModifiers)));
    }
}
