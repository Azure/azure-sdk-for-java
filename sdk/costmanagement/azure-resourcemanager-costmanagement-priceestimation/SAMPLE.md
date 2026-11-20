# Code snippets and samples


## Operations

- [List](#operations_list)

## PriceEstimation

- [EstimatePrices](#priceestimation_estimateprices)
- [GetEstimatePricesOperationResult](#priceestimation_getestimatepricesoperationresult)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/OperationsList.json
     */
    /**
     * Sample code: List price estimation provider operation metadata.
     * 
     * @param manager Entry point to CostManagementPriceEstimationManager.
     */
    public static void listPriceEstimationProviderOperationMetadata(
        com.azure.resourcemanager.costmanagement.priceestimation.CostManagementPriceEstimationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PriceEstimation_EstimatePrices

```java
import com.azure.resourcemanager.costmanagement.priceestimation.models.EstimateArrayValue;
import com.azure.resourcemanager.costmanagement.priceestimation.models.EstimateContent;
import com.azure.resourcemanager.costmanagement.priceestimation.models.EstimateNamedValue;
import com.azure.resourcemanager.costmanagement.priceestimation.models.EstimateNumberValue;
import com.azure.resourcemanager.costmanagement.priceestimation.models.EstimateObjectValue;
import com.azure.resourcemanager.costmanagement.priceestimation.models.EstimateResourceInput;
import com.azure.resourcemanager.costmanagement.priceestimation.models.EstimateStringValue;
import java.util.Arrays;

/**
 * Samples for PriceEstimation EstimatePrices.
 */
public final class PriceEstimationEstimatePricesSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/EstimatePricesVmAndDisk.json
     */
    /**
     * Sample code: Estimate a Linux VM and a separately counted premium disk.
     * 
     * @param manager Entry point to CostManagementPriceEstimationManager.
     */
    public static void estimateALinuxVMAndASeparatelyCountedPremiumDisk(
        com.azure.resourcemanager.costmanagement.priceestimation.CostManagementPriceEstimationManager manager) {
        manager.priceEstimations()
            .estimatePrices(
                new EstimateContent()
                    .withResources(
                        Arrays
                            .asList(
                                new EstimateResourceInput().withResourceType("Microsoft.Compute/virtualMachines")
                                    .withResourceApiVersion("2024-07-01")
                                    .withInputs(
                                        Arrays.asList(
                                            new EstimateNamedValue().withName("resource")
                                                .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                    new EstimateNamedValue().withName("location")
                                                        .withValue(new EstimateStringValue().withValue("eastus")),
                                                    new EstimateNamedValue().withName("properties")
                                                        .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                            new EstimateNamedValue().withName("hardwareProfile")
                                                                .withValue(new EstimateObjectValue()
                                                                    .withMembers(Arrays.asList(new EstimateNamedValue()
                                                                        .withName("vmSize")
                                                                        .withValue(new EstimateStringValue()
                                                                            .withValue("Standard_D2s_v5"))))),
                                                            new EstimateNamedValue().withName("storageProfile")
                                                                .withValue(new EstimateObjectValue().withMembers(Arrays
                                                                    .asList(new EstimateNamedValue().withName("osDisk")
                                                                        .withValue(
                                                                            new EstimateObjectValue().withMembers(
                                                                                Arrays.asList(new EstimateNamedValue()
                                                                                    .withName("osType")
                                                                                    .withValue(new EstimateStringValue()
                                                                                        .withValue(
                                                                                            "Linux")))))))))))))),
                                            new EstimateNamedValue().withName("usage")
                                                .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                    new EstimateNamedValue().withName("purchaseOption")
                                                        .withValue(new EstimateStringValue().withValue("PayAsYouGo")),
                                                    new EstimateNamedValue().withName("instanceCount")
                                                        .withValue(new EstimateNumberValue().withValue(1.0)),
                                                    new EstimateNamedValue().withName("hoursPerMonth")
                                                        .withValue(new EstimateNumberValue().withValue(730.0)),
                                                    new EstimateNamedValue().withName("bandwidth")
                                                        .withValue(
                                                            new EstimateArrayValue().withItems(Arrays.asList()))))))),
                                new EstimateResourceInput().withResourceType("Microsoft.Compute/disks")
                                    .withResourceApiVersion("2024-03-02")
                                    .withInputs(
                                        Arrays.asList(
                                            new EstimateNamedValue().withName("resource")
                                                .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                    new EstimateNamedValue().withName("location")
                                                        .withValue(new EstimateStringValue().withValue("eastus")),
                                                    new EstimateNamedValue().withName("sku")
                                                        .withValue(new EstimateObjectValue().withMembers(
                                                            Arrays.asList(new EstimateNamedValue().withName("name")
                                                                .withValue(new EstimateStringValue()
                                                                    .withValue("Premium_LRS"))))),
                                                    new EstimateNamedValue().withName("properties")
                                                        .withValue(
                                                            new EstimateObjectValue().withMembers(Arrays
                                                                .asList(new EstimateNamedValue().withName("diskSizeGB")
                                                                    .withValue(new EstimateNumberValue()
                                                                        .withValue(128.0)))))))),
                                            new EstimateNamedValue().withName("usage")
                                                .withValue(
                                                    new EstimateObjectValue().withMembers(Arrays.asList(
                                                        new EstimateNamedValue().withName("count")
                                                            .withValue(new EstimateNumberValue().withValue(1.0)),
                                                        new EstimateNamedValue().withName("purchaseOption")
                                                            .withValue(new EstimateStringValue()
                                                                .withValue("PayAsYouGo"))))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/EstimatePricesPartialPrice.json
     */
    /**
     * Sample code: Report a component whose price is unavailable without treating it as zero.
     * 
     * @param manager Entry point to CostManagementPriceEstimationManager.
     */
    public static void reportAComponentWhosePriceIsUnavailableWithoutTreatingItAsZero(
        com.azure.resourcemanager.costmanagement.priceestimation.CostManagementPriceEstimationManager manager) {
        manager.priceEstimations()
            .estimatePrices(
                new EstimateContent()
                    .withResources(
                        Arrays
                            .asList(
                                new EstimateResourceInput().withResourceType("Microsoft.Compute/virtualMachines")
                                    .withResourceApiVersion("2024-07-01")
                                    .withInputs(
                                        Arrays.asList(
                                            new EstimateNamedValue().withName("resource")
                                                .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                    new EstimateNamedValue().withName("location")
                                                        .withValue(new EstimateStringValue().withValue("eastus")),
                                                    new EstimateNamedValue().withName("properties")
                                                        .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                            new EstimateNamedValue().withName("hardwareProfile")
                                                                .withValue(new EstimateObjectValue()
                                                                    .withMembers(Arrays.asList(new EstimateNamedValue()
                                                                        .withName("vmSize")
                                                                        .withValue(new EstimateStringValue()
                                                                            .withValue("Standard_D2s_v5"))))),
                                                            new EstimateNamedValue().withName("storageProfile")
                                                                .withValue(new EstimateObjectValue().withMembers(Arrays
                                                                    .asList(new EstimateNamedValue().withName("osDisk")
                                                                        .withValue(
                                                                            new EstimateObjectValue().withMembers(
                                                                                Arrays.asList(new EstimateNamedValue()
                                                                                    .withName("osType")
                                                                                    .withValue(new EstimateStringValue()
                                                                                        .withValue(
                                                                                            "Linux")))))))))))))),
                                            new EstimateNamedValue().withName("usage")
                                                .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                    new EstimateNamedValue().withName("purchaseOption")
                                                        .withValue(new EstimateStringValue().withValue("PayAsYouGo")),
                                                    new EstimateNamedValue().withName("instanceCount")
                                                        .withValue(new EstimateNumberValue().withValue(1.0)),
                                                    new EstimateNamedValue().withName("hoursPerMonth")
                                                        .withValue(new EstimateNumberValue().withValue(730.0)),
                                                    new EstimateNamedValue().withName("bandwidth")
                                                        .withValue(
                                                            new EstimateArrayValue().withItems(Arrays.asList()))))))),
                                new EstimateResourceInput().withResourceType("Microsoft.Compute/disks")
                                    .withResourceApiVersion("2024-03-02")
                                    .withInputs(
                                        Arrays.asList(
                                            new EstimateNamedValue().withName("resource")
                                                .withValue(new EstimateObjectValue().withMembers(Arrays.asList(
                                                    new EstimateNamedValue().withName("location")
                                                        .withValue(new EstimateStringValue().withValue("eastus")),
                                                    new EstimateNamedValue().withName("sku")
                                                        .withValue(new EstimateObjectValue().withMembers(
                                                            Arrays.asList(new EstimateNamedValue().withName("name")
                                                                .withValue(new EstimateStringValue()
                                                                    .withValue("Premium_LRS"))))),
                                                    new EstimateNamedValue().withName("properties")
                                                        .withValue(
                                                            new EstimateObjectValue().withMembers(Arrays
                                                                .asList(new EstimateNamedValue().withName("diskSizeGB")
                                                                    .withValue(new EstimateNumberValue()
                                                                        .withValue(128.0)))))))),
                                            new EstimateNamedValue().withName("usage")
                                                .withValue(
                                                    new EstimateObjectValue().withMembers(Arrays.asList(
                                                        new EstimateNamedValue().withName("count")
                                                            .withValue(new EstimateNumberValue().withValue(1.0)),
                                                        new EstimateNamedValue().withName("purchaseOption")
                                                            .withValue(new EstimateStringValue()
                                                                .withValue("PayAsYouGo"))))))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### PriceEstimation_GetEstimatePricesOperationResult

```java
/**
 * Samples for PriceEstimation GetEstimatePricesOperationResult.
 */
public final class PriceEstimationGetEstimatePricesOperationResultSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/EstimatePricesOperationResultGet.json
     */
    /**
     * Sample code: Get the result of a price estimation operation.
     * 
     * @param manager Entry point to CostManagementPriceEstimationManager.
     */
    public static void getTheResultOfAPriceEstimationOperation(
        com.azure.resourcemanager.costmanagement.priceestimation.CostManagementPriceEstimationManager manager) {
        manager.priceEstimations()
            .getEstimatePricesOperationResult("2f0d4b7a9c1e4d6f8a3b5c7d9e1f2a4b", com.azure.core.util.Context.NONE);
    }
}
```

