package com.microsoft.azure.utils;

import com.microsoft.azure.configuration.builder.Builder.Registry;

public class Exports implements
		com.microsoft.azure.configuration.builder.Builder.Exports {

	public void register(Registry registry) {
		registry.add(Clock.class, DefaultClock.class);

	}

}
