// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.annotation.AzureMessageListener;
import com.azure.spring.messaging.annotation.AzureMessageListeners;
import com.azure.spring.messaging.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.container.ListenerContainerFactory;
import com.azure.spring.messaging.endpoint.MethodAzureListenerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bean post-processor that registers methods annotated with {@link AzureMessageListener}
 * to be invoked by a Azure message listener container created under the cover
 * by a {@link ListenerContainerFactory}
 * according to the attributes of the annotation.
 *
 * <p>Annotated methods can use flexible arguments as defined by {@link AzureMessageListener}.
 *
 * <p>This post-processor is automatically registered by the {@link EnableAzureMessaging} annotation.
 *
 * <p>Autodetects any {@link AzureListenerConfigurer} instances in the container,
 * allowing for customization of the registry to be used, the default container
 * factory or for fine-grained control over endpoints registration. See the
 * {@link EnableAzureMessaging} javadocs for complete usage details.
 *
 * @author Warren Zhu
 * @see AzureMessageListener
 * @see EnableAzureMessaging
 * @see AzureListenerConfigurer
 * @see AzureListenerEndpointRegistrar
 * @see AzureListenerEndpointRegistry
 * @see MethodAzureListenerEndpoint
 */
public class AzureListenerAnnotationBeanPostProcessor
        implements MergedBeanDefinitionPostProcessor, Ordered, BeanFactoryAware, SmartInitializingSingleton {

    /**
     * The bean name of the default {@link ListenerContainerFactory}.
     */
    public static final String DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "azureListenerContainerFactory";
    public static final String DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME = "azureListenerEndpointRegistry";
    private static final Logger LOG = LoggerFactory.getLogger(AzureListenerAnnotationBeanPostProcessor.class);
    private final MessageHandlerMethodFactoryAdapter messageHandlerMethodFactory =
        new MessageHandlerMethodFactoryAdapter();
    private final AzureListenerEndpointRegistrar registrar = new AzureListenerEndpointRegistrar();
    private final AtomicInteger counter = new AtomicInteger();
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    private String containerFactoryBeanName = DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME;
    @Nullable
    private AzureListenerEndpointRegistry endpointRegistry;
    @Nullable
    private BeanFactory beanFactory;
    @Nullable
    private StringValueResolver embeddedValueResolver;

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
        if (bean instanceof AopInfrastructureBean || bean instanceof ListenerContainerFactory
            || bean instanceof AzureListenerEndpointRegistry) {
            // Ignore AOP infrastructure such as scoped proxies.
            return bean;
        }

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!this.nonAnnotatedClasses.contains(targetClass)) {
            Map<Method, Set<AzureMessageListener>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<Set<AzureMessageListener>>) method -> {
                    Set<AzureMessageListener> listenerMethods = AnnotatedElementUtils
                        .getMergedRepeatableAnnotations(method, AzureMessageListener.class,
                            AzureMessageListeners.class);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(targetClass);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("No @AzureMessageListener annotations found on bean type: " + targetClass);
                }
            } else {
                // Non-empty set of methods
                annotatedMethods.forEach((method, listeners) -> listeners
                    .forEach(listener -> processAzureListener(listener, method, bean)));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        annotatedMethods.size() + " @AzureMessageListener methods processed on bean '" + beanName
                            + "': " + annotatedMethods);
                }
            }
        }
        return bean;
    }

    /**
     * Process the given {@link AzureMessageListener} annotation on the given method,
     * registering a corresponding endpoint for the given bean instance.
     *
     * @param azureMessageListener the annotation to process
     * @param mostSpecificMethod the annotated method
     * @param bean the instance to invoke the method on
     * @see #createMethodAzureListenerEndpoint()
     * @see AzureListenerEndpointRegistrar#registerEndpoint
     * @throws BeanInitializationException If no ListenerContainerFactory could be found.
     */
    protected void processAzureListener(AzureMessageListener azureMessageListener, Method mostSpecificMethod,
            Object bean) {
        Method invocableMethod = AopUtils.selectInvocableMethod(mostSpecificMethod, bean.getClass());

        MethodAzureListenerEndpoint endpoint = createMethodAzureListenerEndpoint();
        endpoint.setBean(bean);
        endpoint.setMethod(invocableMethod);
        endpoint.setBeanFactory(this.beanFactory);
        endpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);
        endpoint.setId(getEndpointId(azureMessageListener));
        endpoint.setDestination(resolve(azureMessageListener.destination()));

        if (StringUtils.hasText(azureMessageListener.group())) {
            endpoint.setGroup(resolve(azureMessageListener.group()));
        }
        if (StringUtils.hasText(azureMessageListener.concurrency())) {
            endpoint.setConcurrency(resolve(azureMessageListener.concurrency()));
        }

        ListenerContainerFactory<?> factory = null;
        String containerFactoryBeanName = resolve(azureMessageListener.containerFactory());
        if (StringUtils.hasText(containerFactoryBeanName)) {
            Assert.state(this.beanFactory != null, "BeanFactory must be set to obtain container factory by bean name");
            try {
                factory = this.beanFactory.getBean(containerFactoryBeanName, ListenerContainerFactory.class);
            } catch (NoSuchBeanDefinitionException ex) {
                throw new BeanInitializationException(
                    "Could not register Azure listener endpoint on [" + mostSpecificMethod + "], no "
                        + ListenerContainerFactory.class.getSimpleName() + " with id '"
                        + containerFactoryBeanName + "' was found in the application context", ex);
            }
        }

        this.registrar.registerEndpoint(endpoint, factory);
    }

    /**
     * Instantiate an empty {@link MethodAzureListenerEndpoint} for further
     * configuration with provided parameters in {@link #processAzureListener}.
     *
     * @return a new {@code MethodAzureListenerEndpoint} or subclass thereof
     */
    protected MethodAzureListenerEndpoint createMethodAzureListenerEndpoint() {
        return new MethodAzureListenerEndpoint();
    }

    private String getEndpointId(AzureMessageListener azureMessageListener) {
        if (StringUtils.hasText(azureMessageListener.id())) {
            String id = resolve(azureMessageListener.id());
            return (id != null ? id : "");
        } else {
            return "org.springframework.azure.AzureListenerEndpointContainer#" + this.counter.getAndIncrement();
        }
    }

    @Nullable
    private String resolve(String value) {
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

}
