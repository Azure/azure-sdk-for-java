// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.ContentType;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.xml.sax.InputSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

/**
 * This class contains utility methods useful for internal use.
 */
public class ImplUtil {

    /**
     * Pretty print the json or xml string content. If the content is not in a format of json or xml,
     * return original string.
     *
     * @param content The body content which need to parse.
     * @param contentType The format of the contents.
     * @return Pretty json or xml format of the content. If it is not in a format of json or xml, returns original one.
     */
    public static String printPrettyFormatJsonOrXml(String content, String contentType) {
        if (content == null || contentType == null) {
            return content;
        }

        ClientLogger logger = new ClientLogger(ImplUtil.class);
        if (contentType.startsWith(ContentType.APPLICATION_JSON)) {
            try {
                ObjectMapper prettyPrinter = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                final Object deserialized = prettyPrinter.readTree(content);
                return prettyPrinter.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.warning("Failed to pretty print JSON: {}", e.getMessage());
            }
        } else if (contentType.startsWith(ContentType.APPLICATION_XML)) {
            try {
                Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(
                    content.getBytes(StandardCharsets.UTF_8))));
                StreamResult res =  new StreamResult(new ByteArrayOutputStream());
                serializer.transform(xmlSource, res);
                return new String(((ByteArrayOutputStream) res.getOutputStream()).toByteArray(),
                    StandardCharsets.UTF_8);
            } catch (TransformerException e) {
                logger.warning("Failed to pretty print XML: {}", e.getMessage());
            }
        }
        return content;
    }
}
