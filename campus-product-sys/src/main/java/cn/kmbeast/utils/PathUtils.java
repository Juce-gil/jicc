package cn.kmbeast.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class PathUtils {

    private static final String TARGET_CLASSES_SUFFIX = File.separator + "target" + File.separator + "classes";

    public static String getClassLoadRootPath() {
        try {
            URL resource = PathUtils.class.getClassLoader().getResource("");
            if (resource == null) {
                return new File(".").getCanonicalPath();
            }
            String decodedPath = URLDecoder.decode(resource.getPath(), "utf-8");
            File classpathRoot = new File(decodedPath);
            String absolutePath = classpathRoot.getAbsolutePath();
            if (absolutePath.endsWith(TARGET_CLASSES_SUFFIX)) {
                return absolutePath.substring(0, absolutePath.length() - TARGET_CLASSES_SUFFIX.length());
            }
            return absolutePath;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to decode classpath root", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to resolve classpath root", e);
        }
    }
}
