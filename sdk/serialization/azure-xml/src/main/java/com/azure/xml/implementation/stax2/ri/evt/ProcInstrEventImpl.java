// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.*;
import javax.xml.stream.events.ProcessingInstruction;

import com.azure.xml.implementation.stax2.*;

public class ProcInstrEventImpl extends BaseEventImpl implements ProcessingInstruction {
    final String mTarget;
    final String mData;

    public ProcInstrEventImpl(Location loc, String target, String data) {
        super(loc);
        mTarget = target;
        mData = data;
    }

    @Override
    public String getData() {
        return mData;
    }

    @Override
    public String getTarget() {
        return mTarget;
    }

    /*
    ///////////////////////////////////////////
    // Implementation of abstract base methods
    ///////////////////////////////////////////
     */

    @Override
    public int getEventType() {
        return PROCESSING_INSTRUCTION;
    }

    @Override
    public boolean isProcessingInstruction() {
        return true;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            w.write("<?");
            w.write(mTarget);
            if (mData != null && !mData.isEmpty()) {
                w.write(mData);
            }
            w.write("?>");
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        if (mData != null && !mData.isEmpty()) {
            w.writeProcessingInstruction(mTarget, mData);
        } else {
            w.writeProcessingInstruction(mTarget);
        }
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

        if (!(o instanceof ProcessingInstruction))
            return false;

        ProcessingInstruction other = (ProcessingInstruction) o;
        return mTarget.equals(other.getTarget()) && stringsWithNullsEqual(mData, other.getData());
    }

    @Override
    public int hashCode() {
        int hash = mTarget.hashCode();
        if (mData != null) {
            hash ^= mData.hashCode();
        }
        return hash;
    }
}
