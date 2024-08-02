// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* StAX2 extension for StAX API (JSR-173).
 *
 * Copyright (c) 2005- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.stax2.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Similar to {@link javax.xml.stream.util.StreamReaderDelegate},
 * but implements a proxy for {@link XMLStreamWriter}.
 * The only additional methods are ones for setting and accessing
 * the delegate to forward requests to.
 *<p>
 * Note: such class really should exist in core Stax API
 * (in package <code>javax.xml.stream.util</code>), but since
 * it does not, it is implemented within Stax2 extension API
 *
 * @since 3.0
 */
public class StreamWriterDelegate implements XMLStreamWriter {
    protected XMLStreamWriter mDelegate;

    /*
    //////////////////////////////////////////////
    // Life-cycle
    //////////////////////////////////////////////
     */

    public StreamWriterDelegate(XMLStreamWriter parentWriter) {
        mDelegate = parentWriter;
    }

    public void setParent(XMLStreamWriter parentWriter) {
        mDelegate = parentWriter;
    }

    public XMLStreamWriter getParent() {
        return mDelegate;
    }

    /*
    //////////////////////////////////////////////
    // XMLStreamWriter implementation
    //////////////////////////////////////////////
     */

    @Override
    public void close() throws XMLStreamException {
        mDelegate.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        mDelegate.flush();

    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return mDelegate.getNamespaceContext();
    }

    @Override
    public String getPrefix(String ns) throws XMLStreamException {
        return mDelegate.getPrefix(ns);
    }

    @Override
    public Object getProperty(String pname) throws IllegalArgumentException {
        return mDelegate.getProperty(pname);
    }

    @Override
    public void setDefaultNamespace(String ns) throws XMLStreamException {
        mDelegate.setDefaultNamespace(ns);

    }

    @Override
    public void setNamespaceContext(NamespaceContext nc) throws XMLStreamException {
        mDelegate.setNamespaceContext(nc);

    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        mDelegate.setPrefix(prefix, uri);
    }

    @Override
    public void writeAttribute(String arg0, String arg1) throws XMLStreamException {
        mDelegate.writeAttribute(arg0, arg1);
    }

    @Override
    public void writeAttribute(String arg0, String arg1, String arg2) throws XMLStreamException {
        mDelegate.writeAttribute(arg0, arg1, arg2);
    }

    @Override
    public void writeAttribute(String arg0, String arg1, String arg2, String arg3) throws XMLStreamException {
        mDelegate.writeAttribute(arg0, arg1, arg2, arg3);
    }

    @Override
    public void writeCData(String arg0) throws XMLStreamException {
        mDelegate.writeCData(arg0);
    }

    @Override
    public void writeCharacters(String arg0) throws XMLStreamException {
        mDelegate.writeCharacters(arg0);
    }

    @Override
    public void writeCharacters(char[] arg0, int arg1, int arg2) throws XMLStreamException {
        mDelegate.writeCharacters(arg0, arg1, arg2);
    }

    @Override
    public void writeComment(String arg0) throws XMLStreamException {
        mDelegate.writeComment(arg0);

    }

    @Override
    public void writeDTD(String arg0) throws XMLStreamException {
        mDelegate.writeDTD(arg0);
    }

    @Override
    public void writeDefaultNamespace(String arg0) throws XMLStreamException {
        mDelegate.writeDefaultNamespace(arg0);
    }

    @Override
    public void writeEmptyElement(String arg0) throws XMLStreamException {
        mDelegate.writeEmptyElement(arg0);
    }

    @Override
    public void writeEmptyElement(String arg0, String arg1) throws XMLStreamException {
        mDelegate.writeEmptyElement(arg0, arg1);
    }

    @Override
    public void writeEmptyElement(String arg0, String arg1, String arg2) throws XMLStreamException {
        mDelegate.writeEmptyElement(arg0, arg1, arg2);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        mDelegate.writeEndDocument();

    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        mDelegate.writeEndElement();

    }

    @Override
    public void writeEntityRef(String arg0) throws XMLStreamException {
        mDelegate.writeEntityRef(arg0);

    }

    @Override
    public void writeNamespace(String arg0, String arg1) throws XMLStreamException {
        mDelegate.writeNamespace(arg0, arg1);

    }

    @Override
    public void writeProcessingInstruction(String arg0) throws XMLStreamException {
        mDelegate.writeProcessingInstruction(arg0);

    }

    @Override
    public void writeProcessingInstruction(String arg0, String arg1) throws XMLStreamException {
        mDelegate.writeProcessingInstruction(arg0, arg1);

    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        mDelegate.writeStartDocument();

    }

    @Override
    public void writeStartDocument(String arg0) throws XMLStreamException {
        mDelegate.writeStartDocument(arg0);

    }

    @Override
    public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
        mDelegate.writeStartDocument(arg0, arg1);

    }

    @Override
    public void writeStartElement(String arg0) throws XMLStreamException {
        mDelegate.writeStartElement(arg0);

    }

    @Override
    public void writeStartElement(String arg0, String arg1) throws XMLStreamException {
        mDelegate.writeStartElement(arg0, arg1);
    }

    @Override
    public void writeStartElement(String arg0, String arg1, String arg2) throws XMLStreamException {
        mDelegate.writeStartElement(arg0, arg1, arg2);
    }
}
