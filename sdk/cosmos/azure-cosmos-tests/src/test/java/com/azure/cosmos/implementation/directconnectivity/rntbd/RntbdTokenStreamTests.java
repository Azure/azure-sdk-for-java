package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.http.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.directconnectivity.WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY;

public class RntbdTokenStreamTests {
    // Created this test for thin client testing, to make sure thin client headers are always special cased
    private static final Logger logger = LoggerFactory.getLogger(RntbdTokenStreamTests.class);
    @Test
    public void parseDotNet() {
        //dumpFile("E:\\Temp\\dotnetRead.bin");
        //dumpFile("E:\\Temp\\javad6bd7634-35ed-44d3-849f-364456be3001.bin");

        dumpFile("E:\\Temp\\dotnet_proxyInputRequest_Read_18.bin");
        dumpFile("E:\\Temp\\java_proxyInputRequest_Read_29.bin");
    }

    private void dumpFile(String fileName) {
        logger.error("FILENAME: {}", fileName);
        File file = new File(fileName);
        byte[] byteArray = null;
        try {
            byteArray = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuf content = Unpooled.wrappedBuffer(byteArray);

        if (RntbdFramer.canDecodeHead(content)) {

            final RntbdRequest request = RntbdRequest.decode(content);

            if (request != null) {
                logger.error("HEADERS: {}", request.getHeaders().dumpTokens());
            }

            logger.error("RNTBD REQUEST empty");
        }
    }

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
        rntbdRequestHeaders.encode(out, false);
    }

    /*final class TestRntbdTokenStream extends RntbdTokenStream<RntbdConstants.RntbdRequestHeader> {
        TestRntbdTokenStream(EnumSet<RntbdConstants.RntbdRequestHeader> headers, Map<Short, RntbdConstants.RntbdRequestHeader> ids, ByteBuf in, Class<RntbdConstants.RntbdRequestHeader> classType) {
            super(headers, ids, in, classType);
        }
    }*/
}
