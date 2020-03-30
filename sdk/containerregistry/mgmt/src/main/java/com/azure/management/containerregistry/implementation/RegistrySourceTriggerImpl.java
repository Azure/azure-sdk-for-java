/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.AuthInfo;
import com.azure.management.containerregistry.AuthInfoUpdateParameters;
import com.azure.management.containerregistry.RegistrySourceTrigger;
import com.azure.management.containerregistry.RegistryTask;
import com.azure.management.containerregistry.SourceControlType;
import com.azure.management.containerregistry.SourceProperties;
import com.azure.management.containerregistry.SourceTrigger;
import com.azure.management.containerregistry.SourceTriggerEvent;
import com.azure.management.containerregistry.SourceTriggerUpdateParameters;
import com.azure.management.containerregistry.SourceUpdateParameters;
import com.azure.management.containerregistry.TokenType;
import com.azure.management.containerregistry.TriggerStatus;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.ArrayList;
import java.util.List;

class RegistrySourceTriggerImpl implements
        RegistrySourceTrigger,
        RegistrySourceTrigger.Definition,
        RegistrySourceTrigger.Update,
        RegistrySourceTrigger.UpdateDefinition,
        HasInner<SourceTrigger> {
    private SourceTrigger inner;
    private RegistryTaskImpl registryTaskImpl;
    private SourceTriggerUpdateParameters sourceTriggerUpdateParameters;

    RegistrySourceTriggerImpl(String sourceTriggerName, RegistryTaskImpl registryTaskImpl, boolean creation) {
        if (creation) {
            this.registryTaskImpl = registryTaskImpl;
            if (registryTaskImpl.inner().getId() == null) {
                this.inner = new SourceTrigger();
                this.inner.withSourceRepository(new SourceProperties());
                this.inner.withName(sourceTriggerName);
            } else {
                this.sourceTriggerUpdateParameters = new SourceTriggerUpdateParameters();
                this.sourceTriggerUpdateParameters.withSourceRepository(new SourceUpdateParameters());
                this.sourceTriggerUpdateParameters.withName(sourceTriggerName);
            }
        } else {
            this.registryTaskImpl = registryTaskImpl;
            this.inner = new SourceTrigger();
            this.inner.withSourceRepository(new SourceProperties());

            boolean foundSourceTrigger = false;
            for (SourceTriggerUpdateParameters stup : registryTaskImpl.taskUpdateParameters.trigger().sourceTriggers()) {
                if (stup.name().equals(sourceTriggerName)) {
                    this.sourceTriggerUpdateParameters = stup;
                    foundSourceTrigger = true;
                }
            }

            if (!foundSourceTrigger) {
                throw new IllegalArgumentException("The trigger you are trying to update does not exist. If you are trying to define a new trigger while updating a task, please use the defineSourceTrigger function instead.");
            }
        }
    }

    @Override
    public SourceControlType sourceControlType() {
        return this.inner.sourceRepository().sourceControlType();
    }

    @Override
    public String sourceControlRepositoryUrl() {
        return this.inner.sourceRepository().repositoryUrl();
    }

    @Override
    public List<SourceTriggerEvent> sourceTriggerEvents() {
        return this.inner.sourceTriggerEvents();
    }

    @Override
    public String sourceControlBranch() {
        return this.inner.sourceRepository().branch();
    }

    @Override
    public TriggerStatus status() {
        return this.inner.status();
    }

    @Override
    public RegistrySourceTriggerImpl withGithubAsSourceControl() {
        if (isInCreateMode()) {
            this.inner.sourceRepository().withSourceControlType(SourceControlType.GITHUB);
        } else {
            this.sourceTriggerUpdateParameters.sourceRepository().withSourceControlType(SourceControlType.GITHUB);
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withAzureDevOpsAsSourceControl() {
        if (isInCreateMode()) {
            this.inner.sourceRepository().withSourceControlType(SourceControlType.VISUAL_STUDIO_TEAM_SERVICE);
        } else {
            this.sourceTriggerUpdateParameters.sourceRepository().withSourceControlType(SourceControlType.VISUAL_STUDIO_TEAM_SERVICE);
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withSourceControl(SourceControlType sourceControl) {
        if (isInCreateMode()) {
            this.inner.sourceRepository().withSourceControlType(SourceControlType.fromString(sourceControl.toString()));
        } else {
            this.sourceTriggerUpdateParameters.sourceRepository().withSourceControlType(SourceControlType.fromString(sourceControl.toString()));
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withSourceControlRepositoryUrl(String sourceControlRepositoryUrl) {
        if (isInCreateMode()) {
            this.inner.sourceRepository().withRepositoryUrl(sourceControlRepositoryUrl);
        } else {
            this.sourceTriggerUpdateParameters.sourceRepository().withRepositoryUrl(sourceControlRepositoryUrl);
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withRepositoryBranch(String branch) {
        if (isInCreateMode()) {
            this.inner.sourceRepository().withBranch(branch);
        } else {
            this.sourceTriggerUpdateParameters.sourceRepository().withBranch(branch);
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withRepositoryAuthentication(TokenType tokenType, String token) {
        if (isInCreateMode()) {
            AuthInfo authInfo = new AuthInfo()
                    .withTokenType(tokenType)
                    .withToken(token);
            this.inner.sourceRepository().withSourceControlAuthProperties(authInfo);
        } else {
            AuthInfoUpdateParameters authInfoUpdateParameters = new AuthInfoUpdateParameters()
                    .withTokenType(tokenType)
                    .withToken(token);
            this.sourceTriggerUpdateParameters.sourceRepository().withSourceControlAuthProperties(authInfoUpdateParameters);
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withRepositoryAuthentication(TokenType tokenType, String token, String refreshToken, String scope, int expiresIn) {
        if (isInCreateMode()) {
            AuthInfo authInfo = new AuthInfo()
                    .withTokenType(tokenType)
                    .withToken(token)
                    .withRefreshToken(refreshToken)
                    .withScope(scope)
                    .withExpiresIn(expiresIn);
            this.inner.sourceRepository().withSourceControlAuthProperties(authInfo);
        } else {
            AuthInfoUpdateParameters authInfoUpdateParameters = new AuthInfoUpdateParameters()
                    .withTokenType(tokenType)
                    .withToken(token)
                    .withRefreshToken(refreshToken)
                    .withScope(scope)
                    .withExpiresIn(expiresIn);
            this.sourceTriggerUpdateParameters.sourceRepository().withSourceControlAuthProperties(authInfoUpdateParameters);
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withCommitTriggerEvent() {
        return this.withTriggerEvent(SourceTriggerEvent.COMMIT);
    }

    @Override
    public RegistrySourceTriggerImpl withPullTriggerEvent() {
        return this.withTriggerEvent(SourceTriggerEvent.PULLREQUEST);
    }

    @Override
    public RegistrySourceTriggerImpl withTriggerEvent(SourceTriggerEvent sourceTriggerEvent) {
        if (this.inner != null) {
            if (this.inner.sourceTriggerEvents() == null) {
                this.inner.withSourceTriggerEvents(new ArrayList<SourceTriggerEvent>());
            }
            List<SourceTriggerEvent> sourceTriggerEvents = this.inner.sourceTriggerEvents();
            if (sourceTriggerEvents.contains(sourceTriggerEvent)) {
                return this;
            }
            sourceTriggerEvents.add(SourceTriggerEvent.fromString(sourceTriggerEvent.toString()));
            if (isInCreateMode()) {
                this.inner.withSourceTriggerEvents(sourceTriggerEvents);
            } else {
                this.sourceTriggerUpdateParameters.withSourceTriggerEvents(sourceTriggerEvents);
            }
        } else {
            if (this.sourceTriggerUpdateParameters.sourceTriggerEvents() == null) {
                this.sourceTriggerUpdateParameters.withSourceTriggerEvents(new ArrayList<SourceTriggerEvent>());
            }
            List<SourceTriggerEvent> sourceTriggerEvents = this.sourceTriggerUpdateParameters.sourceTriggerEvents();
            if (sourceTriggerEvents.contains(sourceTriggerEvent)) {
                return this;
            }
            sourceTriggerEvents.add(SourceTriggerEvent.fromString(sourceTriggerEvent.toString()));
            this.sourceTriggerUpdateParameters.withSourceTriggerEvents(sourceTriggerEvents);
        }
        return this;
    }

    @Override
    public RegistrySourceTriggerImpl withTriggerStatusEnabled() {
        return this.withTriggerStatus(TriggerStatus.ENABLED);
    }

    @Override
    public RegistrySourceTriggerImpl withTriggerStatusDisabled() {
        return this.withTriggerStatus(TriggerStatus.DISABLED);
    }

    @Override
    public RegistrySourceTriggerImpl withTriggerStatus(TriggerStatus triggerStatus) {
        if (isInCreateMode()) {
            this.inner.withStatus(TriggerStatus.fromString(triggerStatus.toString()));
        } else {
            this.sourceTriggerUpdateParameters.withStatus(TriggerStatus.fromString(triggerStatus.toString()));
        }
        return this;
    }

    @Override
    public RegistryTaskImpl attach() {
        if (isInCreateMode()) {
            this.registryTaskImpl.withSourceTriggerCreateParameters(this.inner);
        } else {
            this.registryTaskImpl.withSourceTriggerUpdateParameters(this.sourceTriggerUpdateParameters);
        }
        return this.registryTaskImpl;
    }

    @Override
    public SourceTrigger inner() {
        return this.inner;
    }

    private boolean isInCreateMode() {
        if (this.registryTaskImpl.inner().getId() == null) {
            return true;
        }
        return false;
    }

    @Override
    public RegistryTask.Update parent() {
        return this.registryTaskImpl;
    }
}
