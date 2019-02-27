package com.pointcx.jvm;

import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class ClassScanUtil{
    /**
     * Private helper method
     *
     * @param directory
     *            The directory to start with
     * @param pckgname
     *            The package name to search for. Will be needed for getting the
     *            Class object.
     * @throws ClassNotFoundException
     */
    private static void checkDirectory(File directory, String pckgname, Function<Class<?>, Void> fn){
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files) {
                if (file.endsWith(".class")) {
                    try {
                        String className = pckgname+'.'+file.substring(0, file.length()-6);
                        className = className.startsWith(".")?className.substring(1, className.length()):className;
                        fn.apply(Class.forName(className));
                    } catch (final NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the
                        // loader, and we don't care.
                    } catch (ClassNotFoundException e) {
                        // ignore it
                    }
                } else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
                    checkDirectory(tmpDirectory, pckgname + "." + file, fn);
                }
            }
        }
    }

    /**
     * Private helper method.
     *
     * @param connection
     *            the connection to the jar
     * @param pckgname
     *            the package name to search for
     * @throws ClassNotFoundException
     *             if a file isn't loaded but still is in the jar file
     * @throws IOException
     *             if it can't correctly read from the jar file.
     */
    private static void checkJarFile(JarURLConnection connection,
                                     String pckgname, Function<Class<?>, Void> fn) throws IOException {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry = null; entries.hasMoreElements()
                && ((jarEntry = entries.nextElement()) != null);) {
            name = jarEntry.getName();

            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');

                if (name.contains(pckgname)) {
                    try {
                        fn.apply(Class.forName(name));
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        }
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     *
     * @param pckgname
     *            the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException
     *             if something went wrong
     */
    public static void scan(ClassLoader cld, String pckgname, Function<Class<?>, Void> fn){
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        try {
            if (cld == null)
                throw new IllegalArgumentException("'ClassLoader' can not be null");
            final String scannedPath = pckgname.replace('.', '/');
            final Enumeration<URL> resources = cld.getResources(scannedPath);
            URLConnection connection;

            for (URL url = null; resources.hasMoreElements()
                    && ((url = resources.nextElement()) != null);) {
                try {
                    connection = url.openConnection();
                    if (connection instanceof JarURLConnection) {
                        checkJarFile((JarURLConnection) connection, pckgname, fn);
                    } else if (connection instanceof FileURLConnection) {
                        try {
                            checkDirectory(new File(URLDecoder.decode(url.getPath(),"UTF-8")), pckgname, fn);
                        } catch (final UnsupportedEncodingException ex) {
                            throw new RuntimeException(
                                    pckgname
                                            + " does not appear to be a valid package (Unsupported encoding)",
                                    ex);
                        }
                    } else
                        throw new RuntimeException(pckgname + " ("
                                + url.getPath()
                                + ") does not appear to be a valid package");
                } catch (final IOException ioex) {
                    throw new RuntimeException(
                            "IOException was thrown when trying to get all resources for "
                                    + pckgname, ioex);
                }
            }
        } catch (final NullPointerException ex) {
            throw new RuntimeException(
                    pckgname
                            + " does not appear to be a valid package (Null pointer exception)",
                    ex);
        } catch (final IOException ioex) {
            throw new RuntimeException(
                    "IOException was thrown when trying to get all resources for "
                            + pckgname, ioex);
        }
    }
}

public class ClassUtil {

    public static <T> Class<T> loadClass(String className) throws ClassNotFoundException{
        Class cls = null;
        try {
            cls = loadClass(className, Thread.currentThread().getContextClassLoader());
        }catch (ClassNotFoundException cnfe){
            try {
                cls = loadClass(className, ClassUtil.class.getClassLoader());
            }catch (ClassNotFoundException cnfe2){
                cls = loadClass(className, ClassLoader.getSystemClassLoader());
            }
        }
        return cls;
    }

    public static <T> Class<T> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        if(className==null || classLoader==null) throw new ClassNotFoundException(className);
        return (Class<T>) classLoader.loadClass(className);
    }

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

    public static <T> T invokeStaticMethod(Class<?> sourceClass, String methodName) {
        return invokeStaticMethod(sourceClass, methodName, null, (Object[]) null);
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
        return invoke(instance, methodName, null, (Object[]) null);
    }

    public static <T> T invoke(Object instance, String methodName, Class[] paramTypes, Object... params) {
        try {
            Method method = instance.getClass().getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (T) method.invoke(instance, params);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }


    public static <T> T newInstance(String className) {
        try {
            return newInstance(loadClass(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static <T> T newInstance(String className, ClassLoader classLoader) {
        try {
            Class<T> cls = loadClass(className, classLoader);
            return newInstance(cls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(String className, Class[] paramTypes, Object... params) {
        try {
            return newInstance(loadClass(className), paramTypes, params);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(String className, ClassLoader classLoader, Class[] paramTypes, Object... params) {
        try {
            Class<T> cls = loadClass(className, classLoader);
            return newInstance(cls, paramTypes, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    public static <T> T newInstance(Class<T> cls, Class[] paramTypes, Object... params) {
        try {
            Constructor<T> constructor = cls.getConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(params);
        } catch (Exception err) {
            throw new RuntimeException(err);
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
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            if (field.isAccessible()) {
                field.set(obj, value);
            } else {
                field.setAccessible(true);
                field.set(obj, value);
                field.setAccessible(false);
            }
        } catch (Exception err) {
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

    public static boolean hasDefaultConstructor(Class<?> clazz) {
        Class<?>[] params = {};
        try {
            Constructor constructor = clazz.getConstructor(params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static <T> Constructor<T> getConstructor(String className, Class ... paramTypes){
        try {
            return getConstructor(loadClass(className), paramTypes);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> tClass, Class ... paramTypes){
        try {
            return tClass.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getFieldClass(Class<?> clazz, String name) {
        if (clazz == null || name == null || name.isEmpty()) {
            return null;
        }
        name = name.toLowerCase();
        Class<?> propertyClass = null;
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getName().equals(name)) {
                propertyClass = field.getType();
                break;
            }
        }
        return propertyClass;
    }

    /**
     * enum Constant{VALUE1, VALUE2, ...}
     *
     * @param clazz
     * @param name
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T getEnumConstant(Class<?> enumClass, String name) {
        if (enumClass == null || name == null || name.isEmpty()) {
            return null;
        }
        return (T) Enum.valueOf((Class<Enum>) enumClass, name);
    }


    public static List<Method> findMethod(Class<?> clazz, String methodName) {
        if (clazz==null || methodName==null || methodName.isEmpty()) {
            return null;
        }
        List<Method> methodList = new ArrayList<Method>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                methodList.add(method);
            }
        }
        return methodList;
    }

    public static List<URL> getResources(String resourceName){
        return getResources(resourceName, Thread.currentThread().getContextClassLoader());
    }

    public static List<URL> getResources(String resourceName, ClassLoader classLoader) {
        List<URL> urlList = new ArrayList<>();
        try {
            Enumeration<URL> urls = classLoader.getResources(resourceName);
            if(urls!=null){
                for(;urls.hasMoreElements();){
                    urlList.add(urls.nextElement());
                }
            }
            return urlList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void forEach(ClassLoader classLoader, Function<Class<?>, Void> fn){
        ClassScanUtil.scan(classLoader, "", fn);
    }
}
