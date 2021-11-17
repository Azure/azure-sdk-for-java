// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.util;

import org.springframework.util.StringUtils;
import java.net.MalformedURLException;

/**
 * @author Zhihao Guo
 * A validator util for URL
 */
public class URLValidator {

    /**
     * Used to validate uri, the uri is allowed to be empty.
     * @param uri the uri to be validated.
     * @return whether is uri is valid or not.
     */
    public static boolean isValidURL(String uri) {
        if (!StringUtils.hasLength(uri)) {
            return true;
        }
        try {
            new java.net.URL(uri);
        } catch (MalformedURLException var) {
            return false;
        }
        return true;
    }
}
