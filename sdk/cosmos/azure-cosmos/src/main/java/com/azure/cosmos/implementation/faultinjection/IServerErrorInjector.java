// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Utils;

import java.time.Duration;

/***
 * Rntbd or gateway server error injector.
 */
public interface IServerErrorInjector {

    /***
     * Inject server response delay before sending the request to the service
     *
     * @param faultInjectionRequestArgs fault injection request args to find matched fault injection rules.
     * @param injectedDelay the injected delay. The value will be null if no matched rule can be found.
     * @return flag to indicate whether server response delay is injected.
     */
    boolean injectServerResponseDelayBeforeProcessing(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> injectedDelay);

    /***
     * Inject server response delay after sending the request to the service
     *
     * @param faultInjectionRequestArgs fault injection request args to find matched fault injection rules.
     * @param injectedDelay the injected delay. The value will be null if no matched rule can be found.
     * @return flag to indicate whether server response delay is injected.
     */
    boolean injectServerResponseDelayAfterProcessing(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> injectedDelay);

    /***
     * Inject server response error.
     *
     * @param faultInjectionRequestArgs fault injection request args to find matched fault injection rules.
     * @param injectedException the injected exception. The value will be null if no matched rule can be found.
     * @return flag to indicate whether server response error is injected.
     */
    boolean injectServerResponseError(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<CosmosException> injectedException);

    /***
     * Inject server connection delay error.
     *
     * @param faultInjectionRequestArgs fault injection request args to find matched fault injection rules.
     * @param injectedDelay the injected delay. Value will be null if no matched rule can be found.
     * @return flag to indicate whether server connection delay rule is injected.
     */
    boolean injectServerConnectionDelay(
        FaultInjectionRequestArgs faultInjectionRequestArgs,
        Utils.ValueHolder<Duration> injectedDelay);
}
