package com.pointcx.jvm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class ClassUtil {

    public static <T> T invoke(Object instance, String methodName, Class[] paramTypes, Object... params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = instance.getClass().getMethod(methodName, paramTypes);
        return (T) method.invoke(instance, params);
    }

    public static <T> T newInstance(Class<T> cls) throws IllegalAccessException, InstantiationException {
        return cls.newInstance();
    }

    public static <T> T newInstance(Class<T> cls, Class[] paramTypes, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = cls.getConstructor(paramTypes);
        return constructor.newInstance(params);
    }

    public static URL[] listClassPath(URLClassLoader classLoader) {
        try {
            return invoke(classLoader, "getURLs", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
