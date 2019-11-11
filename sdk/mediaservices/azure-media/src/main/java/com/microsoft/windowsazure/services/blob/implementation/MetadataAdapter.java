/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.services.blob.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.w3c.dom.Element;

/*
 * JAXB adapter for <Metadata> element
 */
public class MetadataAdapter
        extends
        XmlAdapter<MetadataAdapter.MetadataHashMapType, HashMap<String, String>> {

    @Override
    public HashMap<String, String> unmarshal(MetadataHashMapType arg0)
            throws Exception {
        HashMap<String, String> result = new HashMap<String, String>();
        for (Element entry : arg0.getEntries()) {
            result.put(entry.getLocalName(), entry.getFirstChild()
                    .getNodeValue());
        }
        return result;
    }

    @Override
    public MetadataHashMapType marshal(HashMap<String, String> arg0)
            throws Exception {
        // We don't need marshaling for blob/container metadata
        throw new OperationNotSupportedException();
    }

    public static class MetadataHashMapType {
        private List<Element> entries = new ArrayList<Element>();

        @XmlAnyElement
        public List<Element> getEntries() {
            return entries;
        }

        public void setEntries(List<Element> entries) {
            this.entries = entries;
        }
    }
}
