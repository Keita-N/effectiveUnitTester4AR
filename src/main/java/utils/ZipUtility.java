package utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author kazua
 * @author k.yanagida
 */
public class ZipUtility {

    public static File compress(String targetDirPath, String zipFilePath) {
        File zipFile = null;
        ZipOutputStream zos = null;
        try {
            zipFile = new File(zipFilePath);
            if (!zipFile.getParentFile().exists()) zipFile.getParentFile().mkdirs();
            zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
            File targetDir = new File(targetDirPath);
            File[] targetFiles = targetDir.listFiles();
            for (int i = 0; i < targetFiles.length; i++) {
                compress(zos, zipFile, targetFiles[i], "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.flush();
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return zipFile;
    }

    private static void compress(ZipOutputStream zos, File zipFile, File targetFile, String hrc) throws IOException {
        if (targetFile.isDirectory()) {
            File[] files = targetFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                compress(zos, zipFile, files[i], hrc + targetFile.getName() + "/");
            }
        } else if (!zipFile.getCanonicalFile().equals(targetFile.getCanonicalFile())) {
            BufferedInputStream bis = null;
            try {
                ZipEntry entry = new ZipEntry(hrc
                        + targetFile.getName().replace("\\", "/"));
                zos.putNextEntry(entry);
                byte buf[] = new byte[1024];
                int size;
                bis = new BufferedInputStream(
                        new FileInputStream(targetFile.getPath()));
                while ((size = bis.read(buf, 0, 1024)) != -1) {
                    zos.write(buf, 0, size);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (zos != null) {
                    try {
                        zos.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
