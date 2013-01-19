package com.microsoft.windowsazure.services.table.client;

import com.microsoft.windowsazure.services.core.storage.OperationContext;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLStreamWriter;
import java.util.Date;

import static com.microsoft.windowsazure.services.table.client.AtomPubParser.writeEntityToStream;
import static org.mockito.Mockito.*;

public class AtomPubParserTests {

    public static final String WHITESPACE = " ";
    public static final String EMPTYSTRING = "";
    private TableEntity entity;
    private XMLStreamWriter writer;
    private OperationContext context;

    @Before
    public void setUp() {
        entity = mock(TableEntity.class);
        writer = mock(XMLStreamWriter.class);
        context = new OperationContext();
    }

    @Test
    public void writeEntityShouldAcceptWhiteSpaces() throws Exception {
        when(entity.getPartitionKey()).thenReturn(WHITESPACE);
        when(entity.getRowKey()).thenReturn(WHITESPACE);
        when(entity.getTimestamp()).thenReturn(new Date());

        writeEntityToStream(entity, false, writer, context);

        verify(entity, atLeastOnce()).getPartitionKey();
        verify(entity, atLeastOnce()).getRowKey();
    }

    @Test
    public void writeEntityShouldAcceptEmpytString() throws Exception {
        when(entity.getPartitionKey()).thenReturn(EMPTYSTRING);
        when(entity.getRowKey()).thenReturn(EMPTYSTRING);
        when(entity.getTimestamp()).thenReturn(new Date());

        writeEntityToStream(entity, false, writer, context);

        verify(entity, atLeastOnce()).getPartitionKey();
        verify(entity, atLeastOnce()).getRowKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeEntityShouldNotAcceptEmptyStrings() throws Exception {
        when(entity.getPartitionKey()).thenReturn(EMPTYSTRING);
        when(entity.getRowKey()).thenReturn(EMPTYSTRING);
        when(entity.getTimestamp()).thenReturn(new Date());

        writeEntityToStream(entity, false, writer, context);
    }

}
