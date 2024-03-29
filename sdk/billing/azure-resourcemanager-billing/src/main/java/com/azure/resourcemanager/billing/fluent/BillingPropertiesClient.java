// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.resourcemanager.billing.fluent.models.BillingPropertyInner;

/** An instance of this class provides access to all the operations defined in BillingPropertiesClient. */
public interface BillingPropertiesClient {
    /**
     * Get the billing properties for a subscription. This operation is not supported for billing accounts with
     * agreement type Enterprise Agreement.
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the billing properties for a subscription along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<BillingPropertyInner> getWithResponse(Context context);

    /**
     * Get the billing properties for a subscription. This operation is not supported for billing accounts with
     * agreement type Enterprise Agreement.
     *
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the billing properties for a subscription.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    BillingPropertyInner get();

    /**
     * Updates the billing property of a subscription. Currently, cost center can be updated. The operation is supported
     * only for billing accounts with agreement type Microsoft Customer Agreement.
     *
     * @param parameters Request parameters that are provided to the update billing property operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a billing property along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<BillingPropertyInner> updateWithResponse(BillingPropertyInner parameters, Context context);

    /**
     * Updates the billing property of a subscription. Currently, cost center can be updated. The operation is supported
     * only for billing accounts with agreement type Microsoft Customer Agreement.
     *
     * @param parameters Request parameters that are provided to the update billing property operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a billing property.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    BillingPropertyInner update(BillingPropertyInner parameters);
}
