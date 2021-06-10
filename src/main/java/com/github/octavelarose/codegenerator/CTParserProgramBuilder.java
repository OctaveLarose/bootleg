package com.github.octavelarose.codegenerator;

import com.github.octavelarose.codegenerator.builders.ProgramBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * CallTrace Parser Program Builder.
 */
public class CTParserProgramBuilder implements ProgramBuilder {
    public static HashMap<String, ClassBuilder> build() {
        try {
            File ctFile = new File("./input_data/calltrace_Mandelbrot.txt");

            BufferedReader br = new BufferedReader(new FileReader(ctFile));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(Arrays.toString(line.split(" ")));
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
