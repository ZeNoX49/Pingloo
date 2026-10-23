package com.dbeditor.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Singleton<T extends Singleton<T>> {

    private static final Map<Class<?>, Singleton<?>> INSTANCES = new ConcurrentHashMap<>();

    public static <T extends Singleton<T>> T getInstance(Class<T> c) {
        return c.cast(Singleton.INSTANCES.computeIfAbsent(c, key -> {
            try {
                return (Singleton<?>) key.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot instantiate " + key.getName(), e);
            }
        }));
    }
}
