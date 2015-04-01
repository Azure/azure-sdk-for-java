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
package com.microsoft.azure.storage;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to serialize SharedAccessPolicies to a byte array.
 */
public final class SharedAccessPolicySerializer {
    /**
     * RESERVED FOR INTERNAL USE. Writes a collection of shared access policies to the specified stream in XML format.
     * 
     * @param <T>
     * 
     * @param sharedAccessPolicies
     *            A collection of shared access policies
     * @param outWriter
     *            an sink to write the output to.
     * @throws XMLStreamException
     */
    public static <T extends SharedAccessPolicy> void writeSharedAccessIdentifiersToStream(
            final HashMap<String, T> sharedAccessPolicies, final StringWriter outWriter) throws XMLStreamException {
        Utility.assertNotNull("sharedAccessPolicies", sharedAccessPolicies);
        Utility.assertNotNull("outWriter", outWriter);

        final XMLStreamWriter xmlw = Utility.createXMLStreamWriter(outWriter);

        if (sharedAccessPolicies.keySet().size() > Constants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS) {
            final String errorMessage = String.format(SR.TOO_MANY_SHARED_ACCESS_POLICY_IDENTIFIERS,
                    sharedAccessPolicies.keySet().size(), Constants.MAX_SHARED_ACCESS_POLICY_IDENTIFIERS);

            throw new IllegalArgumentException(errorMessage);
        }

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(Constants.SIGNED_IDENTIFIERS_ELEMENT);

        for (final Entry<String, T> entry : sharedAccessPolicies.entrySet()) {
            final SharedAccessPolicy policy = entry.getValue();
            xmlw.writeStartElement(Constants.SIGNED_IDENTIFIER_ELEMENT);

            // Set the identifier
            xmlw.writeStartElement(Constants.ID);
            xmlw.writeCharacters(entry.getKey());
            xmlw.writeEndElement();

            xmlw.writeStartElement(Constants.ACCESS_POLICY);

            // Set the Start Time
            xmlw.writeStartElement(Constants.START);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessStartTime()));
            // end Start
            xmlw.writeEndElement();

            // Set the Expiry Time
            xmlw.writeStartElement(Constants.EXPIRY);
            xmlw.writeCharacters(Utility.getUTCTimeOrEmpty(policy.getSharedAccessExpiryTime()));
            // end Expiry
            xmlw.writeEndElement();

            // Set the Permissions
            xmlw.writeStartElement(Constants.PERMISSION);
            xmlw.writeCharacters(policy.permissionsToString());
            // end Permission
            xmlw.writeEndElement();

            // end AccessPolicy
            xmlw.writeEndElement();
            // end SignedIdentifier
            xmlw.writeEndElement();
        }

        // end SignedIdentifiers
        xmlw.writeEndElement();
        // end doc
        xmlw.writeEndDocument();
    }
}
