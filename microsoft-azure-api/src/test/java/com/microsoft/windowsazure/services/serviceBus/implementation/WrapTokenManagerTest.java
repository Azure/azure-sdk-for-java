package com.microsoft.windowsazure.services.serviceBus.implementation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.utils.DateFactory;

public class WrapTokenManagerTest {
    private WrapContract contract;
    private WrapTokenManager client;
    private DateFactory dateFactory;
    private Calendar calendar;

    @Before
    public void init() {
        calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        dateFactory = mock(DateFactory.class);
        contract = mock(WrapContract.class);
        client = new WrapTokenManager(contract, dateFactory, "testurl", "testscope", "testname", "testpassword");

        when(dateFactory.getDate()).thenAnswer(new Answer<Date>() {
            public Date answer(InvocationOnMock invocation) throws Throwable {
                return calendar.getTime();
            }
        });

    }

    @Test
    public void clientUsesContractToGetToken() throws ServiceException {
        // Arrange
        WrapAccessTokenResult wrapResponse = new WrapAccessTokenResult();
        wrapResponse.setAccessToken("testaccesstoken");
        wrapResponse.setExpiresIn(83);

        when(contract.wrapAccessToken("testurl", "testname", "testpassword", "testscope")).thenReturn(wrapResponse);

        // Act
        String accessToken = client.getAccessToken();

        // Assert
        assertNotNull(accessToken);
        assertEquals("testaccesstoken", accessToken);
    }

    @Test
    public void clientWillNotCallMultipleTimesWhileAccessTokenIsValid() throws ServiceException {
        // Arrange
        WrapAccessTokenResult wrapResponse = new WrapAccessTokenResult();
        wrapResponse.setAccessToken("testaccesstoken");
        wrapResponse.setExpiresIn(83);

        when(contract.wrapAccessToken("testurl", "testname", "testpassword", "testscope")).thenReturn(wrapResponse);

        // Act
        String accessToken1 = client.getAccessToken();
        String accessToken2 = client.getAccessToken();
        calendar.add(Calendar.SECOND, 40);
        String accessToken3 = client.getAccessToken();

        // Assert
        assertEquals("testaccesstoken", accessToken1);
        assertEquals("testaccesstoken", accessToken2);
        assertEquals("testaccesstoken", accessToken3);

        verify(contract, times(1)).wrapAccessToken("testurl", "testname", "testpassword", "testscope");
    }

    @Test
    public void clientWillBeCalledWhenTokenIsHalfwayToExpiring() throws ServiceException {
        // Arrange
        doAnswer(new Answer<WrapAccessTokenResult>() {
            int count = 0;

            public WrapAccessTokenResult answer(InvocationOnMock invocation) throws Throwable {
                ++count;
                WrapAccessTokenResult wrapResponse = new WrapAccessTokenResult();
                wrapResponse.setAccessToken("testaccesstoken" + count);
                wrapResponse.setExpiresIn(83);
                return wrapResponse;
            }
        }).when(contract).wrapAccessToken("testurl", "testname", "testpassword", "testscope");

        // Act
        String accessToken1 = client.getAccessToken();
        String accessToken2 = client.getAccessToken();
        calendar.add(Calendar.SECOND, 45);
        String accessToken3 = client.getAccessToken();

        // Assert
        assertEquals("testaccesstoken1", accessToken1);
        assertEquals("testaccesstoken1", accessToken2);
        assertEquals("testaccesstoken2", accessToken3);

        verify(contract, times(2)).wrapAccessToken("testurl", "testname", "testpassword", "testscope");
    }

}
