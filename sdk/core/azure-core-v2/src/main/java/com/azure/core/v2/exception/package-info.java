// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>This package contains the core exception classes used throughout the Azure SDKs.</p>
 *
 * <p>These exceptions are typically thrown in response to errors that occur when interacting with Azure services.
 * For example, if a network request to an Azure service fails an exception from this package is thrown.
 * The specific exception that is thrown depends on the nature of the error.</p>
 *
 * <p>Here are some of the key exceptions included in this package:</p>
 * <ul>
 *     <li>{@link com.azure.core.exception.AzureException}: The base class for all exceptions thrown by Azure SDKs.</li>
 *
 *     <li>{@link com.azure.core.exception.HttpRequestException}: Represents an exception thrown when an HTTP request
 *     fails.</li>
 *
 *     <li>{@link com.azure.core.exception.HttpResponseException}: Represents an exception thrown when an unsuccessful
 *     HTTP response is received from a service request.</li>
 *
 *     <li>{@link com.azure.core.exception.ResourceExistsException}: Represents an exception thrown when an HTTP request
 *     attempts to create a resource that already exists.</li>
 *
 *     <li>{@link com.azure.core.exception.ResourceNotFoundException}: Represents an exception thrown when an HTTP
 *     request attempts to access a resource that does not exist.</li>
 * </ul>
 *
 * <p>Some exceptions (noted in their documentation) include the HTTP request or response that led to the exception.</p>
 */
package com.azure.core.v2.exception;
