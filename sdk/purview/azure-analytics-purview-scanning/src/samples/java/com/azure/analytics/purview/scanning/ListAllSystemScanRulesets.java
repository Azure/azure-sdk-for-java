package com.azure.analytics.purview.scanning;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class ListAllSystemScanRulesets {
    public static void main(String[] args) {
        SystemScanRulesetsClient client = new PurviewScanningClientBuilder()
            .endpoint(System.getenv("SCANNING_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSystemScanRulesetsClient();
        PagedIterable<BinaryData> response = client.listAll(null);
        List<BinaryData> list = response.stream().collect(Collectors.toList());
        System.out.println(list);
    }
}
