package com.pointcx.jvm.jdk7;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ReflectUtil {

    //////////////////////////////////////////////////////////////////////////
    //// Invoke Methods


    public static <T> T invokeStaticMethod(Class targetCls, String methodName, Class<T> returnType){
        return invokeStaticMethod(targetCls, methodName, returnType, null, (Object[])null);
    }

    public static <T> T invokeStaticMethod(Class targetCls, String methodName, Class<T> returnType, Class[] paramTypes, Object ... params) {
        try {
            MethodType mt = null;
            if(paramTypes!=null) {
                mt = MethodType.methodType(returnType, paramTypes);
            }else{
                mt = MethodType.methodType(returnType);
            }
            MethodHandle methodHandle = MethodHandles.lookup().findStatic(targetCls, methodName, mt);
            T result = (T) methodHandle.invokeWithArguments(params);
            return result;
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }
    }


    public static <T> T invoke(Object target, String methodName, Class<T> returnType){
        return invoke(target, methodName, returnType, null, (Object[])null);
    }

    public static <T> T invoke(Object target, String methodName, Class<T> returnType, Class[] paramTypes, Object... params) {
        try {
            MethodType mt = null;
            if(paramTypes!=null) {
                mt = MethodType.methodType(returnType, paramTypes);
            }else{
                mt = MethodType.methodType(returnType);
            }
            MethodHandle methodHandle = MethodHandles.lookup().findVirtual(target.getClass(), methodName, mt);
            MethodHandle targetMethod = methodHandle.bindTo(target);
            return (T) targetMethod.invokeWithArguments(params);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //// Constructors
    public static <T> T newInstance(String className, ClassLoader classLoader, Class[] paramTypes, Object ... args){
        try {
            return newInstance(classLoader.loadClass(className), paramTypes, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(String className, ClassLoader classLoader){
        try {
            return (T) newInstance(classLoader.loadClass(className));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Class<T> type){
        return newInstance(type, null, (Object[])null);
    }

    public static <T> T newInstance(Class type, Class[] paramTypes, Object ... args){
        MethodType mt = null;
        if(paramTypes!=null) {
            mt = MethodType.methodType(void.class, paramTypes);
        }else{
            mt = MethodType.methodType(void.class);
        }
        try {
            MethodHandle constructor = MethodHandles.lookup().findConstructor(type, mt);
            return (T) constructor.invokeWithArguments(args);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}
