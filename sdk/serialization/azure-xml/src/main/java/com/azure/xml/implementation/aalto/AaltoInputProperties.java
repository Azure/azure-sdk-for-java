// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto;

/**
 * Class that contains constant for property names used to configure
 * cursor and event readers produced by Aalto implementation of
 * {@link javax.xml.stream.XMLInputFactory}.
 *
 * @since 1.3
 */
public final class AaltoInputProperties {
    /**
     * Feature controlling whether general entities in attributes are retained
     * as-is without processing ({@code true}) or replaced as per standard
     * XML processing rules ({@code false}).
     * If enabled, instead of regular General Entity expansion, possible general
     * entities in Attribute values will be left exactly as-is, with no processing;
     * as such they cannot be distinguished from regular textual content.
     *<p>
     * The main reason for enabling this non-standard property is to avoid errors
     * in cases where content contains general entity references in attribute values,
     * but no processing is allowed (for example, for security reasons).
     *<p>
     * Property defaults to {@code false} for XML standard compliancy but may
     * be enabled to avoid processing errors (but note that caller will necessarily
     * lose information as unexpanded entity cannot be distinguished from regular
     * attribute textual content).
     */
    public final static String P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES
        = "com.azure.xml.implementation.aalto.retainAttributeGeneralEntities";
}
