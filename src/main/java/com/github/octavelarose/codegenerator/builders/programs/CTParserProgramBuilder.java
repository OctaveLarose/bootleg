package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * CallTrace Parser Program Builder.
 * Generates a program from a calltrace file of a format I defined myself.
 */
public class CTParserProgramBuilder implements ProgramBuilder {
    public static int DIRECTION = 0;
    public static int SCOPE = 1;
    public static int DESCRIPTOR = 2;
    public static int FULLNAME = 3;
    public static int TIME = 4;

    private List<List<String>> getFileLines(String filename) throws BuildFailedException {
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

    public HashMap<String, ClassBuilder> build() throws BuildFailedException {
        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();

        // Using a hardcoded calltrace for now, should be input arg in the future
        String filename = "./input_data/calltrace_Mandelbrot.txt";
        List<List<String>> fileLines = this.getFileLines(filename);

        for (List<String> methodArr: fileLines) {
            String[] splitFullName = methodArr.get(FULLNAME).split("\\.");
            String className = splitFullName[0];
            String methodName = splitFullName[1];

            ClassBuilder classCb;
            if (!classBuilders.containsKey(className)) {
                classCb = new BasicClassBuilder(className, 0, 0, "com.abc.random");
                classBuilders.put(className, classCb);
            } else {
                classCb = classBuilders.get(className);
            }

            if(!classCb.hasMethod(methodName)) {
                BlockStmt methodBody = new BlockStmt();
                // TODO: Can take access modifiers into account.
                // TODO: Can take return value/parameters into account. (a hassle, need to figure out the syntax used by DiSL, i.e "(LBenchmark;)V")
                classCb.addMethod(methodName, new VoidType(), new NodeList<>(), methodBody, new NodeList<>());
            }

//            System.out.println(className + " " + methodName);
        }

        return classBuilders;
    }
}
