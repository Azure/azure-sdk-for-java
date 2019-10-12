// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.time.Duration;

// Fields that require special serializations
public class SerializerUtil {
    private static final String XML_FEATURE_DISALLOW_DOC_TYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String XML_FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    private static final String XML_FEATURE_EXTERNAL_PARAM_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    private static final String XML_FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public static DocumentBuilderFactory getDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // Disallow DTDs (doctypes) to prevent most XML attacks.
        dbf.setFeature(XML_FEATURE_DISALLOW_DOC_TYPE, true);
        dbf.setFeature(XML_FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
        dbf.setFeature(XML_FEATURE_EXTERNAL_PARAM_ENTITIES, false);
        dbf.setFeature(XML_FEATURE_LOAD_EXTERNAL_DTD, false);
        // as per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        return dbf;
    }

    public static String serializeDuration(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return "";
        }
        Duration remainingTime = duration;
        StringBuffer sb = new StringBuffer("P");
        long days = remainingTime.toDays();
        if (days > 0) {
            sb.append(days).append("D");
            remainingTime = duration.minusDays(days);
        }
        if (!remainingTime.isZero()) {
            sb.append("T");
            long hours = remainingTime.toHours();
            if (hours > 0) {
                sb.append(hours).append("H");
                remainingTime = duration.minusHours(hours);
            }

            long minutes = remainingTime.toMinutes();
            if (minutes > 0) {
                sb.append(minutes).append("M");
                remainingTime = duration.minusMinutes(minutes);
            }

            long seconds = remainingTime.getSeconds();
            if (seconds > 0) {
                sb.append(seconds).append("S");
            }
        }

        return sb.toString();
    }
}
