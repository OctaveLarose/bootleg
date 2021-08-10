package com.github.octavelarose.bootleg.builders.programs.calltraces.fileparsers;

import com.github.octavelarose.bootleg.builders.BuildFailedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Reads and parses an arithmetic operations file.
 */
public class ArithmeticOperationsFileParser {
    private final String filename;
    private HashMap<String, List<String>> opsMap;

    public ArithmeticOperationsFileParser(String filename) { this.filename = filename; }

    /**
     * Parses a calltrace file.
     * @return The instance of the CTFileParser calling.
     * @throws BuildFailedException If there was an I/O error related to the file.
     */
    public ArithmeticOperationsFileParser parse() throws BuildFailedException {
        try {
            File ctFile = new File(filename);
            BufferedReader br = new BufferedReader(new FileReader(ctFile));

            this.opsMap = new HashMap<>();

            String line;
            while ((line = br.readLine()) != null)
                opsMap.put(line, Arrays.asList(br.readLine().split(" ")));

            return this;
        } catch (IOException e) {
            throw new BuildFailedException("Couldn't read file content: " + e.getMessage());
        }
    }

    public HashMap<String, List<String>> getParsedArithmeticOps() { return this.opsMap; }
}
