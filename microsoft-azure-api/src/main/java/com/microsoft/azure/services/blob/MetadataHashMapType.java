package com.microsoft.azure.services.blob;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;

import org.w3c.dom.Element;

public class MetadataHashMapType {
    @XmlAnyElement
    public List<Element> entries = new ArrayList<Element>();
}
