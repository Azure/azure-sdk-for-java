package com.microsoft.azure.management.resources.fluentcore.arm;

import com.microsoft.azure.management.resources.fluentcore.utils.WrappedList;

import java.util.List;

public class ResourceLazyList<ResourceT> extends WrappedList<String, ResourceT> {
    public ResourceLazyList(List<String> resourceIds, final Loader<ResourceT> loader)  {
        super(resourceIds, new Transformer<String, ResourceT>() {
            @Override
            public ResourceT transform(String id) {
                try {
                    return loader.load(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
    }

    public interface Loader<ResourceT> {
        ResourceT load(String resourceGroupName, String resourceName) throws Exception;
    }
}
