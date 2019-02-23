package com.pointcx.jvm;

import com.pointcx.io.FileUtil;
import com.pointcx.jvm.loaders.ClassPathHacker;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;

public class CompositClassLoaderTest {

    @Test
    public void test()throws Exception{
//        CompositClassLoader compositClassLoader = new CompositClassLoader();
//        compositClassLoader.addClassLoader(
//                new FileClassLoader("D:\\Workspace\\java-utils\\jvmutils\\temp"),
//                new URLClassLoader(new URL[]{Paths.get("D:\\tmp\\test.jar").toUri().toURL()})
//                );
//
//        InputStream is = compositClassLoader.getResourceAsStream("test.txt");
//        String content = FileUtil.readAsString(is);
//        System.out.println(content);
//        Class cls = compositClassLoader.loadClass("com.pointcx.jvm.loaders.ExternalClass");
//        Method method = cls.getMethod("getName");
//        String name = (String) method.invoke(cls.newInstance());
//        System.out.println(name);
//        is.close();

        System.out.println(getClass().getClassLoader());
        System.out.println(ClassLoader.getSystemClassLoader());
        System.out.println(CompositClassLoaderTest.class.getClassLoader());
        System.out.println(Thread.currentThread().getContextClassLoader());

        ClassPathHacker.addFile("D:\\Workspace\\java-utils\\jvmutils\\temp");
        ClassPathHacker.addURL(Paths.get("D:\\tmp\\test.jar").toUri().toURL());

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Field ucpField = classLoader.getClass().getDeclaredField("ucp");
        ucpField.setAccessible(true);
        Object ucp = ucpField.get(classLoader);
        Method getURLS = ucp.getClass().getDeclaredMethod("getURLs");
        for(URL url : (URL[]) getURLS.invoke(ucp)){
            System.out.println(url);
        }


        System.out.println(FileUtil.readAsString(getClass().getClassLoader().getResourceAsStream("test.txt")));

    }
}