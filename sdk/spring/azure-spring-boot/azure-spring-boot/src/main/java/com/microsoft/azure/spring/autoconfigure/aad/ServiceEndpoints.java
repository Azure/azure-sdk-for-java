/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import lombok.Data;

/**
 * Pojo file to store the service urls for different azure services.
 */
@Data
public class ServiceEndpoints {
    private String aadSigninUri;
    private String aadGraphApiUri;
    private String aadKeyDiscoveryUri;
    private String aadMembershipRestUri;
}
