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
package com.microsoft.windowsazure.services.table.client;

import java.io.InputStream;
import java.text.ParseException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.core.storage.AccessPolicyResponseBase;
import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse SharedAccessPolicies from an input stream.
 */
final class TableAccessPolicyResponse extends AccessPolicyResponseBase<SharedAccessTablePolicy> {

    /**
     * Initializes the AccessPolicyResponse object
     * 
     * @param stream
     *            the input stream to read error details from.
     */
    public TableAccessPolicyResponse(final InputStream stream) {
        super(stream);
    }

    /**
     * Populates the object from the XMLStreamReader, reader must be at Start element of AccessPolicy.
     * 
     * @param xmlr
     *            the XMLStreamReader object
     * @throws XMLStreamException
     *             if there is a parsing exception
     * @throws ParseException
     *             if a date value is not correctly encoded
     */
    @Override
    protected SharedAccessTablePolicy readPolicyFromXML(final XMLStreamReader xmlr) throws XMLStreamException,
            ParseException {
        int eventType = xmlr.getEventType();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.ACCESS_POLICY);
        final SharedAccessTablePolicy retPolicy = new SharedAccessTablePolicy();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.PERMISSION)) {
                    retPolicy.setPermissions(SharedAccessTablePolicy.permissionsFromString(Utility
                            .readElementFromXMLReader(xmlr, Constants.PERMISSION)));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.START)) {
                    final String tempString = Utility.readElementFromXMLReader(xmlr, Constants.START);
                    retPolicy.setSharedAccessStartTime(Utility.parseISO8061LongDateFromString(tempString));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.EXPIRY)) {
                    final String tempString = Utility.readElementFromXMLReader(xmlr, Constants.EXPIRY);
                    retPolicy.setSharedAccessExpiryTime(Utility.parseISO8061LongDateFromString(tempString));
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(Constants.ACCESS_POLICY)) {
                    break;
                }
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.ACCESS_POLICY);
        return retPolicy;
    }
}
