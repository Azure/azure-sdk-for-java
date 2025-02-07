package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.directconnectivity.WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY;

public class RntbdTokenStreamTests {
    // Created this test for thin client testing, to make sure thin client headers are always special cased
    @Test(groups = { "unit" })
    public void testThinClientSpecialCasing() {
        RxDocumentServiceRequest mockRequest = Mockito.mock(RxDocumentServiceRequest.class);
        Map<String, String> headers = new HashMap<>();
        headers.put(EFFECTIVE_PARTITION_KEY, "13A141365AE34002732EE6DD02677CFC");
        headers.put(HttpConstants.HttpHeaders.GLOBAL_DATABASE_ACCOUNT_NAME, "globalDatabaseAccountName");
        Mockito.doReturn(headers).when(mockRequest).getHeaders();
        RntbdRequestArgs mockRntbdRequestArgs = Mockito.mock(RntbdRequestArgs.class);
        Mockito.doReturn(mockRequest).when(mockRntbdRequestArgs).serviceRequest();
        Mockito.doReturn("").when(mockRntbdRequestArgs).replicaPath();
        Mockito.doReturn(0L).when(mockRntbdRequestArgs).transportRequestId();

        RntbdRequestFrame mockRntbdRequestFrame = Mockito.mock(RntbdRequestFrame.class);
        Mockito.doReturn(RntbdConstants.RntbdOperationType.Connection).when(mockRntbdRequestFrame).getOperationType();
        RntbdRequestHeaders rntbdRequestHeaders = new RntbdRequestHeaders(mockRntbdRequestArgs, mockRntbdRequestFrame);

        final ByteBuf out = Unpooled.buffer();
        rntbdRequestHeaders.encode(out);
    }

    /*final class TestRntbdTokenStream extends RntbdTokenStream<RntbdConstants.RntbdRequestHeader> {
        TestRntbdTokenStream(EnumSet<RntbdConstants.RntbdRequestHeader> headers, Map<Short, RntbdConstants.RntbdRequestHeader> ids, ByteBuf in, Class<RntbdConstants.RntbdRequestHeader> classType) {
            super(headers, ids, in, classType);
        }
    }*/
}
