package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.Map;

import static com.azure.cosmos.implementation.directconnectivity.WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY;

public class RntbdTokenStreamTests {
    // Created this test for thin client testing
    @Test(groups = { "unit" })
    public void testThinClientSpecialCasing() {
        RntbdContextRequest.Headers headers = new RntbdContextRequest.Headers(Unpooled.EMPTY_BUFFER);

        RntbdTokenStream<RntbdConstants.RntbdRequestHeader> rntbdTokenStream =
            new TestRntbdTokenStream(
                RntbdConstants.RntbdRequestHeader.set,
                RntbdConstants.RntbdRequestHeader.map,
                Unpooled.EMPTY_BUFFER,
                RntbdConstants.RntbdRequestHeader.class);

        final ByteBuf out = Unpooled.buffer();
        headers.encode(out);
    }

    final class TestRntbdTokenStream extends RntbdTokenStream<RntbdConstants.RntbdRequestHeader> {
        TestRntbdTokenStream(EnumSet<RntbdConstants.RntbdRequestHeader> headers, Map<Short, RntbdConstants.RntbdRequestHeader> ids, ByteBuf in, Class<RntbdConstants.RntbdRequestHeader> classType) {
            super(headers, ids, in, classType);
        }
    }
}
