// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.*;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.EntityDeclaration;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;

public class EntityReferenceEventImpl extends BaseEventImpl implements EntityReference {
    protected final EntityDeclaration mDecl;

    public EntityReferenceEventImpl(Location loc, EntityDeclaration decl) {
        super(loc);
        mDecl = decl;
    }

    public EntityReferenceEventImpl(Location loc, String name) {
        super(loc);
        // note: location will be incorrect...
        mDecl = new EntityDeclarationEventImpl(loc, name);
    }

    @Override
    public EntityDeclaration getDeclaration() {
        return mDecl;
    }

    @Override
    public String getName() {
        return mDecl.getName();
    }

    /*
    /**********************************************************************
    /* Implementation of abstract base methods
    /**********************************************************************
     */

    @Override
    public int getEventType() {
        return ENTITY_REFERENCE;
    }

    @Override
    public boolean isEntityReference() {
        return true;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            w.write('&');
            w.write(getName());
            w.write(';');
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        w.writeEntityRef(getName());
    }

    /*
    /**********************************************************************
    /* Standard method impl
    /**********************************************************************
     */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;

        if (!(o instanceof EntityReference))
            return false;

        EntityReference other = (EntityReference) o;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
