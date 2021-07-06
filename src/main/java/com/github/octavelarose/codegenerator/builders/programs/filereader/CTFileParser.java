package com.github.octavelarose.codegenerator.builders.programs.filereader;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads and parses a calltrace file. Encapsulates the format used in them.
 */
public class CTFileParser {
    // CT line format example (each element separated by a space):
    // "< pub/con (Lcd/CallSign;Lcd/Vector3D;)V cd/Aircraft.<init> (54562ns)"
    public static int DIRECTION = 0;
    public static int SCOPE = 1;
    public static int DESCRIPTOR = 2;
    public static int FULLNAME = 3;
    public static int TIME = 4;

    private final String filename;
    private List<List<String>> fileLines;

    public CTFileParser(String filename) { this.filename = filename; }

    /**
     * Parses a calltrace file.
     * @return The instance of the CTFileParser calling.
     * @throws BuildFailedException If there was an I/O error related to the file.
     */
    public CTFileParser parse() throws BuildFailedException {
        try {
            File ctFile = new File(filename);
            BufferedReader br = new BufferedReader(new FileReader(ctFile));

            this.fileLines = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null)
                fileLines.add(Arrays.asList(line.split(" ")));

            return this;
        } catch (IOException e) {
            throw new BuildFailedException("Couldn't read file content: " + e.getMessage());
        }
    }

    public List<List<String>> getParsedCT() { return this.fileLines; }
}
