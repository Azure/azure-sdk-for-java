/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.storage.core;

import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. An internal helper class used to parse objects from responses.
 */
public final class DeserializationHelper {
    /**
     * Reads Metadata from the XMLStreamReader.
     * 
     * @param xmlr
     *            the XMLStreamReader object
     * @return the metadata as a hashmap.
     * @throws XMLStreamException
     *             if there is a parsing exception
     */
    public static HashMap<String, String> parseMetadateFromXML(final XMLStreamReader xmlr) throws XMLStreamException {
        final HashMap<String, String> retVals = new HashMap<String, String>();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.METADATA_ELEMENT);

        int eventType = xmlr.getEventType();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.END_ELEMENT && Constants.METADATA_ELEMENT.equals(name)) {
                break;
            }
            else if (Constants.INVALID_METADATA_NAME.equals(name)) {
                // no op , skip
            }
            else if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String tValue = Utility.readElementFromXMLReader(xmlr, name);
                if (!Utility.isNullOrEmpty(tValue)) {
                    retVals.put(name, tValue);
                }
            }
        }

        return retVals;
    }

    /**
     * Private Default Ctor
     */
    private DeserializationHelper() {
        // No op
    }
}
