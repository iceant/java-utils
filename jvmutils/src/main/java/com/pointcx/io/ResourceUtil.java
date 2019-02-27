package com.pointcx.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;

import static com.pointcx.io.ResourceUtil.InternalUtil.readFully;

public class ResourceUtil {
    static class InternalUtil{
        static ByteArrayOutputStream readFully(InputStream inputStream, int bufferSize)
                throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[bufferSize];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos;
        }
    }
    /**
     * Get Resources from classpath
     * @param name
     * @return
     */
    public static Enumeration<URL> getResources(String name, ClassLoader classLoader){
        try {
            return classLoader.getResources(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get resources from thread current classloader
     * @param name
     * @return
     */
    public static Enumeration<URL> getResources(String name){
        return getResources(name, Thread.currentThread().getContextClassLoader());
    }

    /**
     * get resource from given classloader
     * @param name
     * @param classLoader
     * @return
     */
    public static URL getResoruce(String name, ClassLoader classLoader){
        return classLoader.getResource(name);
    }

    /**
     * get resource from thread current classloader
     * @param name
     * @return
     */
    public static URL getResoruce(String name){
        return getResoruce(name, Thread.currentThread().getContextClassLoader());
    }

    /**
     * get resource inputstream from classloader
     * @param name
     * @param classLoader
     * @return
     */
    public static InputStream getResourceAsStream(String name, ClassLoader classLoader){
        return classLoader.getResourceAsStream(name);
    }

    /**
     * get resource input stream from thread current classloader
     * @param name
     * @return
     */
    public static InputStream getResourceAsStream(String name){
        return getResourceAsStream(name, Thread.currentThread().getContextClassLoader());
    }

    /**
     * read resource as utf8 string
     * @param name
     * @return
     */
    public static String getResourceAsUTF8String(String name){
        try {
            return readFully(getResourceAsStream(name), 2*1024).toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get resource reader with encoding
     * @param name
     * @param encoding
     * @return
     */
    public static Reader getResourceAsReader(String name, String encoding){
        try {
            return new InputStreamReader(getResourceAsStream(name), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get resource as utf8 reader
     * @param name
     * @return
     */
    public static Reader getResourceAsUTF8Reader(String name){
        try {
            return new InputStreamReader(getResourceAsStream(name), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
