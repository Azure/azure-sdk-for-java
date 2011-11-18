package com.microsoft.windowsazure.common;

import java.util.Map;

public interface Builder {

    public abstract <T> T build(String profile, Class<T> service, Map<String, Object> properties) throws Exception;

    public interface Factory<T> {
        T create(String profile, Builder builder, Map<String, Object> properties) throws Exception;
    }

    public interface Alteration<T> {
        T alter(T instance, Builder builder, Map<String, Object> properties) throws Exception;
    }

    public interface Registry {
        <T> Registry add(Class<T> service);

        <T, TImpl> Registry add(Class<T> service, Class<TImpl> implementation);

        <T> Registry add(Factory<T> factory);

        <T> void alter(Class<T> service, Alteration<T> alteration);
    }

    public interface Exports {
        void register(Registry registry);
    }
}
