// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.annotation;

import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistrar;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistry;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bean post-processor that registers methods annotated with {@link T}
 * to be invoked by a Azure message listener container created under the cover
 * by a {@link MessageListenerContainerFactory}
 * according to the attributes of the annotation.
 *
 * <p>Annotated methods can use flexible arguments as defined by {@link T}.
 *
 * <p>This post-processor is automatically registered by the {@link EnableAzureMessaging} annotation.
 *
 * <p>Auto-detects any {@link AzureListenerConfigurer} instances in the container,
 * allowing for customization of the registry to be used, the default container
 * factory or for fine-grained control over endpoints registration. See the
 * {@link EnableAzureMessaging} javadocs for complete usage details.
 *
 * @see EnableAzureMessaging
 * @see AzureListenerConfigurer
 * @see AzureListenerEndpointRegistrar
 * @see AzureListenerEndpointRegistry
 */
public abstract class AzureListenerAnnotationBeanPostProcessorAdapter<T>
        implements MergedBeanDefinitionPostProcessor, Ordered, BeanFactoryAware, SmartInitializingSingleton {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureListenerAnnotationBeanPostProcessorAdapter.class);

    public static final String DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME = "azureListenerEndpointRegistry";
    /**
     * The bean name of the default {@link MessageListenerContainerFactory}.
     */
    protected String containerFactoryBeanName;

    private final MessageHandlerMethodFactoryAdapter messageHandlerMethodFactory =
        new MessageHandlerMethodFactoryAdapter();

    protected final AtomicInteger counter = new AtomicInteger();

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    private final AzureListenerEndpointRegistrar registrar = new AzureListenerEndpointRegistrar();

    @Nullable
    private AzureListenerEndpointRegistry endpointRegistry;

    @Nullable
    private BeanFactory beanFactory;

    @Nullable
    private StringValueResolver embeddedValueResolver;

    /**
     * Set the container factory bean name.
     * @param containerFactoryBeanName the container factory bean name.
     */
    public void setContainerFactoryBeanName(String containerFactoryBeanName) {
        this.containerFactoryBeanName = containerFactoryBeanName;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * Making a {@link BeanFactory} available is optional; if not set,
     * {@link AzureListenerConfigurer} beans won't get autodetected and an
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
        this.registrar.setBeanFactory(beanFactory);
    }

    @Override
    public void afterSingletonsInstantiated() {
        // Remove resolved singleton classes from cache
        this.nonAnnotatedClasses.clear();

        if (this.beanFactory instanceof ListableBeanFactory) {
            // Apply AzureListenerConfigurer beans from the BeanFactory, if any
            Map<String, AzureListenerConfigurer> beans =
                    ((ListableBeanFactory) this.beanFactory).getBeansOfType(AzureListenerConfigurer.class);
            List<AzureListenerConfigurer> configurers = new ArrayList<>(beans.values());
            AnnotationAwareOrderComparator.sort(configurers);
            for (AzureListenerConfigurer configurer : configurers) {
                configurer.configureAzureListeners(this.registrar);
            }
        }

        if (this.containerFactoryBeanName != null) {
            this.registrar.setContainerFactoryBeanName(this.containerFactoryBeanName);
        }

        if (this.registrar.getEndpointRegistry() == null) {
            // Determine AzureListenerEndpointRegistry bean from the BeanFactory
            if (this.endpointRegistry == null) {
                Assert.state(this.beanFactory != null,
                        "BeanFactory must be set to find endpoint registry by bean name");
                this.endpointRegistry = this.beanFactory.getBean(DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                        AzureListenerEndpointRegistry.class);
            }
            this.registrar.setEndpointRegistry(this.endpointRegistry);
        }

        // Set the custom handler method factory once resolved by the configurer
        MessageHandlerMethodFactory handlerMethodFactory = this.registrar.getMessageHandlerMethodFactory();
        if (handlerMethodFactory != null) {
            this.messageHandlerMethodFactory.setMessageHandlerMethodFactory(handlerMethodFactory);
        }

        // Actually register all listeners
        this.registrar.afterPropertiesSet();
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
    }

    /**
     * @throws BeansException in case of errors
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * @throws BeansException in case of errors
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AopInfrastructureBean
            || bean instanceof MessageListenerContainerFactory
            || bean instanceof AzureListenerEndpointRegistry) {
            // Ignore AOP infrastructure such as scoped proxies.
            return bean;
        }

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!this.nonAnnotatedClasses.contains(targetClass)) {
            Map<Method, Set<T>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<Set<T>>) method -> {
                    Set<T> listenerMethods = findListenerMethods(method);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });

            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(targetClass);
                LOGGER.trace("No @AzureMessageListener annotations found on bean type: {}", targetClass);
            } else {
                // Non-empty set of methods
                annotatedMethods.forEach((method, listeners) -> listeners
                    .forEach(listener -> processAzureListener(listener, method, bean)));
                LOGGER.debug("{} @AzureMessageListener methods processed on bean '{}': {}", annotatedMethods.size(),
                    beanName, annotatedMethods);
            }
        }
        return bean;
    }

    /**
     * Process the given {@link T} annotation on the given method,
     * registering a corresponding endpoint for the given bean instance.
     *
     * @param listenerAnnotation the annotation to process
     * @param mostSpecificMethod the annotated method
     * @param bean the instance to invoke the method on
     * @see AzureListenerEndpointRegistrar#registerEndpoint
     * @throws BeanInitializationException If no ListenerContainerFactory could be found.
     */
    private void processAzureListener(T listenerAnnotation, Method mostSpecificMethod, Object bean) {
        Method invocableMethod = AopUtils.selectInvocableMethod(mostSpecificMethod, bean.getClass());

        AzureListenerEndpoint endpoint = createAndConfigureMethodListenerEndpoint(listenerAnnotation, bean,
            invocableMethod, beanFactory, messageHandlerMethodFactory);

        MessageListenerContainerFactory<?> factory = null;
        String containerFactoryBeanNameResolved = resolve(getContainerFactoryBeanName(listenerAnnotation));
        if (StringUtils.hasText(containerFactoryBeanNameResolved)) {
            Assert.state(this.beanFactory != null, "BeanFactory must be set to obtain container factory by bean name");
            try {
                factory = this.beanFactory.getBean(containerFactoryBeanNameResolved, MessageListenerContainerFactory.class);
            } catch (NoSuchBeanDefinitionException ex) {
                throw new BeanInitializationException(
                    "Could not register Azure listener endpoint on [" + mostSpecificMethod + "], no "
                        + MessageListenerContainerFactory.class.getSimpleName() + " with id '"
                        + containerFactoryBeanNameResolved + "' was found in the application context", ex);
            }
        }

        this.registrar.registerEndpoint(endpoint, factory);
    }


    protected abstract Set<T> findListenerMethods(Method method);

    /**
     * Instantiate an empty {@link AzureListenerEndpoint} and perform further
     * configuration with provided parameters in {@link #processAzureListener}.
     *
     * @param listenerAnnotation the listener annotation
     * @param bean the object instance that should manage this endpoint.
     * @param method the method to invoke to process a message managed by this endpoint.
     * @param beanFactory the Spring bean factory to use to resolve expressions
     * @param messageHandlerMethodFactory the {@link MessageHandlerMethodFactory} to use to build the
     *                                    {@link InvocableHandlerMethod} responsible to manage the invocation of
     *                                    this endpoint.
     *
     * @return an {@link AzureListenerEndpoint} implementation.
     */
    protected abstract AzureListenerEndpoint createAndConfigureMethodListenerEndpoint(
        T listenerAnnotation, Object bean, Method method, BeanFactory beanFactory,
        MessageHandlerMethodFactory messageHandlerMethodFactory);

    protected abstract String getEndpointId(T listenerAnnotation);

    protected abstract String getContainerFactoryBeanName(T listenerAnnotation);

    protected abstract Class<T> getListenerType();

    @Nullable
    protected String resolve(String value) {
        return (this.embeddedValueResolver != null ? this.embeddedValueResolver.resolveStringValue(value) : value);
    }

    /**
     * A {@link MessageHandlerMethodFactory} adapter that offers a configurable underlying
     * instance to use. Useful if the factory to use is determined once the endpoints
     * have been registered but not created yet.
     *
     * @see AzureListenerEndpointRegistrar#setMessageHandlerMethodFactory
     */
    private class MessageHandlerMethodFactoryAdapter implements MessageHandlerMethodFactory {

        @Nullable
        private MessageHandlerMethodFactory messageHandlerMethodFactory;

        @Override
        public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
            return getMessageHandlerMethodFactory().createInvocableHandlerMethod(bean, method);
        }

        private MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
            if (this.messageHandlerMethodFactory == null) {
                this.messageHandlerMethodFactory = createDefaultAzureHandlerMethodFactory();
            }
            return this.messageHandlerMethodFactory;
        }

        public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
            this.messageHandlerMethodFactory = messageHandlerMethodFactory;
        }

        private MessageHandlerMethodFactory createDefaultAzureHandlerMethodFactory() {
            DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
            if (beanFactory != null) {
                defaultFactory.setBeanFactory(beanFactory);
            }
            defaultFactory.afterPropertiesSet();
            return defaultFactory;
        }
    }

    /**
     * Get the default bean name for an implementation class of {@link AzureListenerAnnotationBeanPostProcessorAdapter}.
     * @return the default bean name for the implementation class.
     */
    public abstract String getDefaultAzureListenerAnnotationBeanPostProcessorBeanName();

}

