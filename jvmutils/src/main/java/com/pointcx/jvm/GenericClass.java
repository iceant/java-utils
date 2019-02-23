package com.pointcx.jvm;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericClass<T>
{
    private Class<T> realType;

    public GenericClass() {
        findTypeArguments(getClass());
    }

    private void findTypeArguments(Type t) {
        if (t instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) t).getActualTypeArguments();
            realType = (Class<T>) typeArgs[0];
        } else {
            Class c = (Class) t;
            findTypeArguments(c.getGenericSuperclass());
        }
    }

    public Type getType()
    {
        // How do I return the type of T? (your question)
        return realType;
    }

    public Class<T> getClassType(){
        return realType;
    }
}