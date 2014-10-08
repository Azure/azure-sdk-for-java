/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.services.servicebus.implementation;

import com.microsoft.windowsazure.core.utils.DateFactory;
import com.microsoft.windowsazure.exception.ServiceException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class WrapTokenManagerTest {
    private WrapContract contract;
    private WrapTokenManager client;
    private DateFactory dateFactory;
    private Calendar calendar;

    @Before
    public void init() throws Exception {
        calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        dateFactory = mock(DateFactory.class);
        contract = mock(WrapContract.class);
        ServiceBusConnectionSettings settings = new ServiceBusConnectionSettings(
                null, null, "testurl", "testname", "testpassword", null, null);
        client = new WrapTokenManager(contract, dateFactory, settings);

        when(dateFactory.getDate()).thenAnswer(new Answer<Date>() {
            @Override
            public Date answer(InvocationOnMock invocation) throws Throwable {
                return calendar.getTime();
            }
        });
    }

    private void doIncrementingTokens() throws ServiceException {
        doAnswer(new Answer<WrapAccessTokenResult>() {
            int count = 0;

            @Override
            public WrapAccessTokenResult answer(InvocationOnMock invocation)
                    throws Throwable {
                ++count;
                WrapAccessTokenResult wrapResponse = new WrapAccessTokenResult();
                wrapResponse.setAccessToken("testaccesstoken1-" + count);
                wrapResponse.setExpiresIn(83);
                return wrapResponse;
            }
        }).when(contract).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope");

        doAnswer(new Answer<WrapAccessTokenResult>() {
            int count = 0;

            @Override
            public WrapAccessTokenResult answer(InvocationOnMock invocation)
                    throws Throwable {
                ++count;
                WrapAccessTokenResult wrapResponse = new WrapAccessTokenResult();
                wrapResponse.setAccessToken("testaccesstoken2-" + count);
                wrapResponse.setExpiresIn(83);
                return wrapResponse;
            }
        }).when(contract).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope2");
    }

    @Test
    public void clientUsesContractToGetToken() throws ServiceException,
            URISyntaxException {
        // Arrange
        doIncrementingTokens();

        // Act
        String accessToken = client
                .getAccessToken(new URI("https://test/scope"));

        // Assert
        assertNotNull(accessToken);
        assertEquals("testaccesstoken1-1", accessToken);
    }

    @Test
    public void clientWillNotCallMultipleTimesWhileAccessTokenIsValid()
            throws ServiceException, URISyntaxException {
        // Arrange
        doIncrementingTokens();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                "https://test/scope?arg=1"));
        String accessToken2 = client.getAccessToken(new URI(
                "https://test/scope?arg=2"));
        calendar.add(Calendar.SECOND, 40);
        String accessToken3 = client.getAccessToken(new URI(
                "https://test/scope?arg=3"));

        // Assert
        assertEquals("testaccesstoken1-1", accessToken1);
        assertEquals("testaccesstoken1-1", accessToken2);
        assertEquals("testaccesstoken1-1", accessToken3);

        verify(contract, times(1)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope");
    }

    @Test
    public void callsToDifferentPathsWillResultInDifferentAccessTokens()
            throws ServiceException, URISyntaxException {
        // Arrange
        doIncrementingTokens();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                "https://test/scope?arg=1"));
        String accessToken2 = client.getAccessToken(new URI(
                "https://test/scope2?arg=2"));
        calendar.add(Calendar.SECOND, 40);
        String accessToken3 = client.getAccessToken(new URI(
                "https://test/scope?arg=3"));

        // Assert
        assertEquals("testaccesstoken1-1", accessToken1);
        assertEquals("testaccesstoken2-1", accessToken2);
        assertEquals("testaccesstoken1-1", accessToken3);

        verify(contract, times(1)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope");
        verify(contract, times(1)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope2");
    }

    @Test
    public void clientWillBeCalledWhenTokenIsHalfwayToExpiring()
            throws ServiceException, URISyntaxException {
        // Arrange
        doIncrementingTokens();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                "https://test/scope"));
        String accessToken2 = client.getAccessToken(new URI(
                "https://test/scope"));
        calendar.add(Calendar.SECOND, 45);
        String accessToken3 = client.getAccessToken(new URI(
                "https://test/scope"));

        // Assert
        assertEquals("testaccesstoken1-1", accessToken1);
        assertEquals("testaccesstoken1-1", accessToken2);
        assertEquals("testaccesstoken1-2", accessToken3);

        verify(contract, times(2)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope");
    }

}
