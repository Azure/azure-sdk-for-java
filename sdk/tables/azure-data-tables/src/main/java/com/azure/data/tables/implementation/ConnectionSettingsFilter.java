// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

/**
 * Type that represents contract for applying filtering on {@link ConnectionSettings}.
 */
@FunctionalInterface
interface ConnectionSettingsFilter {
    static ConnectionSettingsFilter allRequired(final String... settingNames) {
        return (ConnectionSettings inputSettings) -> {
            ConnectionSettings outputSettings = inputSettings.clone();

            for (final String settingName : settingNames) {
                if (outputSettings.hasSetting(settingName)) {
                    outputSettings.removeSetting(settingName);
                } else {
                    return null;
                }
            }

            return outputSettings;
        };
    }

    /**
     * Applies the filter function to the given {@link ConnectionSettings}.
     *
     * @param inputSettings The {@link ConnectionSettings input settings}.
     *
     * @return The result of filtering.
     */
    ConnectionSettings apply(ConnectionSettings inputSettings);

    static ConnectionSettingsFilter optional(final String... settingNames) {
        return (ConnectionSettings inputSettings) -> {
            ConnectionSettings outputSettings = inputSettings.clone();

            for (final String settingName : settingNames) {
                outputSettings.removeSetting(settingName);
            }

            return outputSettings;
        };
    }

    static ConnectionSettingsFilter atLeastOne(final String... settingNames) {
        return (ConnectionSettings inputSettings) -> {
            ConnectionSettings outputSettings = inputSettings.clone();
            boolean foundOne = false;

            for (final String settingName : settingNames) {
                if (outputSettings.hasSetting(settingName)) {
                    outputSettings.removeSetting(settingName);
                    foundOne = true;
                }
            }

            return foundOne ? outputSettings : null;
        };
    }

    static ConnectionSettingsFilter none(final String... settingNames) {
        return (ConnectionSettings inputSettings) -> {
            ConnectionSettings outputSettings = inputSettings.clone();
            boolean foundOne = false;

            for (final String settingName : settingNames) {
                if (outputSettings.hasSetting(settingName)) {
                    outputSettings.removeSetting(settingName);
                    foundOne = true;
                }
            }

            return foundOne ? null : outputSettings;
        };
    }

    static ConnectionSettingsFilter matchesAll(final ConnectionSettingsFilter... filters) {
        return (ConnectionSettings inputSettings) -> {
            ConnectionSettings outputSettings = inputSettings.clone();

            for (final ConnectionSettingsFilter filter : filters) {
                if (outputSettings == null) {
                    break;
                }

                outputSettings = filter.apply(outputSettings);
            }

            return outputSettings;
        };
    }

    static ConnectionSettingsFilter matchesOne(final ConnectionSettingsFilter... filters) {
        return (ConnectionSettings settings) -> {
            ConnectionSettings matchResult = null;

            for (final ConnectionSettingsFilter filter : filters) {
                ConnectionSettings result = filter.apply(settings.clone());

                if (result != null) {
                    if (matchResult == null) {
                        matchResult = result;
                    } else {
                        return null;
                    }
                }
            }

            return matchResult;
        };
    }

    static ConnectionSettingsFilter matchesExactly(final ConnectionSettingsFilter filter) {
        return (ConnectionSettings settings) -> {
            ConnectionSettings result = settings.clone();
            result = filter.apply(result);

            if (result == null || !result.isEmpty()) {
                return null;
            } else {
                return result;
            }
        };
    }

    static boolean matchesSpecification(ConnectionSettings settings,
                                        ConnectionSettingsFilter... constraints) {
        for (ConnectionSettingsFilter constraint : constraints) {
            ConnectionSettings remainingSettings = constraint.apply(settings);

            if (remainingSettings == null) {
                return false;
            } else {
                settings = remainingSettings;
            }
        }

        return settings.isEmpty();
    }
}
