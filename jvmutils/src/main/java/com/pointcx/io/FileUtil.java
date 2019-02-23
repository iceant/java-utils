package com.pointcx.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FileUtil {

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    static class SearchUtil{
        static void recursive(File path, FileFilter filter, List<File> result){
            for(File child : path.listFiles()){
                if(child.isDirectory()){
                    recursive(child, filter, result);
                }
            }
            if(path.isDirectory()){
                File[] files = path.listFiles();
                if(files!=null) {
                    result.addAll(Arrays.asList(path.listFiles(filter)));
                }
            }

        }
    }

    public static List<File> search(File path, FileFilter filter){
        List<File> result = new LinkedList<File>();
        SearchUtil.recursive(path, filter, result);
        return result;
    }

    public static boolean mkdirs(String path){
        return Paths.get(path).toFile().mkdirs();
    }

    public static void rmdir(Path dir)
            throws IOException {
        Files.walkFileTree(dir,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult
                    visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult
                    postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

}
