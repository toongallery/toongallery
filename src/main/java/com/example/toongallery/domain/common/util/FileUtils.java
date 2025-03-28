package com.example.toongallery.domain.common.util;

public class FileUtils {

    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("파일 이름에 확장자가 없습니다: " + filename);
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
