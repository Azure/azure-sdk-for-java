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

    private void setupWrapContract() throws ServiceException {
        doAnswer(new Answer<WrapAccessTokenResult>() {
            int count = 0;

            @Override
            public WrapAccessTokenResult answer(InvocationOnMock invocation)
                    throws Throwable {
                ++count;
                WrapAccessTokenResult wrapResponse = new WrapAccessTokenResult();
                wrapResponse.setAccessToken("testaccesstoken" + count);
                wrapResponse.setExpiresIn(83);
                return wrapResponse;
            }
        }).when(contract).wrapAccessToken(eq("testurl"), eq("testname"),
                eq("testpassword"), anyString());
    }

    @Test
    public void clientUsesContractToGetToken() throws ServiceException,
            URISyntaxException {
        // Arrange
        setupWrapContract();

        // Act
        String accessToken = client
                .getAccessToken(new URI("https://test/scope"));

        // Assert
        assertNotNull(accessToken);
        assertEquals("testaccesstoken1", accessToken);
    }

    @Test
    public void clientWillNotCallMultipleTimesWhileAccessTokenIsValid()
            throws ServiceException, URISyntaxException {
        // Arrange
        setupWrapContract();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                "https://test/scope?arg=1"));
        String accessToken2 = client.getAccessToken(new URI(
                "https://test/scope?arg=2"));
        calendar.add(Calendar.SECOND, 40);
        String accessToken3 = client.getAccessToken(new URI(
                "https://test/scope?arg=3"));

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken1", accessToken2);
        assertEquals("testaccesstoken1", accessToken3);

        verify(contract, times(1)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope");
    }

    @Test
    public void callsToDifferentPathsWillResultInDifferentAccessTokens()
            throws ServiceException, URISyntaxException {
        // Arrange
        setupWrapContract();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                "https://test/scope?arg=1"));
        String accessToken2 = client.getAccessToken(new URI(
                "https://test/scope2?arg=2"));
        calendar.add(Calendar.SECOND, 40);
        String accessToken3 = client.getAccessToken(new URI(
                "https://test/scope?arg=3"));

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken2", accessToken2);
        assertEquals("testaccesstoken1", accessToken3);

        verify(contract, times(1)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope");
        verify(contract, times(1)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope2");
    }

    @Test
    public void clientWillBeCalledWhenTokenIsHalfwayToExpiring()
            throws ServiceException, URISyntaxException {
        // Arrange
        setupWrapContract();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                "https://test/scope"));
        String accessToken2 = client.getAccessToken(new URI(
                "https://test/scope"));
        calendar.add(Calendar.SECOND, 45);
        String accessToken3 = client.getAccessToken(new URI(
                "https://test/scope"));

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken1", accessToken2);
        assertEquals("testaccesstoken2", accessToken3);

        verify(contract, times(2)).wrapAccessToken("testurl", "testname",
                "testpassword", "http://test/scope");
    }

    @Test
    public void clientWillNotCallMultipleTimesForDeleteMessageOnTheSameQueue()
            throws ServiceException, URISyntaxException {
        // Arrange
        String baseUrl = "https://someNamespace.servicebus.windows.net/somequeue/messages/";
        setupWrapContract();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                baseUrl + "1231232/57808b34-b477-4c60-bdc0-c8489a856680"));
        String accessToken2 = client.getAccessToken(new URI(
                baseUrl + "234234/89967abb-dab1-4867-bf79-8cf8fe3fa7e9"));

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken1", accessToken2);
    }

    @Test
    public void clientWillNotCallMultipleTimesForDifferentResourceLists()
            throws ServiceException, URISyntaxException {
        // Arrange
        String baseUrl = "https://someNamespace.servicebus.windows.net/";
        setupWrapContract();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                baseUrl + "$Resources/Queues?api-version=2013-07"));
        String accessToken2 = client.getAccessToken(new URI(
                baseUrl + "$Resources/Topics?api-version=2013-07"));

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken1", accessToken2);
    }

    @Test
    public void clientWillCallMultipleTimesForDifferentSubscriptionsOnTheSameTopic()
            throws ServiceException, URISyntaxException {
        // Arrange
        String baseUrl = "https://someNamespace.servicebus.windows.net/someTopic/subscriptions/";
        setupWrapContract();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                baseUrl + "sub1/messages/head"));
        String accessToken2 = client.getAccessToken(new URI(
                baseUrl + "sub2/messages/head"));
        String accessToken3 = client.getAccessToken(new URI(
                baseUrl + "sub1/messages/head"));

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken2", accessToken2);
        assertEquals("testaccesstoken1", accessToken3);
    }

    @Test
    public void clientWillNotCallMultipleTimesForRenewLockOnTheSameSubscription()
            throws ServiceException, URISyntaxException {
        // Arrange
        String baseUrl = "https://someNamespace.servicebus.windows.net/someTopic/subscriptions/";
        setupWrapContract();

        // Act
        String accessToken1 = client.getAccessToken(new URI(
                baseUrl + "sub1/messages/1231232/57808b34-b477-4c60-bdc0-c8489a856680"));
        String accessToken2 = client.getAccessToken(new URI(
                baseUrl + "sub1/messages/234234/89967abb-dab1-4867-bf79-8cf8fe3fa7e9"));

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken1", accessToken2);
    }

    @Test
    public void givesCorrectScopeForQueueAndTopicMessages() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net/topicPath/messages");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net/topicPath", scope);
    }

    @Test
    public void givesCorrectScopeForQueueMessagesHead() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net/queuePath/messages/head");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net/queuePath", scope);
    }

    @Test
    public void givesCorrectScopeForSubscriptionMessagesHead() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net/someTopicPath/" +
                "subscriptions/someSub/messages/head");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net/someTopicPath/" +
                "subscriptions/someSub", scope);
    }

    @Test
    public void givesCorrectScopeForQueueMessagesLock() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net/queuePath/messages/"
                + "31907572-1647-43c3-8741-631acd554d6f/7da9cfd5-40d5-4bb1-8d64-ec5a52e1c547");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net/queuePath", scope);
    }

    @Test
    public void givesCorrectScopeForSubscriptionMessagesLock() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net/topicPath/subscriptions/"
                + "sub/messages/31907572-1647-43c3-8741-631acd554d6f/"
                + "7da9cfd5-40d5-4bb1-8d64-ec5a52e1c547");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net/topicPath/subscriptions/sub", scope);
    }

    @Test
    public void givesCorrectScopeForSubscriptionSubscriptionNames() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net/subscriptions/"
                + "suBscripTions/subscriptions/messages/head");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net/subscriptions/suBscripTions/subscriptions", scope);
    }

    @Test
    public void givesCorrectScopeForMessagesQueueName() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net/messages/messages");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net/messages", scope);
    }

    @Test
    public void givesCorrectScopeForUrlWithPath() throws URISyntaxException {
        // Arrange
        URI uri = new URI("https://serviceNamespace.servicebus.windows.net");

        // Act
        String scope = WrapTokenManager.extractCacheScope(uri);

        // Assert
        assertEquals("http://serviceNamespace.servicebus.windows.net", scope);
    }
}