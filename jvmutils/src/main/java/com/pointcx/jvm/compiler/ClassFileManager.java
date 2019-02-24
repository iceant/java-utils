package com.pointcx.jvm.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassFileManager<M extends JavaFileManager> extends ForwardingJavaFileManager<M>
{
    private final Map<String, JavaClassObject> classObjectByNameMap = new ConcurrentHashMap<>();

    public ClassFileManager(final M standardManager)
    {
        super(standardManager);
    }

    public ClassLoader getClassLoader(final Location location)
    {
        return new SecureClassLoader()
        {
            protected Class<?> findClass(final String name)
            {
                final byte[] buffer = classObjectByNameMap.get(name).getBytes();
                return super.defineClass(name, buffer, 0, buffer.length);
            }
        };
    }

    public JavaFileObject getJavaFileForOutput(final Location location
            , final String className
            , final JavaFileObject.Kind kind
            , final FileObject sibling)
    {
        final JavaClassObject javaClassObject = new JavaClassObject(className, kind);
        classObjectByNameMap.put(className, javaClassObject);

        return javaClassObject;
    }

    public void clear(){
        classObjectByNameMap.clear();
    }

    public void remove(String className){
        classObjectByNameMap.remove(className);
    }
}