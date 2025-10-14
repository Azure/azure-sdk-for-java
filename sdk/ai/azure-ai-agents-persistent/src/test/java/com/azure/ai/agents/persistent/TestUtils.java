// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import org.junit.jupiter.params.provider.Arguments;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

public class TestUtils {

    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients}
     * that should be tested.
     *
     * @return A stream of HttpClients to test.
     */
    static Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient)));
        return argumentsList.stream();
    }

    static <T> int size(PagedIterable<T> pagedIterable) {
        int length = 0;
        if (pagedIterable != null) {
            for (PagedResponse<T> page : pagedIterable.iterableByPage()) {
                length += page.getValue().size();
            }
        }
        return length;
    }

    static <T> T first(PagedIterable<T> pagedIterable) {
        T firstElement = null;
        if (pagedIterable != null) {
            Iterator<T> iterator = pagedIterable.iterator();
            if (iterator.hasNext()) {
                firstElement = iterator.next();
            }
        }
        return firstElement;
    }
}
