// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.url;

enum UrlTokenizerState {
    SCHEME,

    SCHEME_OR_HOST,

    HOST,

    PORT,

    PATH,

    QUERY,

    DONE
}
