/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.BadRequestException;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Strings;
import org.apache.commons.lang3.EnumUtils;

public class RequestHelper {
    public static ConsistencyLevel GetConsistencyLevelToUse(GatewayServiceConfigurationReader serviceConfigReader,
                                                            RxDocumentServiceRequest request) throws CosmosClientException {
        ConsistencyLevel consistencyLevelToUse = serviceConfigReader.getDefaultConsistencyLevel();

        String requestConsistencyLevelHeaderValue = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel = EnumUtils.getEnum(ConsistencyLevel.class, requestConsistencyLevelHeaderValue);
            if (requestConsistencyLevel == null) {
                throw new BadRequestException(
                        String.format(
                                RMResources.InvalidHeaderValue,
                                requestConsistencyLevelHeaderValue,
                                HttpConstants.HttpHeaders.CONSISTENCY_LEVEL));
            }

            consistencyLevelToUse = requestConsistencyLevel;
        }

        return consistencyLevelToUse;
    }
}
