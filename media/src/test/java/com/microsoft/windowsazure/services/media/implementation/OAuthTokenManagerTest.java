/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.microsoft.windowsazure.core.utils.DateFactory;
import com.microsoft.windowsazure.exception.ServiceException;

public class OAuthTokenManagerTest {
    private OAuthContract contract;
    private OAuthTokenManager client;
    private DateFactory dateFactory;
    private Calendar calendar;

    @Before
    public void init() throws URISyntaxException {
        calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        dateFactory = mock(DateFactory.class);

        // Client channel = new Client();
        contract = mock(OAuthRestProxy.class);

        String acsBaseUri = "testurl";
        String accountName = "testname";
        String accountPassword = "testpassword";
        String scope = "testscope";

        client = new OAuthTokenManager(contract, dateFactory, acsBaseUri,
                accountName, accountPassword, scope);

        when(dateFactory.getDate()).thenAnswer(new Answer<Date>() {
            @Override
            public Date answer(InvocationOnMock invocation) throws Throwable {
                return calendar.getTime();
            }
        });
    }

    private void doIncrementingTokens() throws ServiceException,
            URISyntaxException, IOException {
        doAnswer(new Answer<OAuthTokenResponse>() {
            int count = 0;

            @Override
            public OAuthTokenResponse answer(InvocationOnMock invocation)
                    throws Throwable {
                ++count;
                OAuthTokenResponse wrapResponse = new OAuthTokenResponse();
                wrapResponse.setAccessToken("testaccesstoken1-" + count);
                wrapResponse.setExpiresIn(83);
                return wrapResponse;
            }
        }).when(contract).getAccessToken(new URI("testurl"), "testname",
                "testpassword", "testscope");

    }

    @Test
    public void clientUsesContractToGetToken() throws ServiceException,
            URISyntaxException, IOException {
        // Arrange
        doIncrementingTokens();

        // Act
        String accessToken = client.getAccessToken();

        // Assert
        assertNotNull(accessToken);
        assertEquals("testaccesstoken1-1", accessToken);
    }

    @Test
    public void clientWillNotCallMultipleTimesWhileAccessTokenIsValid()
            throws ServiceException, URISyntaxException, IOException {
        // Arrange
        doIncrementingTokens();

        // Act
        String accessToken1 = client.getAccessToken();
        String accessToken2 = client.getAccessToken();
        calendar.add(Calendar.SECOND, 40);
        String accessToken3 = client.getAccessToken();

        // Assert
        assertEquals("testaccesstoken1-1", accessToken1);
        assertEquals("testaccesstoken1-1", accessToken2);
        assertEquals("testaccesstoken1-1", accessToken3);

        verify(contract, times(1)).getAccessToken(new URI("testurl"),
                "testname", "testpassword", "testscope");
    }

    @Test
    public void clientWillBeCalledWhenTokenIsHalfwayToExpiring()
            throws ServiceException, URISyntaxException, IOException {
        // Arrange
        doIncrementingTokens();

        // Act
        String accessToken1 = client.getAccessToken();
        String accessToken2 = client.getAccessToken();
        calendar.add(Calendar.SECOND, 45);
        String accessToken3 = client.getAccessToken();

        // Assert
        assertEquals("testaccesstoken1-1", accessToken1);
        assertEquals("testaccesstoken1-1", accessToken2);
        assertEquals("testaccesstoken1-2", accessToken3);

        verify(contract, times(2)).getAccessToken(new URI("testurl"),
                "testname", "testpassword", "testscope");
    }

}
