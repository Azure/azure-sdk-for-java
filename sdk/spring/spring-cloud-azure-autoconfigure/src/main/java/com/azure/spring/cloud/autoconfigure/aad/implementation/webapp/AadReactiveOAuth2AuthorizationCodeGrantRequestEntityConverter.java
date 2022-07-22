// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;

public class AadReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter extends AbstractReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter {

    /**
     * Creates a new instance of {@link AadReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter}.
     *
     * @param entityConverter the converter.
     */
    public AadReactiveOAuth2AuthorizationCodeGrantRequestEntityConverter(OAuth2AuthorizationCodeGrantRequestEntityConverter entityConverter) {
        super(entityConverter);
    }

}
