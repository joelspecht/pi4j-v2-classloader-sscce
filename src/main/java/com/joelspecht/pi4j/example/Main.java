package com.joelspecht.pi4j.example;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.util.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main implements Closeable {

    private final Context context;

    private Main() throws Exception {
        String workingDir = System.getProperty("user.dir");
        Logger log = LoggerFactory.getLogger(getClass());
        log.info("Working Directory = {}", workingDir);
        Path pathToLibDir = Path.of(workingDir)
                .resolve("target")
                .resolve("lib");
        List<URL> pluginJarUrls = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(pathToLibDir, Files::isRegularFile)) {
            for (Path pathToPluginJar : ds) {
                log.info("Found plugin {}", pathToPluginJar);
                URL pluginJarUrl = pathToPluginJar.toUri().toURL();
                pluginJarUrls.add(pluginJarUrl);
            }
        } catch (NoSuchFileException e) {
            log.warn("Unable to load plugins from {}", pathToLibDir, e);
        }
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader classLoader;
        if (previousClassLoader == null) {
            classLoader = new URLClassLoader(pluginJarUrls.toArray(URL[]::new));
        } else {
            classLoader = new URLClassLoader(pluginJarUrls.toArray(URL[]::new), previousClassLoader);
        }
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            context = Pi4J.newAutoContext();
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassLoader);
        }
    }

    private void printInfo() {
        Console console = new Console();
        PrintInfo.printLoadedPlatforms(console, context);
        PrintInfo.printDefaultPlatform(console, context);
        PrintInfo.printProviders(console, context);
        PrintInfo.printRegistry(console, context);
    }

    @Override
    public void close() {
        context.shutdown();
    }

    public static void main(String[] args) throws Exception {
        try (Main main = new Main()) {
            main.printInfo();
        }
    }

}
