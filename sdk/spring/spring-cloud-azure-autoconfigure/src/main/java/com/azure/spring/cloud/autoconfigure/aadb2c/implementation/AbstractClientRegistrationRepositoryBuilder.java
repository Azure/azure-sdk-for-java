package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractClientRegistrationRepositoryBuilder<O> implements ClientRegistrationRepositoryBuilder<O> {

    private final AtomicBoolean building = new AtomicBoolean();

    private final List<ClientRegistrationRepositoryConfigurerAdapter<O>> configurers = new ArrayList<>();

    private final List<ClientRegistration> allClientRegistrations = new ArrayList<>();

    private final List<ClientRegistration> signInClientRegistrations = new ArrayList<>();

    protected void beforeInit() throws Exception {
    }
    protected abstract O performBuild() throws Exception;

    public List<ClientRegistration> getAllClientRegistrations() {
        return this.allClientRegistrations;
    }

    public List<ClientRegistration> getSignInClientRegistrations() {
        return this.signInClientRegistrations;
    }

    @Override
    public void addSignInClientRegistrations(List<ClientRegistration> signInClientRegistrations) {
        Assert.isTrue(!CollectionUtils.isEmpty(signInClientRegistrations), "signInClientRegistrations cannot be empty");
        this.signInClientRegistrations.addAll(signInClientRegistrations);
    }

    @Override
    public void addClientRegistrations(List<ClientRegistration> clientRegistrations) {
        Assert.isTrue(!CollectionUtils.isEmpty(clientRegistrations), "clientRegistrations cannot be empty");
        this.allClientRegistrations.addAll(clientRegistrations);
    }

    @Override
    public final O build() throws Exception {
        if (this.building.compareAndSet(false, true)) {
            return doBuild();
        }
        throw new AlreadyBuiltException("This clientRegistrationRepository has already been built");
    }

    public ClientRegistrationRepositoryConfigurerAdapter<O> apply(ClientRegistrationRepositoryConfigurerAdapter<O> configurer) {
        configurer.setBuilder(this);
        this.configurers.add(configurer);
        return configurer;
    }

    private void configure() throws Exception {
        for (ClientRegistrationRepositoryConfigurerAdapter<O> configurer : configurers) {
            configurer.configure(this);
        }
    }

    protected final O doBuild() throws Exception {
        synchronized (this.configurers) {
            beforeInit();
            configure();
            return performBuild();
        }
    }
}
