package com.pointcx.jvm.compiler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class CharSequenceJavaFileObject extends SimpleJavaFileObject {
    private final CharSequence sourceCode;

    public CharSequenceJavaFileObject(final String className, final CharSequence sourceCode)
    {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    public CharSequence getCharContent(final boolean ignoreEncodingErrors)
    {
        return sourceCode;
    }
}