// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SqlQuerySpecLoggerTest {

    private SqlQuerySpecLogger sqlQuerySpecLogger;
    private Logger logger;
    private boolean isDebugEnabledInvoked = false;

    @BeforeMethod(groups = {"unit"})
    private void setup() {
        logger = Mockito.mock(Logger.class);
        sqlQuerySpecLogger = new SqlQuerySpecLogger(logger);
    }

    private SqlQuerySpec createQuerySpec(String queryText) {
        SqlQuerySpec querySpec = new SqlQuerySpec();
        querySpec.setQueryText(queryText);
        return querySpec;
    }

    private void setupIsEnabled(boolean isTraceEnabled, boolean isDebugEnabled) {
        doReturn(isTraceEnabled).when(logger).isTraceEnabled();
        doAnswer(invocationOnMock -> {
            isDebugEnabledInvoked = true;
            return isDebugEnabled;
        }).when(logger).isDebugEnabled();
    }

    private void verifyFinalInteractions() {
        verify(logger).isTraceEnabled();
        if (isDebugEnabledInvoked) {
            verify(logger).isDebugEnabled();
        }
        verifyNoMoreInteractions(logger);
    }

    @Test(groups = {"unit"})
    public void shouldNotLogIfDebugNotEnabled() {
        setupIsEnabled(false, false);
        final SqlQuerySpec querySpec = createQuerySpec("select * from r");

        sqlQuerySpecLogger.logQuery(querySpec);
        verifyFinalInteractions();
    }

    @Test(groups = {"unit"})
    public void shouldNotLogParametersIfTraceNotEnabled() {
        setupIsEnabled(false, true);
        final SqlQuerySpec querySpec = createQuerySpec("select * from r");
        querySpec.setParameters(Collections.singletonList(new SqlParameter("@id", "id")));

        sqlQuerySpecLogger.logQuery(querySpec);

        verify(logger).debug("select * from r");
        verifyFinalInteractions();
    }

    @Test(groups = {"unit"})
    public void shouldLogParametersIfTraceEnabled() {
        setupIsEnabled(true, true);
        final SqlQuerySpec querySpec = createQuerySpec("select * from r where id = @id");
        querySpec.setParameters(Collections.singletonList(new SqlParameter("@id", "id")));

        sqlQuerySpecLogger.logQuery(querySpec);

        verify(logger).debug("select * from r where id = @id" + System.getProperty("line.separator") + " > param: @id = id");
        verifyFinalInteractions();
    }

    @Test(groups = {"unit"})
    public void shouldNotLogParametersIfTraceEnabledButSpecHasNoParameters() {
        setupIsEnabled(true, true);
        final SqlQuerySpec querySpec = createQuerySpec("select * from r");

        sqlQuerySpecLogger.logQuery(querySpec);

        verify(logger).debug("select * from r");
        verifyFinalInteractions();
    }

}
