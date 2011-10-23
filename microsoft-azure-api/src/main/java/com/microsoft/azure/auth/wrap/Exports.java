package com.microsoft.azure.auth.wrap;

import com.microsoft.azure.auth.wrap.contract.WrapContract;
import com.microsoft.azure.auth.wrap.contract.WrapContractImpl;
import com.microsoft.azure.configuration.builder.Builder.Registry;

public class Exports implements
		com.microsoft.azure.configuration.builder.Builder.Exports {

	public void register(Registry registry) {
		registry.add(WrapContract.class, WrapContractImpl.class);
		registry.add(WrapClient.class);
		registry.add(WrapFilter.class);
	}

}
