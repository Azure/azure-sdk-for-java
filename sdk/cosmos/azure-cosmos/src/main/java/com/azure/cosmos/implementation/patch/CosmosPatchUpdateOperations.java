package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.ContainerDirectConnectionMetadata;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosPatchOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class CosmosPatchUpdateOperations {
    private final List<PatchOperation> patchOperations;

    private CosmosPatchUpdateOperations() {
        this.patchOperations = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Initializes a new instance of {@link CosmosPatchUpdateOperations} that will contain operations to be performed on a item atomically.
     *
     * @return A new instance of {@link CosmosPatchUpdateOperations}.
     */
    public static CosmosPatchUpdateOperations create() {
        return new CosmosPatchUpdateOperations();
    }

    /**
     * This sets the value at the target location with a new value.
     *
     * For the above JSON, we can have something like this:
     * <code>
     *     CosmosPatchOperations cosmosPatch = CosmosPatchOperations.create();
     *     cosmosPatch.set("/f", "new value"); // will add a new path "/f" and set it's value as "new value".
     *     cosmosPatch.set("/b/e", "bar"); // will set "/b/e" path to be "bar".
     * </code>
     *
     * This operation is idempotent as multiple execution will set the same value. If a new path is added, next time
     * same value will be set.
     *
     * @param <T> The type of item to be set.
     *
     * @param path the operation path.
     * @param value the value which will be set.
     *
     * @return same instance of {@link CosmosPatchOperations}
     */
    public <T> CosmosPatchUpdateOperations set(String path, T value) {

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);

        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.SET,
                path,
                value));

        return this;
    }

    // NOTE returning this patchOperations means any
    // modifications - like adding new entries is still
    // thread-safe - but enumerating over the collection is not
    // unless synchronized
    List<PatchOperation> getPatchOperations() {
        return this.patchOperations;
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosPatchUpdateOperationsHelper.setCosmosPatchUpdateOperationsAccessor(
            cosmosPatchUpdateOperations -> cosmosPatchUpdateOperations.getPatchOperations()
        );
    }

    static { initialize(); }
}
