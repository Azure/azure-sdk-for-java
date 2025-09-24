import { describe, it, expect } from "vitest";
import { isManagementPlaneModule } from "./clean-java-source.js";

describe("clean-java-source", () => {
    describe("isManagementPlaneModule", () => {
        it("should be defined", () => {
            expect(isManagementPlaneModule).toBeDefined();
            expect(typeof isManagementPlaneModule).toBe("function");
        });

        it("should return true for management plane modules", () => {
            // Test with various management plane module names
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager-compute")).toBe(true);
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager-storage")).toBe(true);
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager-network")).toBe(true);
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager-keyvault")).toBe(true);
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager-appservice")).toBe(true);
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager-sql")).toBe(true);
        });

        it("should return true for management plane modules with complex paths", () => {
            // Test with Windows-style paths
            expect(
                isManagementPlaneModule(
                    "C:\\projects\\azure-sdk-for-java\\sdk\\resourcemanager\\azure-resourcemanager-compute",
                ),
            ).toBe(true);

            // Test with Unix-style paths
            expect(
                isManagementPlaneModule(
                    "/home/user/projects/azure-sdk-for-java/sdk/resourcemanager/azure-resourcemanager-storage",
                ),
            ).toBe(true);

            // Test with relative paths
            expect(isManagementPlaneModule("./sdk/resourcemanager/azure-resourcemanager-network")).toBe(true);
            expect(isManagementPlaneModule("../azure-resourcemanager-keyvault")).toBe(true);
        });

        it("should return false for data plane modules", () => {
            // Test with various data plane module names
            expect(isManagementPlaneModule("/path/to/azure-storage-blob")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-keyvault-secrets")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-messaging-servicebus")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-core")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-identity")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-cosmos")).toBe(false);
        });

        it("should return false for core and common modules", () => {
            expect(isManagementPlaneModule("/path/to/azure-core")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-core-http-netty")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-core-test")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-common")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-client-core")).toBe(false);
        });

        it("should return false for Spring modules", () => {
            expect(isManagementPlaneModule("/path/to/azure-spring-boot")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-spring-cloud")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-spring-data-cosmos")).toBe(false);
        });

        it("should return false for modules with similar but incorrect prefixes", () => {
            // Test modules that might look similar but don't match the exact prefix
            expect(isManagementPlaneModule("/path/to/azure-resource-manager")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager")).toBe(false); // Missing hyphen
            expect(isManagementPlaneModule("/path/to/resourcemanager-azure")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-resourcemanager_compute")).toBe(false); // Underscore instead of hyphen
            expect(isManagementPlaneModule("/path/to/azure-resourcemanagers-compute")).toBe(false); // Extra 's'
        });

        it("should handle edge cases", () => {
            // Test with empty or minimal paths
            expect(isManagementPlaneModule("azure-resourcemanager-compute")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-compute/")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-")).toBe(true); // Ends with prefix
            expect(isManagementPlaneModule("azure-resourcemanager")).toBe(false); // Exact prefix without hyphen

            // Test with just the module name
            expect(isManagementPlaneModule("azure-resourcemanager-test")).toBe(true);
            expect(isManagementPlaneModule("some-other-module")).toBe(false);
        });

        it("should be case sensitive", () => {
            // Management plane modules should be lowercase
            expect(isManagementPlaneModule("/path/to/Azure-ResourceManager-Compute")).toBe(false);
            expect(isManagementPlaneModule("/path/to/AZURE-RESOURCEMANAGER-COMPUTE")).toBe(false);
            expect(isManagementPlaneModule("/path/to/azure-ResourceManager-compute")).toBe(false);
        });

        it("should handle typical Azure SDK module names", () => {
            // Test with actual module names from the Azure SDK
            expect(isManagementPlaneModule("azure-resourcemanager-authorization")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-cognitiveservices")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-containerinstance")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-containerregistry")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-containerservice")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-cosmosdb")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-dns")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-eventhubs")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-monitor")).toBe(true);
            expect(isManagementPlaneModule("azure-resourcemanager-resources")).toBe(true);
        });
    });
});
