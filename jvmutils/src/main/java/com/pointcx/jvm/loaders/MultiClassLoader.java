package com.pointcx.jvm.loaders;

import java.util.Hashtable;

/**
 * A simple test class loader capable of loading from multiple sources, such as local files or a
 * URL.
 *
 * This class is derived from an article by Chuck McManis
 * http://www.javaworld.com/javaworld/jw-10-1996/indepth.src.html with large modifications.
 *
 * Note that this has been updated to use the non-deprecated version of defineClass() -- JDM.
 *
 * @author Jack Harich - 8/18/97
 * @author John D. Mitchell - 99.03.04
 */

public abstract class MultiClassLoader extends ClassLoader{
    private final Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();
    private char classNameReplacementChar;

    public MultiClassLoader() {
    }

    protected String formatClassName(final String className) {
        if (classNameReplacementChar == '\u0000') {
            // '/' is used to map the package to the path
            return className.replace('.', '/') + ".class";
        } else {
            // Replace '.' with custom char, such as '_'
            return className.replace('.', classNameReplacementChar) + ".class";
        }
    }

    protected abstract byte[] loadClassBytes(String className);

    @Override
    public Class<?> loadClass(final String className) throws ClassNotFoundException {
        return loadClass(className, true);
    }

    @Override
    public synchronized Class<?> loadClass(final String className, final boolean resolveIt)
            throws ClassNotFoundException {

        Class<?> result;
        byte[] classBytes;

        // ----- Check our local cache of classes
        result = classes.get(className);
        if (result != null) {
            return result;
        }

        // ----- Check with the primordial class loader
        try {
            result = super.findSystemClass(className);
            return result;
        } catch (final ClassNotFoundException e) {
        }

        // ----- Try to load it from preferred source
        // Note loadClassBytes() is an abstract method
        classBytes = loadClassBytes(className);
        if (classBytes == null) {
            throw new ClassNotFoundException();
        }

        // ----- Define it (parse the class file)
        result = defineClass(className, classBytes, 0, classBytes.length);
        if (result == null) {
            throw new ClassFormatError();
        }

        // ----- Resolve if necessary
        if (resolveIt) {
            resolveClass(result);
        }

        // Done
        classes.put(className, result);
        return result;
    }

    public void setClassNameReplacementChar(char classNameReplacementChar) {
        this.classNameReplacementChar = classNameReplacementChar;
    }
}
