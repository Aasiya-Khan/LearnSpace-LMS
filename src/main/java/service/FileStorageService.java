package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileStorageService {

    public static final String VIDEOS = "uploads/videos";
    public static final String DOCUMENTS = "uploads/documents";

    public String copy(File source, String folder) throws IOException {
        if (source == null) return null;
        if (!source.isFile())
            throw new IllegalArgumentException("File not found: " + source.getName());

        Path dir = Paths.get(folder);
        Files.createDirectories(dir);
        String safeName = source.getName().replaceAll("[^A-Za-z0-9._-]", "_");
        Path target = dir.resolve(System.currentTimeMillis() + "_" + safeName);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString().replace('\\', '/');
    }
}