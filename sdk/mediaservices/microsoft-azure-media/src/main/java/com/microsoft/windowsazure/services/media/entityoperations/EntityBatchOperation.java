/*
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

package com.microsoft.windowsazure.services.media.entityoperations;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.LinkType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;

public class EntityBatchOperation {

    private String verb;
    private EntryType entryType;

    public EntityBatchOperation() {
        this.entryType = new EntryType();
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public EntityBatchOperation setEntityType(EntryType entryType) {
        this.entryType = entryType;
        return this;
    }

    protected EntityBatchOperation setVerb(String verb) {
        this.verb = verb;
        return this;
    }

    public String getVerb() {
        return this.verb;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EntityBatchOperation addContentObject(Object contentObject) {
        ContentType atomContent = new ContentType();
        atomContent.setType("application/xml");
        atomContent
                .getContent()
                .add(new JAXBElement(new QName(Constants.ODATA_METADATA_NS,
                        "properties"), contentObject.getClass(), contentObject));

        this.entryType.getEntryChildren().add(
                new JAXBElement(new QName(Constants.ATOM_NS, "content"),
                        ContentType.class, atomContent));
        return this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected EntityBatchOperation addLink(String title, String href,
            String type, String rel) {
        LinkType linkType = new LinkType();
        linkType.setTitle(title);
        linkType.setHref(href);
        linkType.setType(type);
        linkType.setRel(rel);
        this.entryType.getEntryChildren().add(
                new JAXBElement(new QName(Constants.ATOM_NS, "link"),
                        LinkType.class, linkType));
        return this;

    }

}
