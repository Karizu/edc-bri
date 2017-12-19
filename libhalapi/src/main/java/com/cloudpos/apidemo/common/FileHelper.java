
package com.cloudpos.apidemo.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    private static final int BUFFER_SIZE = 4096;

    private static void checkFileNullByPath(File file) throws Exception {

        if (file == null || file.length() == 0) {
            throw new Exception("file is null");
        }
    }

    public static byte[] read(String filePath) throws Exception {
        File file = new File(filePath);
        checkFileNullByPath(file);
        byte[] fileContent = new byte[(int) file.length()];
        InputStream is = null;
        int startPosition = 0;
        try {
            is = new FileInputStream(filePath);
            int len = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = is.read(buffer)) != -1) {
                System.arraycopy(buffer, 0, fileContent, startPosition, len);
                startPosition += len;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return fileContent;
    }

    public static void save(String filePath, byte[] buffer) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checkout whether the dir exists. If not, create it.
     */
    public static void checkAndCreateDirection(String folderPath) {
        File dirFile = new File(folderPath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    /**
     * Checkout whether the file exists. If exist, delete it.
     */
    public static void checkAndDeleteFileByPath(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Checkout whether the file or folder exist by path
     */
    public static boolean isFileExists(String filePath) {
        boolean isExists = false;
        File file = new File(filePath);
        if (file.exists()) {
            isExists = true;
        }
        return isExists;
    }

    // /**
    // * checkout the latest download time<br/>
    // * if over threshold, throw exception
    // */
    // public static void checkLastModified(String filePath) throws Exception {
    // File file = new File(filePath);
    // if ((System.currentTimeMillis() - file.lastModified()) >
    // Constants.REFRESH_INTERVAL) {
    // throw new Exception("last modified is too long");
    // }
    // }

    public static void checkFolderOrFileNotExists(String folderPath, String filePath)
            throws Exception {
        if (!isFileExists(folderPath) || !isFileExists(filePath)) {
            throw new Exception("folder or file not exists");
        }
    }

    /**
     * Checkout whether the folder exists.<br/>
     * If exists, checkout whether the number of files in the folder over the minimum value.<br/>
     * Otherwise, throw exception.
     */
    public static void checkFolderAndFileCount(String folderPath, int minCount) throws Exception {
        if (!isFileExists(folderPath)) {
            throw new Exception("folder or file not exists");
        } else {
            checkFolderFileCounts(folderPath, minCount);
        }
    }

    private static void checkFolderFileCounts(String folderPath, int minCount) throws Exception {
        File folder = new File(folderPath);
        if (folder.listFiles().length <= minCount) {
            throw new Exception("folder do not meet requirements");
        }
    }

    /**
     * Get the number of files in the folder. 
     */
    public static int getFileCounts(String folderPath) {
        int fileCount = 0;
        File folder = new File(folderPath);
        fileCount = folder.listFiles().length;
        return fileCount;
    }

    public static void checkFileNameAndFileCount(String folderPath) throws Exception {
        if (getMaxFileName(folderPath) != (getFileCounts(folderPath) - 1)) {
            throw new Exception("File name do not meet file counts in this folder");
        }
    }

    /**
     * With the precondition that files in the folder were named after numbers, <br/>
     * calculate the largest filename.
     * 
     * @return
     */
    private static int getMaxFileName(String folderPath) {
        int maxName = 1;
        File folder = new File(folderPath);
        for (File file : folder.listFiles()) {
            int fileNameToInt = Integer.parseInt(file.getName());
            if (fileNameToInt > maxName) {
                maxName = fileNameToInt;
            }
        }
        return maxName;
    }
}
