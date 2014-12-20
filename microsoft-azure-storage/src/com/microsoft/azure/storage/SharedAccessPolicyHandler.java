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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to deserialize SharedAccessPolicies.
 */
public class SharedAccessPolicyHandler<T extends SharedAccessPolicy> extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final HashMap<String, T> policies = new HashMap<String, T>();
    private final Class<T> policyClassType;

    private String id;
    private T policy;

    private SharedAccessPolicyHandler(final Class<T> cls) {
        this.policyClassType = cls;
    }

    /**
     * RESERVED FOR INTERNAL USE. Gets the HashMap of SharedAccessPolicies from the response.
     * 
     * @param stream
     *            the stream to read from
     * @param cls
     *            the <code>SharedAccessPolicy</code> class type
     * @return the HashMap of SharedAccessPolicies from the response
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     * @throws IOException
     */
    public static <T extends SharedAccessPolicy> HashMap<String, T> getAccessIdentifiers(final InputStream stream,
            final Class<T> cls) throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        SharedAccessPolicyHandler<T> handler = new SharedAccessPolicyHandler<T>(cls);
        saxParser.parse(stream, handler);

        return handler.policies;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (Constants.SIGNED_IDENTIFIER_ELEMENT.equals(localName)) {
            this.id = null;

            try {
                this.policy = this.policyClassType.newInstance();
            }
            catch (Exception e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentNode = this.elementStack.pop();

        // if the node popped from the stack and the localName don't match, the xml document is improperly formatted
        if (!localName.equals(currentNode)) {
            throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
        }

        String value = this.bld.toString();
        if (value.isEmpty()) {
            value = null;
        }

        if (Constants.SIGNED_IDENTIFIER_ELEMENT.equals(currentNode)) {
            this.policies.put(this.id, this.policy);
        }
        else if (Constants.ID.equals(currentNode)) {
            this.id = value;
        }
        else if (Constants.START.equals(currentNode)) {
            try {
                this.policy.setSharedAccessStartTime(Utility.parseDate(value));
            }
            catch (IllegalArgumentException e) {
                throw new SAXException(e);
            }
        }
        else if (Constants.EXPIRY.equals(currentNode)) {
            try {
                this.policy.setSharedAccessExpiryTime(Utility.parseDate(value));
            }
            catch (IllegalArgumentException e) {
                throw new SAXException(e);
            }
        }
        else if (Constants.PERMISSION.equals(currentNode)) {
            this.policy.setPermissionsFromString(value);
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }
}
