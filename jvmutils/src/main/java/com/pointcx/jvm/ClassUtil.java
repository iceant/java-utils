package com.pointcx.jvm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassUtil {

    public static Method getMethod(Class<?> sourceClass, String methodName, Class... paramTypes) {
        Method method = null;
        try {
            method = sourceClass.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ignored) {
        }

        if (method == null) {
            try {
                method = sourceClass.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }

        if (method == null) {
            Class<?> superClass = sourceClass.getSuperclass();
            if (superClass != null) {
                method = getMethod(superClass, methodName, paramTypes);
            }
        }
        return method;
    }

    public static <T> T invokeStaticMethod(Class<?> sourceClass, String methodName){
        return invokeStaticMethod(sourceClass, methodName, null, (Object[])null);
    }

    public static <T> T invokeStaticMethod(Class<?> sourceClass, String methodName, Class[] paramTypes, Object... params) {
        Method method = getMethod(sourceClass, methodName, paramTypes);
        Object result = null;
        try {
            if (method != null) {
                method.setAccessible(true);
                result = method.invoke(null, params);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) result;
    }


    public static <T> T invoke(Object instance, String methodName) {
        return invoke(instance, methodName, null, (Object[])null);
    }

    public static <T> T invoke(Object instance, String methodName, Class[] paramTypes, Object... params){
        try {
            Method method = instance.getClass().getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (T) method.invoke(instance, params);
        }catch (Exception err){
            throw new RuntimeException(err);
        }
    }

    public static <T> T newInstance(String className){
        return newInstance(className, Thread.currentThread().getContextClassLoader());
    }

    public static <T> T newInstance(String className, ClassLoader classLoader){
        try {
            Class<T> cls = (Class<T>) classLoader.loadClass(className);
            return newInstance(cls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(String className, Class[] paramTypes, Object ... params){
        return newInstance(className, Thread.currentThread().getContextClassLoader(), paramTypes, params);
    }

    public static <T> T newInstance(String className, ClassLoader classLoader, Class[] paramTypes, Object ... params){
        try {
            Class<T> cls = (Class<T>) classLoader.loadClass(className);
            return newInstance(cls, paramTypes, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Class<T> cls){
        try {
            return cls.newInstance();
        } catch (Exception err){
            throw new RuntimeException(err);
        }
    }

    public static <T> T newInstance(Class<T> cls, Class[] paramTypes, Object... params){
        try {
            Constructor<T> constructor = cls.getConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(params);
        }catch (Exception err){
            throw new RuntimeException(err);
        }
    }

    public static URL[] listClassPath(URLClassLoader classLoader) {
        try {
            return invoke(classLoader, "getURLs", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getFieldByFieldName(Object obj, String fieldName) {
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }

    public static Field getField(Class<?> sourceClass, String fieldName) {
        Field field = null;
        try {
            field = sourceClass.getField(fieldName);
        } catch (NoSuchFieldException ignored) {
        }

        if (field == null) {
            try {
                field = sourceClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            }
        }

        if (field == null) {
            Class<?> superClass = sourceClass.getSuperclass();
            if (superClass != null) {
                field = getField(superClass, fieldName);
            }
        }
        return field;
    }


    public static <T> T getFieldValue(Object obj, String fieldName) {
        try {
            Field field = getFieldByFieldName(obj, fieldName);
            T value = null;
            if (field != null) {
                if (field.isAccessible()) {
                    value = (T) field.get(obj);
                } else {
                    field.setAccessible(true);
                    value = (T) field.get(obj);
                    field.setAccessible(false);
                }
            }
            return value;
        }catch (Exception err){
            throw new RuntimeException(err);
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value){
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            if (field.isAccessible()) {
                field.set(obj, value);
            } else {
                field.setAccessible(true);
                field.set(obj, value);
                field.setAccessible(false);
            }
        }catch (Exception err){
            throw new RuntimeException(err);
        }
    }

    public static boolean setStaticField(Class clazz, String fieldName, Object value) {
        if (clazz == null) {
            return false;
        }
        try {
            Field field = getField(clazz, fieldName);
            if (field != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(null, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static <T> T getStaticFieldValue(Class<?> sourceClass, String fieldName) {
        Field field = getField(sourceClass, fieldName);
        T value = null;
        if (field != null) {
            field.setAccessible(true);
            if (isStatic(field)) {
                try {
                    value = (T) field.get(null);
                    return value;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            throw new IllegalArgumentException(String.format("Field '%s' is not static!", fieldName));
        } else {
            throw new IllegalArgumentException(String.format("Field '%s' is not exist!", fieldName));
        }
    }

    public static boolean isStatic(Object fieldOrMethod) {
        return java.lang.reflect.Modifier.isStatic((Integer) invoke(fieldOrMethod, "getModifiers", null));
    }

    public static boolean isFinal(Object fieldOrMethod) {
        return java.lang.reflect.Modifier.isFinal((Integer) invoke(fieldOrMethod, "getModifiers", null));
    }

    public static boolean isPublic(Object fieldOrMethod) {
        return java.lang.reflect.Modifier.isPublic((Integer) invoke(fieldOrMethod, "getModifiers", null));
    }

    public static boolean isPrivate(Object fieldOrMethod) {
        return java.lang.reflect.Modifier.isPrivate((Integer) invoke(fieldOrMethod, "getModifiers", null));
    }

    public static boolean isProtected(Object fieldOrMethod) {
        return java.lang.reflect.Modifier.isProtected((Integer) invoke(fieldOrMethod, "getModifiers", null));
    }

}
