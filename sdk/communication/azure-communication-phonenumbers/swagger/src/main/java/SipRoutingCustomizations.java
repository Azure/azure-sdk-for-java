// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import org.slf4j.Logger;

/**
 * Contains customizations for SIP routing configuration.
 */
public class SipRoutingCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.communication.phonenumbers.siprouting.implementation.models", "SipConfiguration")
            .customizeAst(ast -> ast.getClassByName("SipConfiguration").ifPresent(clazz ->
                clazz.getMethodsByName("toJson").forEach(method -> method.setBody(StaticJavaParser.parseBlock("{"
                    + "jsonWriter.writeStartObject();"
                    + "jsonWriter.writeMapField(\"trunks\", this.trunks, JsonWriter::writeJson, false);"
                    + "jsonWriter.writeArrayField(\"routes\", this.routes, JsonWriter::writeJson, false);"
                    + "return jsonWriter.writeEndObject(); }")))));
    }
}
