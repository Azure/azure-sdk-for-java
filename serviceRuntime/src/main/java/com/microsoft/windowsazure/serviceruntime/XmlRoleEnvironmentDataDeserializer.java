/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * 
 */
class XmlRoleEnvironmentDataDeserializer implements
        RoleEnvironmentDataDeserializer {
    public XmlRoleEnvironmentDataDeserializer() {
    }

    @Override
    public RoleEnvironmentData deserialize(InputStream stream) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(RoleEnvironmentInfo.class.getPackage()
                            .getName());
            Unmarshaller unmarshaller = context.createUnmarshaller();

            @SuppressWarnings("unchecked")
            RoleEnvironmentInfo environmentInfo = ((JAXBElement<RoleEnvironmentInfo>) unmarshaller
                    .unmarshal(stream)).getValue();

            Map<String, String> configurationSettings = translateConfigurationSettings(environmentInfo);
            Map<String, LocalResource> localResources = translateLocalResources(environmentInfo);
            RoleInstance currentInstance = translateCurrentInstance(environmentInfo);
            Map<String, Role> roles = translateRoles(environmentInfo,
                    currentInstance, environmentInfo.getCurrentInstance()
                            .getRoleName());

            return new RoleEnvironmentData(environmentInfo.getDeployment()
                    .getId(), configurationSettings, localResources,
                    currentInstance, roles, environmentInfo.getDeployment()
                            .isEmulated());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> translateConfigurationSettings(
            RoleEnvironmentInfo environmentInfo) {
        Map<String, String> configurationSettings = new HashMap<String, String>();

        for (ConfigurationSettingInfo settingInfo : environmentInfo
                .getCurrentInstance().getConfigurationSettings()
                .getConfigurationSetting()) {
            configurationSettings.put(settingInfo.getName(),
                    settingInfo.getValue());
        }

        return configurationSettings;
    }

    private Map<String, LocalResource> translateLocalResources(
            RoleEnvironmentInfo environmentInfo) {
        Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();

        for (LocalResourceInfo resourceInfo : environmentInfo
                .getCurrentInstance().getLocalResources().getLocalResource()) {
            localResources.put(resourceInfo.getName(), new LocalResource(
                    resourceInfo.getSizeInMB(), resourceInfo.getName(),
                    resourceInfo.getPath()));
        }

        return localResources;
    }

    private Map<String, Role> translateRoles(
            RoleEnvironmentInfo environmentInfo, RoleInstance currentInstance,
            String currentRole) {
        Map<String, Role> roles = new HashMap<String, Role>();

        for (RoleInfo roleInfo : environmentInfo.getRoles().getRole()) {
            Map<String, RoleInstance> instances = translateRoleInstances(roleInfo
                    .getInstances());

            if (roleInfo.getName().equals(currentRole)) {
                instances.put(currentInstance.getId(), currentInstance);
            }

            Role role = new Role(roleInfo.getName(), instances);

            for (RoleInstance instance : role.getInstances().values()) {
                instance.setRole(role);
            }

            roles.put(roleInfo.getName(), role);
        }

        if (!roles.containsKey(currentRole)) {
            Map<String, RoleInstance> instances = new HashMap<String, RoleInstance>();

            instances.put(currentInstance.getId(), currentInstance);

            Role singleRole = new Role(currentRole, instances);

            currentInstance.setRole(singleRole);

            roles.put(currentRole, singleRole);
        }

        return roles;
    }

    private Map<String, RoleInstance> translateRoleInstances(
            RoleInstancesInfo instancesInfo) {
        Map<String, RoleInstance> roleInstances = new HashMap<String, RoleInstance>();

        for (RoleInstanceInfo instanceInfo : instancesInfo.getInstance()) {
            RoleInstance instance = new RoleInstance(instanceInfo.getId(),
                    instanceInfo.getFaultDomain(),
                    instanceInfo.getUpdateDomain(),
                    translateRoleInstanceEndpoints(instanceInfo.getEndpoints()));

            for (RoleInstanceEndpoint endpoint : instance
                    .getInstanceEndpoints().values()) {
                endpoint.setRoleInstance(instance);
            }

            roleInstances.put(instance.getId(), instance);
        }

        return roleInstances;
    }

    private Map<String, RoleInstanceEndpoint> translateRoleInstanceEndpoints(
            EndpointsInfo endpointsInfo) {
        Map<String, RoleInstanceEndpoint> endpoints = new HashMap<String, RoleInstanceEndpoint>();

        for (EndpointInfo endpointInfo : endpointsInfo.getEndpoint()) {
            RoleInstanceEndpoint endpoint = new RoleInstanceEndpoint(
                    endpointInfo.getProtocol().toString(),
                    new InetSocketAddress(endpointInfo.getAddress(),
                            endpointInfo.getPort()));

            endpoints.put(endpointInfo.getName(), endpoint);
        }

        return endpoints;
    }

    private RoleInstance translateCurrentInstance(
            RoleEnvironmentInfo environmentInfo) {
        CurrentRoleInstanceInfo currentInstanceInfo = environmentInfo
                .getCurrentInstance();
        RoleInstance currentInstance = new RoleInstance(
                currentInstanceInfo.getId(),
                currentInstanceInfo.getFaultDomain(),
                currentInstanceInfo.getUpdateDomain(),
                translateRoleInstanceEndpoints(environmentInfo
                        .getCurrentInstance().getEndpoints()));

        for (RoleInstanceEndpoint endpoint : currentInstance
                .getInstanceEndpoints().values()) {
            endpoint.setRoleInstance(currentInstance);
        }

        return currentInstance;
    }
}
