package com.pointcx.jvm.loaders;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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
        if(classLoaders!=null) {
            classLoaderList.addAll(Arrays.asList(classLoaders));
        }
        return this;
    }

    public List<ClassLoader> getClassLoaders(){
        return Collections.unmodifiableList(classLoaderList);
    }

    private static <T> T invoke(ClassLoader classLoader, String methodName, Class[] params, Object ... values){
        try {
            Method method = classLoader.getClass().getMethod(methodName, params);
            return (T) method.invoke(classLoader, values);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, true);
    }

    @Override
    public URL getResource(String name) {
        for(ClassLoader classLoader: classLoaderList){
            URL url = classLoader.getResource(name);
            if(url!=null) return url;
        }
        return super.getResource(name);
    }

    void appendUrl(Enumeration<URL> urlEnumeration, Vector<URL> result){
        for(;urlEnumeration.hasMoreElements();){
            result.add(urlEnumeration.nextElement());
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Vector<URL> urls = new Vector<URL>();
        for(ClassLoader classLoader : classLoaderList){
            Enumeration<URL> urlEnumeration = classLoader.getResources(name);
            appendUrl(urlEnumeration, urls);
        }
        appendUrl(super.getResources(name), urls);
        return urls.elements();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = classCacheMap.get(name);
        if(result!=null) return result;

        for(ClassLoader classLoader : classLoaderList){
            try {
                Method method = classLoader.getClass().getMethod("loadClass", new Class[]{String.class, boolean.class});
                result = (Class<?>) method.invoke(classLoader, name, resolve);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
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

    public void clearCache(){
        classCacheMap.clear();
    }
}
