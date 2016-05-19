/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.batch.protocol.models.AutoScaleRun;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudPool;
import com.microsoft.azure.batch.protocol.models.NodeRemoveParameter;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.batch.protocol.models.PoolAddHeaders;
import com.microsoft.azure.batch.protocol.models.PoolAddOptions;
import com.microsoft.azure.batch.protocol.models.PoolAddParameter;
import com.microsoft.azure.batch.protocol.models.PoolDeleteHeaders;
import com.microsoft.azure.batch.protocol.models.PoolDeleteOptions;
import com.microsoft.azure.batch.protocol.models.PoolDisableAutoScaleHeaders;
import com.microsoft.azure.batch.protocol.models.PoolDisableAutoScaleOptions;
import com.microsoft.azure.batch.protocol.models.PoolEnableAutoScaleHeaders;
import com.microsoft.azure.batch.protocol.models.PoolEnableAutoScaleOptions;
import com.microsoft.azure.batch.protocol.models.PoolEnableAutoScaleParameter;
import com.microsoft.azure.batch.protocol.models.PoolEvaluateAutoScaleHeaders;
import com.microsoft.azure.batch.protocol.models.PoolEvaluateAutoScaleOptions;
import com.microsoft.azure.batch.protocol.models.PoolExistsHeaders;
import com.microsoft.azure.batch.protocol.models.PoolExistsOptions;
import com.microsoft.azure.batch.protocol.models.PoolGetAllPoolsLifetimeStatisticsHeaders;
import com.microsoft.azure.batch.protocol.models.PoolGetAllPoolsLifetimeStatisticsOptions;
import com.microsoft.azure.batch.protocol.models.PoolGetHeaders;
import com.microsoft.azure.batch.protocol.models.PoolGetOptions;
import com.microsoft.azure.batch.protocol.models.PoolListHeaders;
import com.microsoft.azure.batch.protocol.models.PoolListNextOptions;
import com.microsoft.azure.batch.protocol.models.PoolListOptions;
import com.microsoft.azure.batch.protocol.models.PoolListPoolUsageMetricsHeaders;
import com.microsoft.azure.batch.protocol.models.PoolListPoolUsageMetricsNextOptions;
import com.microsoft.azure.batch.protocol.models.PoolListPoolUsageMetricsOptions;
import com.microsoft.azure.batch.protocol.models.PoolPatchHeaders;
import com.microsoft.azure.batch.protocol.models.PoolPatchOptions;
import com.microsoft.azure.batch.protocol.models.PoolPatchParameter;
import com.microsoft.azure.batch.protocol.models.PoolRemoveNodesHeaders;
import com.microsoft.azure.batch.protocol.models.PoolRemoveNodesOptions;
import com.microsoft.azure.batch.protocol.models.PoolResizeHeaders;
import com.microsoft.azure.batch.protocol.models.PoolResizeOptions;
import com.microsoft.azure.batch.protocol.models.PoolResizeParameter;
import com.microsoft.azure.batch.protocol.models.PoolStatistics;
import com.microsoft.azure.batch.protocol.models.PoolStopResizeHeaders;
import com.microsoft.azure.batch.protocol.models.PoolStopResizeOptions;
import com.microsoft.azure.batch.protocol.models.PoolUpdatePropertiesHeaders;
import com.microsoft.azure.batch.protocol.models.PoolUpdatePropertiesOptions;
import com.microsoft.azure.batch.protocol.models.PoolUpdatePropertiesParameter;
import com.microsoft.azure.batch.protocol.models.PoolUpgradeOSHeaders;
import com.microsoft.azure.batch.protocol.models.PoolUpgradeOSOptions;
import com.microsoft.azure.batch.protocol.models.PoolUsageMetrics;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in Pools.
 */
public interface Pools {
    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetrics&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<PoolUsageMetrics>, PoolListPoolUsageMetricsHeaders> listPoolUsageMetrics() throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listPoolUsageMetricsAsync(final ListOperationCallback<PoolUsageMetrics> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param poolListPoolUsageMetricsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetrics&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<PoolUsageMetrics>, PoolListPoolUsageMetricsHeaders> listPoolUsageMetrics(final PoolListPoolUsageMetricsOptions poolListPoolUsageMetricsOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param poolListPoolUsageMetricsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listPoolUsageMetricsAsync(final PoolListPoolUsageMetricsOptions poolListPoolUsageMetricsOptions, final ListOperationCallback<PoolUsageMetrics> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PoolStatistics object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PoolStatistics, PoolGetAllPoolsLifetimeStatisticsHeaders> getAllPoolsLifetimeStatistics() throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAllPoolsLifetimeStatisticsAsync(final ServiceCallback<PoolStatistics> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param poolGetAllPoolsLifetimeStatisticsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PoolStatistics object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PoolStatistics, PoolGetAllPoolsLifetimeStatisticsHeaders> getAllPoolsLifetimeStatistics(PoolGetAllPoolsLifetimeStatisticsOptions poolGetAllPoolsLifetimeStatisticsOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param poolGetAllPoolsLifetimeStatisticsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAllPoolsLifetimeStatisticsAsync(PoolGetAllPoolsLifetimeStatisticsOptions poolGetAllPoolsLifetimeStatisticsOptions, final ServiceCallback<PoolStatistics> serviceCallback) throws IllegalArgumentException;

    /**
     * Adds a pool to the specified account.
     *
     * @param pool The pool to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolAddHeaders> add(PoolAddParameter pool) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a pool to the specified account.
     *
     * @param pool The pool to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addAsync(PoolAddParameter pool, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Adds a pool to the specified account.
     *
     * @param pool The pool to be added.
     * @param poolAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolAddHeaders> add(PoolAddParameter pool, PoolAddOptions poolAddOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a pool to the specified account.
     *
     * @param pool The pool to be added.
     * @param poolAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addAsync(PoolAddParameter pool, PoolAddOptions poolAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the pools in the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPool&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<CloudPool>, PoolListHeaders> list() throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the pools in the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final ListOperationCallback<CloudPool> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the pools in the specified account.
     *
     * @param poolListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPool&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<CloudPool>, PoolListHeaders> list(final PoolListOptions poolListOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the pools in the specified account.
     *
     * @param poolListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final PoolListOptions poolListOptions, final ListOperationCallback<CloudPool> serviceCallback) throws IllegalArgumentException;

    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolDeleteHeaders> delete(String poolId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String poolId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @param poolDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolDeleteHeaders> delete(String poolId, PoolDeleteOptions poolDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @param poolDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String poolId, PoolDeleteOptions poolDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the boolean object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<Boolean, PoolExistsHeaders> exists(String poolId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall existsAsync(String poolId, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolExistsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the boolean object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<Boolean, PoolExistsHeaders> exists(String poolId, PoolExistsOptions poolExistsOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolExistsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall existsAsync(String poolId, PoolExistsOptions poolExistsOptions, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudPool object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudPool, PoolGetHeaders> get(String poolId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String poolId, final ServiceCallback<CloudPool> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudPool object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudPool, PoolGetHeaders> get(String poolId, PoolGetOptions poolGetOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String poolId, PoolGetOptions poolGetOptions, final ServiceCallback<CloudPool> serviceCallback) throws IllegalArgumentException;

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolPatchHeaders> patch(String poolId, PoolPatchParameter poolPatchParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall patchAsync(String poolId, PoolPatchParameter poolPatchParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @param poolPatchOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolPatchHeaders> patch(String poolId, PoolPatchParameter poolPatchParameter, PoolPatchOptions poolPatchOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @param poolPatchOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall patchAsync(String poolId, PoolPatchParameter poolPatchParameter, PoolPatchOptions poolPatchOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolDisableAutoScaleHeaders> disableAutoScale(String poolId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall disableAutoScaleAsync(String poolId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @param poolDisableAutoScaleOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolDisableAutoScaleHeaders> disableAutoScale(String poolId, PoolDisableAutoScaleOptions poolDisableAutoScaleOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @param poolDisableAutoScaleOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall disableAutoScaleAsync(String poolId, PoolDisableAutoScaleOptions poolDisableAutoScaleOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolEnableAutoScaleHeaders> enableAutoScale(String poolId, PoolEnableAutoScaleParameter poolEnableAutoScaleParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall enableAutoScaleAsync(String poolId, PoolEnableAutoScaleParameter poolEnableAutoScaleParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @param poolEnableAutoScaleOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolEnableAutoScaleHeaders> enableAutoScale(String poolId, PoolEnableAutoScaleParameter poolEnableAutoScaleParameter, PoolEnableAutoScaleOptions poolEnableAutoScaleOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @param poolEnableAutoScaleOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall enableAutoScaleAsync(String poolId, PoolEnableAutoScaleParameter poolEnableAutoScaleParameter, PoolEnableAutoScaleOptions poolEnableAutoScaleOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula A formula for the desired number of compute nodes in the pool.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AutoScaleRun object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<AutoScaleRun, PoolEvaluateAutoScaleHeaders> evaluateAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula A formula for the desired number of compute nodes in the pool.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall evaluateAutoScaleAsync(String poolId, String autoScaleFormula, final ServiceCallback<AutoScaleRun> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula A formula for the desired number of compute nodes in the pool.
     * @param poolEvaluateAutoScaleOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AutoScaleRun object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<AutoScaleRun, PoolEvaluateAutoScaleHeaders> evaluateAutoScale(String poolId, String autoScaleFormula, PoolEvaluateAutoScaleOptions poolEvaluateAutoScaleOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula A formula for the desired number of compute nodes in the pool.
     * @param poolEvaluateAutoScaleOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall evaluateAutoScaleAsync(String poolId, String autoScaleFormula, PoolEvaluateAutoScaleOptions poolEvaluateAutoScaleOptions, final ServiceCallback<AutoScaleRun> serviceCallback) throws IllegalArgumentException;

    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolResizeHeaders> resize(String poolId, PoolResizeParameter poolResizeParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall resizeAsync(String poolId, PoolResizeParameter poolResizeParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @param poolResizeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolResizeHeaders> resize(String poolId, PoolResizeParameter poolResizeParameter, PoolResizeOptions poolResizeOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @param poolResizeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall resizeAsync(String poolId, PoolResizeParameter poolResizeParameter, PoolResizeOptions poolResizeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolStopResizeHeaders> stopResize(String poolId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall stopResizeAsync(String poolId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @param poolStopResizeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolStopResizeHeaders> stopResize(String poolId, PoolStopResizeOptions poolStopResizeOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @param poolStopResizeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall stopResizeAsync(String poolId, PoolStopResizeOptions poolStopResizeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolUpdatePropertiesHeaders> updateProperties(String poolId, PoolUpdatePropertiesParameter poolUpdatePropertiesParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updatePropertiesAsync(String poolId, PoolUpdatePropertiesParameter poolUpdatePropertiesParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @param poolUpdatePropertiesOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolUpdatePropertiesHeaders> updateProperties(String poolId, PoolUpdatePropertiesParameter poolUpdatePropertiesParameter, PoolUpdatePropertiesOptions poolUpdatePropertiesOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @param poolUpdatePropertiesOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updatePropertiesAsync(String poolId, PoolUpdatePropertiesParameter poolUpdatePropertiesParameter, PoolUpdatePropertiesOptions poolUpdatePropertiesOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion The Azure Guest OS version to be installed on the virtual machines in the pool.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolUpgradeOSHeaders> upgradeOS(String poolId, String targetOSVersion) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion The Azure Guest OS version to be installed on the virtual machines in the pool.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall upgradeOSAsync(String poolId, String targetOSVersion, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion The Azure Guest OS version to be installed on the virtual machines in the pool.
     * @param poolUpgradeOSOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolUpgradeOSHeaders> upgradeOS(String poolId, String targetOSVersion, PoolUpgradeOSOptions poolUpgradeOSOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion The Azure Guest OS version to be installed on the virtual machines in the pool.
     * @param poolUpgradeOSOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall upgradeOSAsync(String poolId, String targetOSVersion, PoolUpgradeOSOptions poolUpgradeOSOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolRemoveNodesHeaders> removeNodes(String poolId, NodeRemoveParameter nodeRemoveParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall removeNodesAsync(String poolId, NodeRemoveParameter nodeRemoveParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @param poolRemoveNodesOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, PoolRemoveNodesHeaders> removeNodes(String poolId, NodeRemoveParameter nodeRemoveParameter, PoolRemoveNodesOptions poolRemoveNodesOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @param poolRemoveNodesOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall removeNodesAsync(String poolId, NodeRemoveParameter nodeRemoveParameter, PoolRemoveNodesOptions poolRemoveNodesOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetrics&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<PoolUsageMetrics>, PoolListPoolUsageMetricsHeaders> listPoolUsageMetricsNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listPoolUsageMetricsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<PoolUsageMetrics> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListPoolUsageMetricsNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetrics&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<PoolUsageMetrics>, PoolListPoolUsageMetricsHeaders> listPoolUsageMetricsNext(final String nextPageLink, final PoolListPoolUsageMetricsNextOptions poolListPoolUsageMetricsNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListPoolUsageMetricsNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listPoolUsageMetricsNextAsync(final String nextPageLink, final PoolListPoolUsageMetricsNextOptions poolListPoolUsageMetricsNextOptions, final ServiceCall serviceCall, final ListOperationCallback<PoolUsageMetrics> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPool&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<CloudPool>, PoolListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudPool> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPool&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<CloudPool>, PoolListHeaders> listNext(final String nextPageLink, final PoolListNextOptions poolListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final PoolListNextOptions poolListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudPool> serviceCallback) throws IllegalArgumentException;

}
