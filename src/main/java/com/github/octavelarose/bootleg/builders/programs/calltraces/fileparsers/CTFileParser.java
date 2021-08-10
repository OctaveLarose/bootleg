package com.github.octavelarose.bootleg.builders.programs.calltraces.fileparsers;

import com.github.octavelarose.bootleg.builders.BuildFailedException;

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
