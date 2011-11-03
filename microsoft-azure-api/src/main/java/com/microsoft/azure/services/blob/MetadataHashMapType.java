package com.microsoft.azure.services.blob;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;

import org.w3c.dom.Element;

public class MetadataHashMapType {
    private List<Element> entries = new ArrayList<Element>();

    @XmlAnyElement
    public List<Element> getEntries() {
        return entries;
    }

    public void setEntries(List<Element> entries) {
        this.entries = entries;
    }
}
