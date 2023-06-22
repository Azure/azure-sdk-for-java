// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

/**
 * Test utilities.
 */
public final class AzureXmlTestUtils {
    /**
     * Gets the XML root element name that should be used.
     *
     * @param rootElementName The XML root element name passed to {@link XmlSerializable#toXml(XmlWriter, String)}
     * or {@link XmlSerializable#fromXml(XmlReader, String)} implementations.
     * @param defaultRootElementName The XML root element name defined by Swagger to TypeSpec.
     * @return The root element name that should be used. {@code rootElementName} is used if it isn't null or empty,
     * otherwise {@code defaultRootElementName} is used.
     */
    public static String getRootElementName(String rootElementName, String defaultRootElementName) {
        return (rootElementName == null || rootElementName.isEmpty()) ? defaultRootElementName : rootElementName;
    }

    private AzureXmlTestUtils() {
    }
}
