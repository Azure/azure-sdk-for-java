package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.configuration.Configuration;

public class ServiceBusConfiguration {
	public final static String URI = "serviceBus.uri";
	public final static String WRAP_URI = "serviceBus.wrap.uri";
	public final static String WRAP_NAME = "serviceBus.wrap.name";
	public final static String WRAP_PASSWORD = "serviceBus.wrap.password";
	public final static String WRAP_SCOPE = "serviceBus.wrap.scope";

	public static Configuration configure(Configuration configuration,
			String namespace, String authenticationName,
			String authenticationPassword) {
		return configure(null, configuration, namespace, authenticationName,
				authenticationPassword);
	}

	public static Configuration configure(String profile, Configuration configuration,
			String namespace, String authenticationName,
			String authenticationPassword) {

		if (profile == null) {
			profile = "";
		} else if (profile.length() != 0 && !profile.endsWith(".")) {
			profile = profile + ".";
		}

		configuration.setProperty(profile + URI, "https://" + namespace
				+ ".servicebus.windows.net/");

		configuration.setProperty(profile + WRAP_URI, "https://" + namespace
				+ "-sb.accesscontrol.windows.net/WRAPv0.9");

		configuration.setProperty(profile + WRAP_NAME, authenticationName);
		configuration.setProperty(profile + WRAP_PASSWORD, authenticationPassword);
		configuration.setProperty(profile + WRAP_SCOPE, "http://" + namespace
				+ ".servicebus.windows.net/");

		return configuration;
	}
}
