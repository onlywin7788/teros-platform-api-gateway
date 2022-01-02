package com.teros.api_gateway.common.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommonFile {
    public boolean fileExist(String filePath) {
        if (Files.exists(Paths.get(filePath)))
            return true;
        else
            return false;
    }

    public List<String> getFileList(String dir)
    {
        List fileList = new ArrayList();

        for (File info : FileUtils.listFiles(new File(dir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            fileList.add(info.getName());
        }
        return fileList;
    }

    public byte[] readFile(String ConfigPath) {
        byte[] contents = null;
        try {
            File file = new File(ConfigPath);
            contents = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public void writeFile(String filePath, String contents) {
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            byte[] strToBytes = contents.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
