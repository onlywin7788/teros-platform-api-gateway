package com.teros.api_gateway.common.file;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class CommonFile {
    public boolean fileExist(String filePath) {
        if (Files.exists(Paths.get(filePath)))
            return true;
        else
            return false;
    }

    public String readFile(String ConfigPath) {
        String contents = "";
        try {
            File file = new File(ConfigPath);
            contents = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
}
