// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeprecateApplicationGatewaySku {

    @Test
    public void deprecateApplicationGatewaySku() throws Exception {
        Path modulePath = Paths.get(this.getClass().getResource("/junit-platform.properties").toURI())
            .getParent()
            .getParent()
            .getParent();
        Path modelsPath = Paths.get(modulePath.toString(), "src/main/java/com/azure/resourcemanager/network/models");
        Path skuNameFile = Paths.get(modelsPath.toString(), "ApplicationGatewaySkuName.java");
        Path tierFile = Paths.get(modelsPath.toString(), "ApplicationGatewayTier.java");

        Map<String, String> skuReplacementMap = new HashMap<>();
        skuReplacementMap.put("    /** Static value Standard_Small for ApplicationGatewaySkuName. */", "    /**\n"
            + "     * Static value Standard_Small for ApplicationGatewaySkuName.\n" + "     *\n"
            + "     * @deprecated Application Gateway V1 is officially deprecated on April 28, 2023.\n"
            + "     *             See <a href=\"https://learn.microsoft.com/azure/application-gateway/v1-retirement#retirement-timelines\">v1-retirement-timeline</a>\n"
            + "     *             for V1 retirement timeline and start planning your migration to Application Gateway V2 today.\n"
            + "     */\n" + "    @Deprecated");
        skuReplacementMap.put("    /** Static value Standard_Medium for ApplicationGatewaySkuName. */", "    /**\n"
            + "     * Static value Standard_Medium for ApplicationGatewaySkuName.\n" + "     *\n"
            + "     * @deprecated Application Gateway V1 is officially deprecated on April 28, 2023.\n"
            + "     *             See <a href=\"https://learn.microsoft.com/azure/application-gateway/v1-retirement#retirement-timelines\">v1-retirement-timeline</a>\n"
            + "     *             for V1 retirement timeline and start planning your migration to Application Gateway V2 today.\n"
            + "     */\n" + "    @Deprecated");
        skuReplacementMap.put("    /** Static value Standard_Large for ApplicationGatewaySkuName. */", "    /**\n"
            + "     * Static value Standard_Large for ApplicationGatewaySkuName.\n" + "     *\n"
            + "     * @deprecated Application Gateway V1 is officially deprecated on April 28, 2023.\n"
            + "     *             See <a href=\"https://learn.microsoft.com/azure/application-gateway/v1-retirement#retirement-timelines\">v1-retirement-timeline</a>\n"
            + "     *             for V1 retirement timeline and start planning your migration to Application Gateway V2 today.\n"
            + "     */\n" + "    @Deprecated");
        skuReplacementMap.put("    /** Static value WAF_Medium for ApplicationGatewaySkuName. */", "    /**\n"
            + "     * Static value WAF_Medium for ApplicationGatewaySkuName.\n" + "     *\n"
            + "     * @deprecated Application Gateway V1 is officially deprecated on April 28, 2023.\n"
            + "     *             See <a href=\"https://learn.microsoft.com/azure/application-gateway/v1-retirement#retirement-timelines\">v1-retirement-timeline</a>\n"
            + "     *             for V1 retirement timeline and start planning your migration to Application Gateway V2 today.\n"
            + "     */\n" + "    @Deprecated");
        skuReplacementMap.put("    /** Static value WAF_Large for ApplicationGatewaySkuName. */", "    /**\n"
            + "     * Static value WAF_Large for ApplicationGatewaySkuName.\n" + "     *\n"
            + "     * @deprecated Application Gateway V1 is officially deprecated on April 28, 2023.\n"
            + "     *             See <a href=\"https://learn.microsoft.com/azure/application-gateway/v1-retirement#retirement-timelines\">v1-retirement-timeline</a>\n"
            + "     *             for V1 retirement timeline and start planning your migration to Application Gateway V2 today.\n"
            + "     */\n" + "    @Deprecated");

        Map<String, String> tierReplacementMap = new HashMap<>();
        tierReplacementMap.put("    /** Static value Standard for ApplicationGatewayTier. */", "    /**\n"
            + "     * Static value Standard for ApplicationGatewayTier.\n" + "     *\n"
            + "     * @deprecated Application Gateway V1 is officially deprecated on April 28, 2023.\n"
            + "     *             See <a href=\"https://learn.microsoft.com/azure/application-gateway/v1-retirement#retirement-timelines\">v1-retirement-timeline</a>\n"
            + "     *             for V1 retirement timeline and start planning your migration to Application Gateway V2 today.\n"
            + "     */\n" + "    @Deprecated");
        tierReplacementMap.put("    /** Static value WAF for ApplicationGatewayTier. */", "    /**\n"
            + "     * Static value WAF for ApplicationGatewayTier.\n" + "     *\n"
            + "     * @deprecated Application Gateway V1 is officially deprecated on April 28, 2023.\n"
            + "     *             See <a href=\"https://learn.microsoft.com/azure/application-gateway/v1-retirement#retirement-timelines\">v1-retirement-timeline</a>\n"
            + "     *             for V1 retirement timeline and start planning your migration to Application Gateway V2 today.\n"
            + "     */\n" + "    @Deprecated");

        replaceInFile(skuNameFile, skuReplacementMap);
        replaceInFile(tierFile, tierReplacementMap);
    }

    private static void replaceInFile(Path filename, Map<String, String> replacementMap) throws IOException {
        List<String> newLines = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(filename.toString()), StandardCharsets.UTF_8)) {
            List<String> replacementLines = null;
            for (Map.Entry<String, String> e : replacementMap.entrySet()) {
                if (line.equals(e.getKey())) {
                    replacementLines = Arrays.stream(e.getValue().split("\n")).collect(Collectors.toList());
                    break;
                }
            }

            if (replacementLines != null) {
                newLines.addAll(replacementLines);
            } else {
                newLines.add(line);
            }
        }
        Files.write(Paths.get(filename.toString()), newLines, StandardCharsets.UTF_8);
    }
}
