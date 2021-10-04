// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
 * Most applications do not need to pass arbitrary data to the pipeline and can pass {@code Context.NONE} or
 * {@code null}.
 * <p>
 * Each context object is immutable. The {@link #addData(Object, Object)} method creates a new
 * {@code Context} object that refers to its parent, forming a linked list.
 */
@Immutable
public class Context {
    private static final ClientLogger LOGGER = new ClientLogger(Context.class);

    // All fields must be immutable.
    //
    /**
     * Signifies that no data needs to be passed to the pipeline.
     */
    public static final Context NONE = new Context(null, null, null) {
        @Override
        public Optional<Object> getData(Object key) {
            if (key == null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("key cannot be null"));
            }

            return Optional.empty();
        }

        @Override
        public Map<Object, Object> getValues() {
            return Collections.emptyMap();
        }
    };

    private final Context parent;
    private final Object key;
    private final Object value;

    /**
     * Constructs a new {@link Context} object.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.azure.core.util.context#object-object -->
     * <pre>
     * &#47;&#47; Create an empty context having no data
     * Context emptyContext = Context.NONE;
     *
     * &#47;&#47; Tracing spans created by users can be passed to calling methods in sdk clients using Context object
     * final String userParentSpan = &quot;user-parent-span&quot;;
     *
     * &#47;&#47; Create a context using the provided key and user parent span
     * Context keyValueContext = new Context&#40;PARENT_SPAN_KEY, userParentSpan&#41;;
     * </pre>
     * <!-- end com.azure.core.util.context#object-object -->
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context(Object key, Object value) {
        this.parent = null;
        this.key = Objects.requireNonNull(key, "'key' cannot be null.");
        this.value = value;
    }

    private Context(Context parent, Object key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    /**
     * Adds a new immutable {@link Context} object with the specified key-value pair to
     * the existing {@link Context} chain.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.azure.core.util.context.addData#object-object -->
     * <pre>
     * &#47;&#47; Users can send parent span information and pass additional metadata to attach to spans of the calling methods
     * &#47;&#47; using the Context object
     * final String hostNameValue = &quot;host-name-value&quot;;
     * final String entityPathValue = &quot;entity-path-value&quot;;
     * final String userParentSpan = &quot;user-parent-span&quot;;
     * Context parentSpanContext = new Context&#40;PARENT_SPAN_KEY, userParentSpan&#41;;
     *
     * &#47;&#47; Add a new key value pair to the existing context object.
     * Context updatedContext = parentSpanContext.addData&#40;HOST_NAME_KEY, hostNameValue&#41;
     *     .addData&#40;ENTITY_PATH_KEY, entityPathValue&#41;;
     *
     * &#47;&#47; Both key values found on the same updated context object
     * System.out.printf&#40;&quot;Hostname value: %s%n&quot;, updatedContext.getData&#40;HOST_NAME_KEY&#41;.get&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Entity Path value: %s%n&quot;, updatedContext.getData&#40;ENTITY_PATH_KEY&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.context.addData#object-object -->
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     * @return the new {@link Context} object containing the specified pair added to the set of pairs.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("key cannot be null"));
        }
        return new Context(this, key, value);
    }

    /**
     * Creates a new immutable {@link Context} object with all the keys and values provided by
     * the input {@link Map}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.azure.core.util.context.of#map -->
     * <pre>
     * final String key1 = &quot;Key1&quot;;
     * final String value1 = &quot;first-value&quot;;
     * Map&lt;Object, Object&gt; keyValueMap = new HashMap&lt;&gt;&#40;&#41;;
     * keyValueMap.put&#40;key1, value1&#41;;
     *
     * &#47;&#47; Create a context using the provided key value pair map
     * Context keyValueContext = Context.of&#40;keyValueMap&#41;;
     * System.out.printf&#40;&quot;Key1 value %s%n&quot;, keyValueContext.getData&#40;key1&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.context.of#map -->
     *
     * @param keyValues The input key value pairs that will be added to this context.
     * @return Context object containing all the key-value pairs in the input map.
     * @throws IllegalArgumentException If {@code keyValues} is {@code null} or empty
     */
    public static Context of(Map<Object, Object> keyValues) {
        if (CoreUtils.isNullOrEmpty(keyValues)) {
            throw new IllegalArgumentException("Key value map cannot be null or empty");
        }

        Context context = null;
        for (Map.Entry<Object, Object> entry : keyValues.entrySet()) {
            if (context == null) {
                context = new Context(entry.getKey(), entry.getValue());
            } else {
                context = context.addData(entry.getKey(), entry.getValue());
            }
        }
        return context;
    }

    /**
     * Scans the linked-list of {@link Context} objects looking for one with the specified key.
     * Note that the first key found, i.e. the most recently added, will be returned.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.azure.core.util.context.getData#object -->
     * <pre>
     * final String key1 = &quot;Key1&quot;;
     * final String value1 = &quot;first-value&quot;;
     *
     * &#47;&#47; Create a context object with given key and value
     * Context context = new Context&#40;key1, value1&#41;;
     *
     * &#47;&#47; Look for the specified key in the returned context object
     * Optional&lt;Object&gt; optionalObject = context.getData&#40;key1&#41;;
     * if &#40;optionalObject.isPresent&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Key1 value: %s%n&quot;, optionalObject.get&#40;&#41;&#41;;
     * &#125; else &#123;
     *     System.out.println&#40;&quot;Key1 does not exist or have data.&quot;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.core.util.context.getData#object -->
     *
     * @param key The key to search for.
     * @return The value of the specified key if it exists.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Optional<Object> getData(Object key) {
        if (key == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("key cannot be null"));
        }
        for (Context c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return Optional.of(c.value);
            }
        }
        return Optional.empty();
    }

    /**
     * Scans the linked-list of {@link Context} objects populating a {@link Map} with the values of the context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.azure.core.util.Context.getValues -->
     * <pre>
     * final String key1 = &quot;Key1&quot;;
     * final String value1 = &quot;first-value&quot;;
     * final String key2 = &quot;Key2&quot;;
     * final String value2 = &quot;second-value&quot;;
     *
     * Context context = new Context&#40;key1, value1&#41;
     *     .addData&#40;key2, value2&#41;;
     *
     * Map&lt;Object, Object&gt; contextValues = context.getValues&#40;&#41;;
     * if &#40;contextValues.containsKey&#40;key1&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Key1 value: %s%n&quot;, contextValues.get&#40;key1&#41;&#41;;
     * &#125; else &#123;
     *     System.out.println&#40;&quot;Key1 does not exist.&quot;&#41;;
     * &#125;
     *
     * if &#40;contextValues.containsKey&#40;key2&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Key2 value: %s%n&quot;, contextValues.get&#40;key2&#41;&#41;;
     * &#125; else &#123;
     *     System.out.println&#40;&quot;Key2 does not exist.&quot;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.core.util.Context.getValues -->
     *
     * @return A map containing all values of the context linked-list.
     */
    public Map<Object, Object> getValues() {
        return getValuesHelper(new HashMap<>());
    }

    private Map<Object, Object> getValuesHelper(Map<Object, Object> values) {
        if (key != null) {
            values.putIfAbsent(key, value);
        }

        return (parent == null) ? values : parent.getValuesHelper(values);
    }
}
