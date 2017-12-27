// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

/**
 * This package contains the implementation classes for ContentModeratorClient.
 * You use the API to scan your content as it is generated. Content Moderator then processes your content and sends the results along with relevant information either back to your systems or to the built-in review tool. You can use this information to take decisions e.g. take it down, send to human judge, etc.
 When using the API, images need to have a minimum of 128 pixels and a maximum file size of 4MB.
 Text can be at most 1024 characters long.
 If the content passed to the text API or the image API exceeds the size limits, the API will return an error code that informs about the issue.
 This API is currently available in:
 * West US - westus.api.cognitive.microsoft.com
 * East US 2 - eastus2.api.cognitive.microsoft.com
 * West Central US - westcentralus.api.cognitive.microsoft.com
 * West Europe - westeurope.api.cognitive.microsoft.com
 * Southeast Asia - southeastasia.api.cognitive.microsoft.com .
 */
package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;
