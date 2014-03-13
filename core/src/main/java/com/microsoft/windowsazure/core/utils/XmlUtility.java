package com.microsoft.windowsazure.core.utils;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtility {
    public static Element getElementByTagNameNS(Node element, String namespace, String name) {
        NodeList elements = element.getChildNodes();
        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                if (elements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element currentElement = (Element) elements.item(i);
                    if (currentElement.getNamespaceURI().equals(namespace) &&
                        currentElement.getNodeName().equals(name)) {

                        return currentElement;
                    }
                }
            }
        }

        return null;
    }

    public static ArrayList<Element> getElementsByTagNameNS(Node element, String namespace, String name) {
        ArrayList<Element> childElements = new ArrayList<Element>();

        NodeList elements = element.getChildNodes();
        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                if (elements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element currentElement = (Element) elements.item(i);
                    if (currentElement.getNamespaceURI().equals(namespace) &&
                        currentElement.getNodeName().equals(name)) {

                        childElements.add(currentElement);
                    }
                }
            }
        }

        return childElements;
    }

    public static Element getElementByTagName(Node element, String name) {
        NodeList elements = element.getChildNodes();
        for (int i = 0; i < elements.getLength(); i++) {
            Element currentElement = (Element) elements.item(i);
            if (currentElement.getNodeName().equals(name)) {

                return currentElement;
            }
        }

        return null;
    }
}
