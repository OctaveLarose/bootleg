package com.github.octavelarose.codegenerator.builders.programs.filereader;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CTFileReader {
    static public List<List<String>> getFileLines(String filename) throws BuildFailedException {
        try {
            File ctFile = new File(filename);
            BufferedReader br = new BufferedReader(new FileReader(ctFile));

            List<List<String>> fileLines = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null)
                fileLines.add(Arrays.asList(line.split(" ")));

            return fileLines;
        } catch (IOException e) {
            throw new BuildFailedException("Couldn't read file content: " + e.getMessage());
        }
    }
}
