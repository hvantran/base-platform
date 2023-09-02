package com.hoatv.fwk.common.ultilities;

import com.hoatv.fwk.common.services.BiCheckedConsumer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFileUtils {

    private ZipFileUtils() {}

    public static List<Path> getFilesFromZip(ZipInputStream zipInputStream) throws IOException {

        List<Path> fileOutputPaths = new ArrayList<>();
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            int size;
            byte[] buffer = new byte[2048];
            Path tempFile = Files.createTempFile("tmp", ".json");
            FileOutputStream fos = new FileOutputStream(tempFile.toFile());
            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

            while ((size = zipInputStream.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }
            fileOutputPaths.add(tempFile.toAbsolutePath());
            bos.flush();
            bos.close();
        }

        return fileOutputPaths;
    }

    public static byte[] zipFileContents(Map<String, String> fileContents) throws IOException {
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
