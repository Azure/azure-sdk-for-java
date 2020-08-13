// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureGate;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;
import com.microsoft.azure.spring.cloud.feature.manager.FeatureManagerSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ConfigurationProperties("controller")
public class HelloController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private FeatureManager featureManager;

    @Autowired
    private FeatureManagerSnapshot featureManagerSnapshot;

    @GetMapping("/privacy")
    public String getRequestBased(Model model) {
        model.addAttribute("Beta", featureManager.isEnabledAsync("beta").block());
        model.addAttribute("isDarkThemeS1", featureManagerSnapshot.isEnabledAsync("dark-theme").block());
        model.addAttribute("isDarkThemeS2", featureManagerSnapshot.isEnabledAsync("dark-theme").block());
        model.addAttribute("isDarkThemeS3", featureManagerSnapshot.isEnabledAsync("dark-theme").block());
        return "privacy";
    }

    @GetMapping(value = {"/Beta", "/BetaA"})
    @FeatureGate(feature = "beta-ab", fallback = "/BetaB")
    public String getRedirect(Model model) {
        return "BetaA";
    }

    @GetMapping("/BetaB")
    public String getRedirected(Model model) {
        return "BetaB";
    }

    @GetMapping(value = {"", "/", "/welcome"})
    public String mainWithParam(Model model) {
        model.addAttribute("Beta", featureManager.isEnabledAsync("beta").block());
        model.addAttribute("Target", String.valueOf(featureManager.isEnabledAsync("target").block()));
        return "welcome";
    }
}
