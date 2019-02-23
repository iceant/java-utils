package com.pointcx.jvm.loaders;

import com.pointcx.io.FileUtil;
import com.pointcx.jvm.ClassUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.List;

public class ClassPathHackerTest {

    static final String EXTERNAL_PATH = "temp";
    static final String CLASSNAME = "com.pointcx.jvm.classloaders.ExternalClass";

    private String formatClassNameToPath(String className){
        return className.replace('.', '/') + ".class";
    }

    public File findSource(){
        List<File> result = FileUtil.search(Paths.get(".").toFile(), new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.toString().contains("ExternalClass.class") && !pathname.toString().contains(EXTERNAL_PATH);
            }
        });
        return result.size()>0?result.get(0):null;
    }

    @Before
    public void setup(){
        File dest = Paths.get(EXTERNAL_PATH, formatClassNameToPath(CLASSNAME)).toFile();
        try {
            File src = findSource();
            if(src!=null) {
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                System.out.printf("COPY %s TO %s\n", src.getAbsolutePath(), dest.getAbsolutePath());
                FileUtil.copyFileUsingStream(src, dest);
                src.delete();
            }else{

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws IOException {
//        File dest = Paths.get(EXTERNAL_PATH, formatClassNameToPath(CLASSNAME)).toFile();
//        dest.delete();
    }

    @Test
    public void test() throws Exception {

        System.out.println("SYSTEM CLASSLOADER: "+ClassLoader.getSystemClassLoader());

        System.out.printf("Adding %s to %s\n", Paths.get(EXTERNAL_PATH).toUri().toURL(), ClassLoader.getSystemClassLoader());
        ClassPathHacker.addUrlToClassLoader(Paths.get(EXTERNAL_PATH).toUri().toURL(), ClassLoader.getSystemClassLoader());

        for(URL url:ClassUtil.listClassPath((URLClassLoader) ClassLoader.getSystemClassLoader())){
            System.out.println("path: "+ url);
        }

        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        System.out.println("Try to load class now");
        Class cls = Class.forName(CLASSNAME);
        Object object = cls.newInstance();
        String name = ClassUtil.invoke(object, "getName", null);
        System.out.println(name);

    }
}
