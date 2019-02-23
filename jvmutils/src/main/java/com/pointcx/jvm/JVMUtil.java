package com.pointcx.jvm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JVMUtil {
    /**
     * Open module/package to all
     * @param moduleName for example: "java.base"
     * @param packages for example: "jkd.internal.loader"
     */
    public static void jdk9AddOpens(String moduleName, String... packages) {
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
                System.out.printf("open pkg: %s/%s\n", moduleName, pkg);
                opens.invoke(moduleBuilder, pkg);
            }
            Method build = moduleBuilder.getClass().getDeclaredMethod("build");
            build.invoke(moduleBuilder);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
