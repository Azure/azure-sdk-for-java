package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.ReturnValueWireType;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.MockHttpClient;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RestProxyWithMockTests extends RestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockHttpClient();
    }

    @Host("http://httpbin.org")
    private interface Service1 {
        @GET("Base64UrlBytes/10")
        @ReturnValueWireType(Base64Url.class)
        byte[] getBase64UrlBytes10();

        @GET("Base64UrlListOfBytes")
        @ReturnValueWireType(Base64Url.class)
        List<byte[]> getBase64UrlListOfBytes();

        @GET("Base64UrlListOfListOfBytes")
        @ReturnValueWireType(Base64Url.class)
        List<List<byte[]>> getBase64UrlListOfListOfBytes();

        @GET("Base64UrlMapOfBytes")
        @ReturnValueWireType(Base64Url.class)
        Map<String,byte[]> getBase64UrlMapOfBytes();

        @GET("DateTimeRfc1123")
        @ReturnValueWireType(DateTimeRfc1123.class)
        DateTime getDateTimeRfc1123();

        @GET("UnixTime")
        @ReturnValueWireType(UnixTime.class)
        DateTime getDateTimeUnix();
    }

    @Test
    public void service1GetBase64UrlBytes10() {
        final byte[] bytes = createService(Service1.class)
                .getBase64UrlBytes10();
        assertNotNull(bytes);
        assertEquals(10, bytes.length);
        for (int i = 0; i < 10; ++i) {
            assertEquals((byte)i, bytes[i]);
        }
    }

    @Test
    public void service1GetBase64UrlListOfBytes() {
        final List<byte[]> bytesList = createService(Service1.class)
                .getBase64UrlListOfBytes();
        assertNotNull(bytesList);
        assertEquals(3, bytesList.size());

        for (int i = 0; i < bytesList.size(); ++i) {
            final byte[] bytes = bytesList.get(i);
            assertNotNull(bytes);
            assertEquals((i + 1) * 10, bytes.length);
            for (int j = 0; j < bytes.length; ++j) {
                assertEquals((byte)j, bytes[j]);
            }
        }
    }

    @Test
    public void service1GetBase64UrlListOfListOfBytes() {
        final List<List<byte[]>> bytesList = createService(Service1.class)
                .getBase64UrlListOfListOfBytes();
        assertNotNull(bytesList);
        assertEquals(2, bytesList.size());

        for (int i = 0; i < bytesList.size(); ++i) {
            final List<byte[]> innerList = bytesList.get(i);
            assertEquals((i + 1) * 2, innerList.size());

            for (int j = 0; j < innerList.size(); ++j) {
                final byte[] bytes = innerList.get(j);
                assertNotNull(bytes);
                assertEquals((j + 1) * 5, bytes.length);
                for (int k = 0; k < bytes.length; ++k) {
                    assertEquals(k, bytes[k]);
                }
            }
        }
    }

    @Test
    public void service1GetBase64UrlMapOfBytes() {
        final Map<String,byte[]> bytesMap = createService(Service1.class)
                .getBase64UrlMapOfBytes();
        assertNotNull(bytesMap);
        assertEquals(2, bytesMap.size());

        for (int i = 0; i < bytesMap.size(); ++i) {
            final byte[] bytes = bytesMap.get(Integer.toString(i));

            final int expectedArrayLength = (i + 1) * 10;
            assertEquals(expectedArrayLength, bytes.length);
            for (int j = 0; j < expectedArrayLength; ++j) {
                assertEquals((byte)j, bytes[j]);
            }
        }
    }

    @Test
    public void service1GetDateTimeRfc1123() {
        final DateTime dateTime = createService(Service1.class)
                .getDateTimeRfc1123();
        assertNotNull(dateTime);
        assertEquals(new DateTime(0).withZone(dateTime.getZone()), dateTime);
    }

    @Test
    public void service1GetDateTimeUnix() {
        final DateTime dateTime = createService(Service1.class)
                .getDateTimeUnix();
        assertNotNull(dateTime);
        assertEquals(new DateTime(0).withZone(dateTime.getZone()), dateTime);
    }
}
