package com.microsoft.windowsazure.auth.wrap;

import com.microsoft.windowsazure.auth.wrap.contract.WrapContract;
import com.microsoft.windowsazure.auth.wrap.contract.WrapContractImpl;
import com.microsoft.windowsazure.configuration.builder.Builder.Registry;

public class Exports implements
        com.microsoft.windowsazure.configuration.builder.Builder.Exports {

    public void register(Registry registry) {
        registry.add(WrapContract.class, WrapContractImpl.class);
        registry.add(WrapClient.class);
        registry.add(WrapFilter.class);
    }

}
