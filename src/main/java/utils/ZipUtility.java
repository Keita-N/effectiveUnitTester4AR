package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author saka-en.
 */
public class ZipUtility {

    public static File compressDirectory(String zipFilePath, String targetDirPath) {
        File zipFile = new File(zipFilePath);
        File targetDir = new File(targetDirPath);
        ZipOutputStream outZip = null;
        try {
            // ZIPファイル出力オブジェクト作成
            outZip = new ZipOutputStream(new FileOutputStream(zipFile));
            archive(outZip, zipFile, targetDir);
            return zipFile;
        } catch (Exception e) {
            return null;
        } finally {
            // ZIPエントリクローズ
            if (outZip != null) {
                try {
                    outZip.closeEntry();
                } catch (Exception e) {
                }
                try {
                    outZip.flush();
                } catch (Exception e) {
                }
                try {
                    outZip.close();
                } catch (Exception e) {
                }
            }
        }
    }


    private static void archive(ZipOutputStream outZip, File zipFile, File targetFile) {
        if (targetFile.isDirectory()) {
            File[] files = targetFile.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    archive(outZip, zipFile, f);
                } else {
                    if (!f.getAbsoluteFile().equals(zipFile.getAbsoluteFile())) {
                        // 圧縮処理
                        archive(outZip, f, f.getName());
                    }
                }
            }
        }
    }

    private static boolean archive(ZipOutputStream outZip, File targetFile, String entryName) {
        // 圧縮レベル設定
        outZip.setLevel(5);

        // 文字コードを指定
//        outZip.setEncoding(enc);
        try {

            // ZIPエントリ作成
            outZip.putNextEntry(new ZipEntry(entryName));

            // 圧縮ファイル読み込みストリーム取得
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(targetFile));

            // 圧縮ファイルをZIPファイルに出力
            int readSize = 0;
            byte buffer[] = new byte[1024]; // 読み込みバッファ
            while ((readSize = in.read(buffer, 0, buffer.length)) != -1) {
                outZip.write(buffer, 0, readSize);
            }
            // クローズ処理
            in.close();
            // ZIPエントリクローズ
            outZip.closeEntry();
        } catch (Exception e) {
            // ZIP圧縮失敗
            return false;
        }
        return true;
    }
}
