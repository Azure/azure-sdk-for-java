// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This package contains the core model classes used across the Azure SDK.
 *
 * <p>These classes provide common structures and functionality for working with Azure services. They include
 * representations for various types of data, such GeoJSON objects, and JSON Patch documents.</p>
 *
 * <p>Classes in this package are typically used as base classes or utility classes, and are extended or used by other
 * classes in the Azure SDK to provide service-specific functionality.</p>
 *
 * <p>Some of the key classes in this package include:</p>
 * <ul>
 *     <li>{@link com.azure.core.models.GeoObject}: Represents an abstract geometric object in GeoJSON format.</li>
 *     <li>{@link com.azure.core.models.GeoPolygonCollection}: Represents a collection of
 *     {@link com.azure.core.models.GeoPolygon GeoPolygons} in GeoJSON format.</li>
 *     <li>{@link com.azure.core.models.JsonPatchDocument}: Represents a JSON Patch document.</li>
 *     <li>{@link com.azure.core.models.ResponseError}: Represents the error details of an HTTP response.</li>
 *     <li>{@link com.azure.core.models.ResponseInnerError}: Represents the inner error details of a
 *     {@link com.azure.core.models.ResponseError}.</li>
 *     <li>{@link com.azure.core.models.MessageContent}: Represents a message with a specific content type and data.</li>
 * </ul>
 */
package com.azure.core.models;
