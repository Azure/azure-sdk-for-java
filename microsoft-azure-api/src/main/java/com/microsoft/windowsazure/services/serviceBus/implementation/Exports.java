package com.microsoft.windowsazure.services.serviceBus.implementation;

import com.microsoft.windowsazure.common.Builder;

public class Exports implements Builder.Exports {

    public void register(Builder.Registry registry) {
        registry.add(WrapContract.class, WrapRestProxy.class);
        registry.add(WrapTokenManager.class);
        registry.add(WrapFilter.class);
    }

}
