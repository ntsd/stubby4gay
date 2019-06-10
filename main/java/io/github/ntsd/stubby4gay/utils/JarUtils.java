package io.github.ntsd.stubby4gay.utils;

import io.github.ntsd.stubby4gay.annotations.CoberturaIgnore;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 6:33 PM
 */
public final class JarUtils {

    private JarUtils() {

    }

    @CoberturaIgnore
    public static String readManifestImplementationVersion() {
        final URLClassLoader classLoader = (URLClassLoader) JarUtils.class.getClassLoader();
        try {
            final URL url = classLoader.findResource("META-INF/MANIFEST.MF");
            final Manifest manifest = new Manifest(url.openStream());
            return manifest.getMainAttributes().getValue("Implementation-Version");
        } catch (Exception e) {
            //Do nothing
        }

        return "x.x.xx";
    }

    @CoberturaIgnore
    public static String readManifestBuiltDate() {
        final URLClassLoader classLoader = (URLClassLoader) JarUtils.class.getClassLoader();
        try {
            final URL url = classLoader.findResource("META-INF/MANIFEST.MF");
            final Manifest manifest = new Manifest(url.openStream());
            return manifest.getMainAttributes().getValue("Built-Date");
        } catch (Exception e) {
            //Do nothing
        }

        return DateTimeUtils.systemDefault();
    }
}
