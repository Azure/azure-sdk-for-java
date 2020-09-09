// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

@Component
public class FeatureFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureFilter.class);

    @Autowired
    private FeatureManager featureManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (!featureManager.isEnabledAsync("Beta").block()) {
            chain.doFilter(request, response);
            return;
        }
        LOGGER.info("Run the Beta filter");
        chain.doFilter(request, response);
    }
}
