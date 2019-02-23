package com.pointcx.jvm.loaders;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class ClassPathHacker {

    public static void addFile(final File f) throws IOException {
        try {
            addURL(f.toURI().toURL());
        } catch (final MalformedURLException e) {
            // Should never happen
            throw new AssertionError("Malformed URL from toURI() method");
        }
    }

    public static void addFile(final String s) throws IOException {
        final File f = new File(s);
        addFile(f);
    }

    public static void addURL(final URL url) throws IOException {
        addUrlToClassLoader(url, ClassLoader.getSystemClassLoader());
    }

//    public static void setAccessible(Field field, boolean accessible){
//        try {
//            Class unSafeClass = ClassLoader.getSystemClassLoader().loadClass("jdk.internal.misc.Unsafe");
//            Field theUnsafe = unSafeClass.getDeclaredField("theUnsafe");
//            theUnsafe.setAccessible(true);
//            Object unsafe = theUnsafe.get(null);
//            Method objectFieldOffsetMethod = unsafe.getClass().getDeclaredMethod("objectFieldOffset", new Class[]{Field.class});
//            long offset = (long) objectFieldOffsetMethod.invoke(unsafe, AccessibleObject.class.getDeclaredField("override"));
//            Method putBooleanMethod = unsafe.getClass().getDeclaredMethod("putBoolean", new Class[]{Object.class, long.class, boolean.class});
//            putBooleanMethod.invoke(unsafe, field, offset, accessible);
//        }catch (Exception err){
//            err.printStackTrace();
//        }
//    }

    /**
     * Open module/package to all
     * @param moduleName for example: "java.base"
     * @param packages for example: "jkd.internal.loader"
     */
    private static void jdk9AddOpens(String moduleName, String... packages) {
        // Use these JVM parameter to run java.exe
        //  --add-opens java.base/jdk.internal.loader=ALL-UNNAMED
        //
        // ModuleDescriptor.Builder moduleBuilder = ModuleDescriptor.newModule("java.base");
        // moduleBuilder.opens("jdk.internal.loader");
        try {
            Class ModuleDescriptor = Class.forName("java.lang.module.ModuleDescriptor");
            Method newModule = ModuleDescriptor.getDeclaredMethod("newModule", String.class);

            Object moduleBuilder = newModule.invoke(null, moduleName);
            Method opens = moduleBuilder.getClass().getDeclaredMethod("opens", String.class);
            for (String pkg : packages) {
                opens.invoke(moduleBuilder, pkg);
            }
        } catch (ClassNotFoundException e) {
            // it's not jdk9 above, ignore it
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private static void appendToClasspathForJDK9Above(URL u, ClassLoader classLoader) throws NoSuchMethodException {
        jdk9AddOpens("java.base", "jdk.internal.loader");
        try {
            Field ucpField = classLoader.getClass().getDeclaredField("ucp");
            ucpField.setAccessible(true);
            Object ucp = ucpField.get(classLoader);
            Method addURL = ucp.getClass().getDeclaredMethod("addURL", new Class[]{URL.class});
            addURL.invoke(ucp, u);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void addUrlToClassLoader(URL u, ClassLoader classLoader) throws IOException {
        try {
            appendToClasspathForJDK9Above(u, classLoader);
            return;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            Method method = classLoader.getClass().getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
            method.setAccessible(true);
            method.invoke(classLoader, new Object[]{u});
        } catch (NoSuchMethodException e) {
            try {
                Method method = classLoader.getClass().getSuperclass()
                        .getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
                method.setAccessible(true);
                method.invoke(classLoader, new Object[]{u});
            } catch (NoSuchMethodException ex) {
                try {
                    Method method = classLoader.getClass().getSuperclass().getSuperclass()
                            .getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
                    method.setAccessible(true);
                    method.invoke(classLoader, new Object[]{u});
                } catch (Throwable t) {
                    try {
                        if (classLoader.getParent() != null) {
                            addUrlToClassLoader(u, classLoader.getParent());
                        } else {
                            throw new IOException("Error, could not add URL to classloader " + classLoader.getClass().getName());
                        }
                    } catch (IOException e3) {
                        throw e3;
                    }
                }// end try catch
            } catch (Throwable t) {
                throw new IOException("Error, could not add URL to system classloader " + classLoader.getClass().getName());
            }// end try catch
        } catch (Throwable t) {
            throw new IOException("Error, could not add URL to system classloader " + classLoader.getClass().getName());
        }// end try catch
    }
}
