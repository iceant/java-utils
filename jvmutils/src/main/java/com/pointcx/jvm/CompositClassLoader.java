package com.pointcx.jvm;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompositClassLoader extends ClassLoader{
    private List<ClassLoader> classLoaderList;
    private Map<String, Class<?>> classCacheMap = new ConcurrentHashMap<String, Class<?>>();

    public CompositClassLoader(ClassLoader parent) {
        super(parent);
        this.classLoaderList = new ArrayList<ClassLoader>();
    }

    public CompositClassLoader() {
        this.classLoaderList = new ArrayList<ClassLoader>();
    }

    public CompositClassLoader addClassLoader(ClassLoader ... classLoaders){
        classLoaderList.addAll(Arrays.asList(classLoaders));
        return this;
    }

    public List<ClassLoader> getClassLoaders(){
        return Collections.unmodifiableList(classLoaderList);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, true);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = classCacheMap.get(name);
        if(result!=null) return result;

        for(ClassLoader classLoader : classLoaderList){
            try {
                result = ClassUtil.invoke(classLoader, "loadClass", new Class[]{String.class, Boolean.class}, name, resolve);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if(result!=null){
                break;
            }
        }

        if(result==null){
            result = super.loadClass(name, resolve);
        }

        if(resolve){
            resolveClass(result);
        }

        classCacheMap.put(name, result);

        return result;
    }
}
