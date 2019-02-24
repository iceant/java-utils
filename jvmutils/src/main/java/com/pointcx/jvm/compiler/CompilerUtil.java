package com.pointcx.jvm.compiler;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class CompilerUtil {

    public static Class<?> compileInMemory(final String className, final Map<String, CharSequence> sources)
            throws ClassNotFoundException {
        JavaFileManager fileManager = null;
        try {
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (null == compiler) {
                throw new IllegalStateException("JDK required to run tests. JRE is not sufficient.");
            }

            fileManager = new ClassFileManager<>(
                    compiler.getStandardFileManager(null, null, null));
            final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
                    null, wrap(sources));

            return compileAndLoad(className, diagnostics, fileManager, task);
        }finally {

        }
    }

    public static Class<?> compileAndLoad(
            final String className,
            final DiagnosticCollector<JavaFileObject> diagnostics,
            final JavaFileManager fileManager,
            final JavaCompiler.CompilationTask task) throws ClassNotFoundException {
        if (!compile(diagnostics, task)) {
            return null;
        }

        return fileManager.getClassLoader(null).loadClass(className);
    }

    public static boolean compile(final DiagnosticCollector<JavaFileObject> diagnostics, final JavaCompiler.CompilationTask task) {
        final Boolean succeeded = task.call();

        if (!succeeded) {
            for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.err.println(diagnostic.getCode());
                System.err.println(diagnostic.getKind());

                final JavaFileObject source = diagnostic.getSource();
                System.err.printf("Line = %d, Col = %d, File = %s",
                        diagnostic.getLineNumber(), diagnostic.getColumnNumber(), source);

                System.err.println("Start: " + diagnostic.getStartPosition());
                System.err.println("End: " + diagnostic.getEndPosition());
                System.err.println("Pos: " + diagnostic.getPosition());

                try {
                    final String content = source.getCharContent(true).toString();
                    final int begin = content.lastIndexOf('\n', (int) diagnostic.getStartPosition());
                    final int end = content.indexOf('\n', (int) diagnostic.getEndPosition());
                    System.err.println(diagnostic.getMessage(null));
                    System.err.println(content.substring(Math.max(0, begin), end));
                } catch (final IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return succeeded;
    }

    private static Collection<CharSequenceJavaFileObject> wrap(final Map<String, CharSequence> sources) {
        return sources
                .entrySet()
                .stream()
                .map((e) -> new CharSequenceJavaFileObject(e.getKey(), e.getValue()))
                .collect(toList());
    }
}
