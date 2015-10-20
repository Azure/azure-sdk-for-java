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

package com.microsoft.windowsazure.core.utils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public abstract class XmlUtility {
    public static Element getElementByTagNameNS(Node element, String namespace,
            String name) {
        NodeList elements = element.getChildNodes();
        CharSequence colon = ":";
        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                if (elements.item(i).getNodeType() == Node.ELEMENT_NODE
                    && (elements.item(i).getAttributes().getNamedItemNS("http://www.w3.org/2001/XMLSchema-instance", "nil") == null
                    || !"true".equals(elements.item(i).getAttributes().getNamedItemNS("http://www.w3.org/2001/XMLSchema-instance", "nil")))) {
                    Element currentElement = (Element) elements.item(i);
                    String nodeName = currentElement.getNodeName();
                    String nodeNameOnly = nodeName;
                    if (nodeName.contains(colon)) {
                        String[] nodeNameSplit = nodeName.split(":");
                        nodeNameOnly = nodeNameSplit[1];
                    }
                    
                    if ((currentElement.getNamespaceURI() == null
                        || currentElement.getNamespaceURI().equals(namespace))
                        && nodeNameOnly.equals(name)) {
                        return currentElement;
                    }
                }
            }
        }

        return null;
    }

    public static ArrayList<Element> getElementsByTagNameNS(Node element,
            String namespace, String name) {
        ArrayList<Element> childElements = new ArrayList<Element>();

        NodeList elements = element.getChildNodes();
        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                if (elements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element currentElement = (Element) elements.item(i);
                    if ((currentElement.getNamespaceURI() == null
                        || currentElement.getNamespaceURI().equals(namespace))
                        && currentElement.getNodeName().equals(name)) {

                        childElements.add(currentElement);
                    }
                }
            }
        }

        return childElements;
    }

    public static Element getElementByTagName(Node element, String name) {
        NodeList elements = element.getChildNodes();
        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                if (elements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element currentElement = (Element) elements.item(i);
                    if (currentElement.getNodeName().equals(name)) {

                        return currentElement;
                    }
                }
            }
        }

        return null;
    }
}
