/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.annotations;

public interface Doc_Http_Verb_Annotation_Value_URI_Template {
/**
 * This section describes the 'value' argument of GET, PUT, POST, DELETE, PATCH annotations
 * ========================================================================================
 *
 *  The value of `value` attribute can be relative path or absolute url of the endpoint (with query parameters? should we support this?).
 *
 *  This value can can be a template. i.e. as a template it can contains multiple variables, each variable surrounded by braces,
 *  a variable must match regular expression "[^/]+?"
 *
 *  e.g. relative uri template:
 *  ---------------------------
 *  "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}"
 *
 *  The relative URI will be resolved to absolute URI using the base uri provided during client configuration.
 *
 *  The relative uri template may or may not begin with a slash ('/'). When resolving, engine should ensure there is slash between base URI.
 *
 *  e.g. absolute uri template:
 *  ---------------------------
 *
 *  "https://management.azure.com/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}"
 *
 *  An absolute uri template can parameterize protocol, host and protocol parts.
 *
 *  {protocol}://{host}:{port}/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}"
 *
 *  The name of these parameters are predefined, if they are present then they must be present exactly in places where they are suppose to present in a valid URI.
 *
 *  Value of {protocol} is derived from {@link EndpointProtocol} annotation. Refer documentation of {@link EndpointProtocol} for more details.
 *  Value of {host} is derived from {@link EndpointHost} annotation. Refer documentation of {@link EndpointHost} for more details.
 *  Value of {port} is derived from {@link EndpointPort} annotation. Refer documentation of {@link EndpointPort} for more details.
 *
 *  Resolving variables in uri template:
 *  ------------------------------------
 *
 *  1. Refer documentation for {@link PathParam} regarding resolution of path segment variables in the uri template.
 *  2. TODO: QueryParam
 *  3. TODO: Fragments
 *  4. TODO: Matrix Params
 *
 *  Origin of uri template:
 *  -----------------------
 *
 *  [1]. A uri template can present as value of {@link [HTTPVerb]#value()} argument
 *  [2]. A uri template can present as value of a method parameter annotated with {@link Url}
 *  [3]. Resolving the full uri may involves using base uri configured in the client along with relative uri template
 **/
}
