package com.hoatv.fwk.common.ultilities;

import com.hoatv.fwk.common.services.BiCheckedConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipFileUtils.class);

    private ZipFileUtils() {}

    public static void processZipFile(ZipInputStream zipInputStream, Consumer<List<Path>> fileHandler) throws IOException {
        List<Path> filesFromZip = new ArrayList<>();
        try {
            filesFromZip = getFilesFromZip(zipInputStream);
            fileHandler.accept(filesFromZip);
        } finally {
            if (!filesFromZip.isEmpty()) {

                filesFromZip.forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException exception) {
                        LOGGER.error("An exception occurred while deleting file {}", p.getFileName(), exception);
                    }
                });
            }
        }
    }

    public static List<Path> getFilesFromZip(ZipInputStream zipInputStream) throws IOException {

        List<Path> fileOutputPaths = new ArrayList<>();
        while ((zipInputStream.getNextEntry()) != null) {
            int size;
            byte[] buffer = new byte[2048];
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            File tempFile = Files.createTempFile("tmp", ".json", attr).toFile();
            FileOutputStream fos = new FileOutputStream(tempFile);

            try (BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {
                while ((size = zipInputStream.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                fileOutputPaths.add(tempFile.toPath().toAbsolutePath());
                bos.flush();
            }
        }

        return fileOutputPaths;
    }

    public static byte[] zipFileContents(Map<String, String> fileContents) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        BiCheckedConsumer<String, String> contentToFileFunction = (fileName, fileContent) -> {
            ZipEntry entry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write(fileContent.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        };
        fileContents.forEach(contentToFileFunction);
        return byteArrayOutputStream.toByteArray();
    }
}
