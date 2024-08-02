// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.*;
import javax.xml.stream.events.Comment;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;

public class CommentEventImpl extends BaseEventImpl implements Comment {
    final String mContent;

    public CommentEventImpl(Location loc, String content) {
        super(loc);
        mContent = content;
    }

    @Override
    public String getText() {
        return mContent;
    }

    /*
    ///////////////////////////////////////////
    // Implementation of abstract base methods
    ///////////////////////////////////////////
     */

    @Override
    public int getEventType() {
        return COMMENT;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            w.write("<!--");
            w.write(mContent);
            w.write("-->");
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        w.writeComment(mContent);
    }

    /*
    ///////////////////////////////////////////
    // Standard method impl
    ///////////////////////////////////////////
     */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (!(o instanceof Comment))
            return false;

        Comment other = (Comment) o;
        return mContent.equals(other.getText());
    }

    @Override
    public int hashCode() {
        return mContent.hashCode();
    }
}
