/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.functions.Func1;

/**
 * Base class for creatable wrapper collections, i.e. those where a new member of the collection can be created.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 */
public abstract class CreatableWrappersImpl<T, ImplT extends T, InnerT>
    extends ReadableWrappersImpl<T, ImplT, InnerT>
    implements
        // Assume anything creatable is deletable
        SupportsDeleting {

    protected CreatableWrappersImpl() {
    }

    protected abstract ImplT wrapModel(String name);

    @Override
    public void delete(String id) {
        deleteAsync(id).toBlocking().subscribe();
    }

    @Override
    public ServiceCall<Void> deleteAsync(String id, ServiceCallback<Void> callback) {
        return ServiceCall.create(deleteAsync(id).map(new Func1<Void, ServiceResponse<Void>>() {
            @Override
            public ServiceResponse<Void> call(Void aVoid) {
                return new ServiceResponse<>(aVoid, null);
            }
        }), callback);
    }
}
