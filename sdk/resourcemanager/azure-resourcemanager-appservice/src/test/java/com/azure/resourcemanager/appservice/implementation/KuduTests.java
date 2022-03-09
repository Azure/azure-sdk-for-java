// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class KuduTests {

    private static final String LOG_STREAM =
        "2020-02-25T07:19:40  Welcome, you are now connected to log-streaming service. The default timeout is 2 hours."
            + " Change the timeout with the App Setting SCM_LOGSTREAM_TIMEOUT (in seconds).\n"
            + "2020-02-25T07:20:40  No new trace in the past 1 min(s).\n"
            + "2020-02-25T07:21:40  No new trace in the past 2 min(s).\n"
            + "2020-02-25 07:21:48 WA1-WEIDXU GET / X-ARR-LOG-ID=0b0cce68-2698-48a6-91cd-3bf7634ac63b 80 - ::1"
            + " AlwaysOn ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857 -"
            + " wa1-weidxu.azurewebsites.net 200 0 0 6651 679 78\n"
            + "2020-02-25 07:21:48 ~1WA1-WEIDXU GET / - 80 - 10.0.128.39 AlwaysOn - - wa1-weidxu.azurewebsites.net 200"
            + " 0 0 3108 443 140\n"
            + "2020-02-25 07:23:07 WA1-WEIDXU GET /coffeeshop/ X-ARR-LOG-ID=cb1691d6-a3d7-4331-92a1-670beeeb4c7a 80 -"
            + " 13.64.92.44"
            + " Mozilla/5.0+(Windows+NT+10.0;+Win64;+x64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/79.0.3945.130+Safari/537.36"
            + " JSESSIONID=3080AD1F0E745FFB80CE3DC1039A820F;+ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857"
            + " - wa1-weidxu.azurewebsites.net 200 0 0 1147 1124 503\n"
            + "2020-02-25 07:23:07 WA1-WEIDXU GET /coffeeshop/Images/bkg.png"
            + " X-ARR-LOG-ID=029e30f3-92b6-4c8d-8e90-fe1fc2554926 80 - 13.64.92.44"
            + " Mozilla/5.0+(Windows+NT+10.0;+Win64;+x64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/79.0.3945.130+Safari/537.36"
            + " JSESSIONID=3080AD1F0E745FFB80CE3DC1039A820F;+ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857"
            + " http://wa1-weidxu.azurewebsites.net/coffeeshop/Content/Site.css wa1-weidxu.azurewebsites.net 404 0 0"
            + " 1405 1099 15\n"
            + "2020-02-25 07:23:07 WA1-WEIDXU GET /coffeeshop/Images/brand.png"
            + " X-ARR-LOG-ID=200dda67-062b-445f-9501-03fd716b3b49 80 - 13.64.92.44"
            + " Mozilla/5.0+(Windows+NT+10.0;+Win64;+x64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/79.0.3945.130+Safari/537.36"
            + " JSESSIONID=3080AD1F0E745FFB80CE3DC1039A820F;+ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857"
            + " http://wa1-weidxu.azurewebsites.net/coffeeshop/Content/Site.css wa1-weidxu.azurewebsites.net 404 0 0"
            + " 1409 1105 38\n"
            + "2020-02-25 07:23:07 WA1-WEIDXU GET /Images/brand.png X-ARR-LOG-ID=7c8c9018-308b-4ccc-8f0a-def855b03797"
            + " 80 - 13.64.92.44"
            + " Mozilla/5.0+(Windows+NT+10.0;+Win64;+x64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/79.0.3945.130+Safari/537.36"
            + " ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857"
            + " http://wa1-weidxu.azurewebsites.net/coffeeshop/ wa1-weidxu.azurewebsites.net 404 0 0 1387 1011 46\n"
            + "2020-02-25 07:23:07 WA1-WEIDXU GET /Images/bkg.png X-ARR-LOG-ID=8f2a2049-bc2f-4c96-b8f6-31b3fd05cfea 80"
            + " - 13.64.92.44"
            + " Mozilla/5.0+(Windows+NT+10.0;+Win64;+x64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/79.0.3945.130+Safari/537.36"
            + " ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857"
            + " http://wa1-weidxu.azurewebsites.net/coffeeshop/ wa1-weidxu.azurewebsites.net 404 0 0 1383 1005 31\n"
            + "2020-02-25T07:24:40  No new trace in the past 1 min(s).\n"
            + "2020-02-25 07:25:16 WA1-WEIDXU GET /coffeeshop/Images/brand.png"
            + " X-ARR-LOG-ID=9cf7d479-82f0-41e0-9b81-62e435d312c0 80 - 13.64.92.44"
            + " Mozilla/5.0+(Windows+NT+10.0;+Win64;+x64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/79.0.3945.130+Safari/537.36"
            + " JSESSIONID=3080AD1F0E745FFB80CE3DC1039A820F;+ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857"
            + " http://wa1-weidxu.azurewebsites.net/coffeeshop/ wa1-weidxu.azurewebsites.net 404 0 0 1409 1089 46\n"
            + "2020-02-25 07:25:16 WA1-WEIDXU GET /coffeeshop/Images/bkg.png"
            + " X-ARR-LOG-ID=ac923602-6ab7-4f59-8222-941a82f05a2f 80 - 13.64.92.44"
            + " Mozilla/5.0+(Windows+NT+10.0;+Win64;+x64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/79.0.3945.130+Safari/537.36"
            + " JSESSIONID=3080AD1F0E745FFB80CE3DC1039A820F;+ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857"
            + " http://wa1-weidxu.azurewebsites.net/coffeeshop/ wa1-weidxu.azurewebsites.net 404 0 0 1405 1083 69\n"
            + "2020-02-25T07:26:40  No new trace in the past 1 min(s).\n"
            + "2020-02-25 07:26:48 ~1WA1-WEIDXU GET / - 80 - 10.0.128.39 AlwaysOn - - wa1-weidxu.azurewebsites.net 200"
            + " 0 0 3108 443 46\n"
            + "2020-02-25 07:26:48 WA1-WEIDXU GET / X-ARR-LOG-ID=7aebc121-d061-4230-9c2b-cc6b1be4b15a 80 - ::1"
            + " AlwaysOn ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857 -"
            + " wa1-weidxu.azurewebsites.net 200 0 0 6651 679 61\n"
            + "2020-02-25T07:28:40  No new trace in the past 1 min(s).\n"
            + "2020-02-25T07:29:40  No new trace in the past 2 min(s).\n"
            + "2020-02-25T07:30:40  No new trace in the past 3 min(s).\n"
            + "2020-02-25T07:31:40  No new trace in the past 4 min(s).\n"
            + "2020-02-25 07:31:48 ~1WA1-WEIDXU GET / - 80 - 10.0.128.39 AlwaysOn - - wa1-weidxu.azurewebsites.net 200"
            + " 0 0 3108 443 46\n"
            + "2020-02-25 07:31:48 WA1-WEIDXU GET / X-ARR-LOG-ID=5f7a3006-6e4e-42dd-aadb-d9a2f1af4736 80 - ::1"
            + " AlwaysOn ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857 -"
            + " wa1-weidxu.azurewebsites.net 200 0 0 6651 679 79\n"
            + "2020-02-25T07:33:40  No new trace in the past 1 min(s).\n"
            + "2020-02-25T07:34:40  No new trace in the past 2 min(s).\n"
            + "2020-02-25T07:35:40  No new trace in the past 3 min(s).\n"
            + "2020-02-25T07:36:40  No new trace in the past 4 min(s).\n"
            + "2020-02-25 07:36:48 ~1WA1-WEIDXU GET / - 80 - 10.0.128.39 AlwaysOn - - wa1-weidxu.azurewebsites.net 200"
            + " 0 0 3109 443 51\n"
            + "2020-02-25 07:36:48 WA1-WEIDXU GET / X-ARR-LOG-ID=9705abff-8f34-4bb9-867a-7194f7e0577c 80 - ::1"
            + " AlwaysOn ARRAffinity=9c45f76fcf2ed72fc0f79776f0387a38a4f53d6739f8aa06a66a120a04466857 -"
            + " wa1-weidxu.azurewebsites.net 200 0 0 6651 679 60\n"
            + "2020-02-25T07:38:40  No new trace in the past 1 min(s).\n"
            + "2020-02-25T07:39:40  No new trace in the past 2 min(s).\n"
            + "2020-02-25T07:40:40  No new trace in the past 3 min(s).\n"
            + "2020-02-25T07:41:40  No new trace in the past 4 min(s).\n";

    @Test
    public void testConversionFromByteToString() {
        // prepare expected lines
        String[] logArray = LOG_STREAM.split("\n");
        final List<String> expectedLogLines = new ArrayList<>(logArray.length);
        for (String logLine : logArray) {
            if (!logLine.isEmpty() && logLine.charAt(logLine.length() - 1) == '\r') {
                logLine = logLine.substring(0, logLine.length() - 1);
            }
            expectedLogLines.add(logLine);
        }

        byte[] logStreamBytes = LOG_STREAM.getBytes(StandardCharsets.UTF_8);

        // simple cast, just one big ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.wrap(logStreamBytes);
        Flux<ByteBuffer> byteBufferFlux = Flux.fromIterable(Arrays.asList(byteBuffer));

        Flux<String> lineFlux = KuduClient.streamFromFluxBytes(byteBufferFlux);
        List<String> lines = lineFlux.collectList().block();

        Assertions.assertEquals(expectedLogLines, lines);

        // simple case, Flux breaks at newline
        List<ByteBuffer> byteBuffers = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < logStreamBytes.length; ++i) {
            if (logStreamBytes[i] == '\n') {
                int nextOffset = i + 1;
                byteBuffers.add(ByteBuffer.wrap(logStreamBytes, offset, nextOffset - offset));
                offset = nextOffset;
            }
        }
        if (offset != logStreamBytes.length) {
            byteBuffers.add(ByteBuffer.wrap(logStreamBytes, offset, logStreamBytes.length - offset));
        }

        byteBufferFlux = Flux.fromIterable(byteBuffers);

        lineFlux = KuduClient.streamFromFluxBytes(byteBufferFlux);
        lines = lineFlux.collectList().block();

        Assertions.assertEquals(expectedLogLines, lines);

        // random
        for (int seed = 0; seed < 100; ++seed) {
            Random random = new Random(seed);

            byteBuffers = new ArrayList<>();
            offset = 0;
            for (int i = 0; i < logStreamBytes.length; ++i) {
                if (random.nextInt(256) == 0) {
                    int nextOffset = i + 1;
                    byteBuffers.add(ByteBuffer.wrap(logStreamBytes, offset, nextOffset - offset));
                    offset = nextOffset;
                }
            }
            if (offset != logStreamBytes.length) {
                byteBuffers.add(ByteBuffer.wrap(logStreamBytes, offset, logStreamBytes.length - offset));
            }

            byteBufferFlux = Flux.fromIterable(byteBuffers);

            lineFlux = KuduClient.streamFromFluxBytes(byteBufferFlux);
            lines = lineFlux.collectList().block();

            Assertions.assertEquals(expectedLogLines, lines);
        }

        // random
        logStreamBytes = LOG_STREAM.replace("\n", "\r\n").getBytes(StandardCharsets.UTF_8);
        for (int seed = 0; seed < 100; ++seed) {
            Random random = new Random(seed);

            byteBuffers = new ArrayList<>();
            offset = 0;
            for (int i = 0; i < logStreamBytes.length; ++i) {
                if (random.nextInt(256) == 0) {
                    int nextOffset = i + 1;
                    byteBuffers.add(ByteBuffer.wrap(logStreamBytes, offset, nextOffset - offset));
                    offset = nextOffset;
                }
            }
            if (offset != logStreamBytes.length) {
                byteBuffers.add(ByteBuffer.wrap(logStreamBytes, offset, logStreamBytes.length - offset));
            }

            byteBufferFlux = Flux.fromIterable(byteBuffers);

            lineFlux = KuduClient.streamFromFluxBytes(byteBufferFlux);
            lines = lineFlux.collectList().block();

            Assertions.assertEquals(expectedLogLines, lines);
        }
    }
}
