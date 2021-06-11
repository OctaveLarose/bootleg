package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
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
        String PKG_NAME = "com.abc.random";

        // Using a hardcoded calltrace for now, should be input arg in the future
        String filename = "./input_data/calltrace_Mandelbrot.txt";
        List<List<String>> fileLines = this.getFileLines(filename);

        for (List<String> methodArr: fileLines) {
            String[] splitFullName = methodArr.get(FULLNAME).split("\\.");
            String className = splitFullName[0];
            String methodName = splitFullName[1];

            ClassBuilder classCb;
            if (!classBuilders.containsKey(className)) {
                classCb = new BasicClassBuilder(className, 0, 0, PKG_NAME);
                classBuilders.put(className, classCb);
            } else {
                classCb = classBuilders.get(className);
            }

            if(!classCb.hasMethod(methodName))
                this.addNewMethodToClass(methodName, methodArr, classCb);

//            System.out.println(className + " " + methodName);
        }

        return classBuilders;
    }

    /**
     * Adds a new method to a class, setting adequate parameters beforehand.
     * @param methodName The name of the method.
     * @param methodArr Info about the method in general.
     * @param classCb The class(builder) to which it needs to be added.
     */
    private void addNewMethodToClass(String methodName, List<String> methodArr, ClassBuilder classCb) {
        NodeList<Modifier> modifiers = this.getModifiersListFromScope(methodArr.get(SCOPE));

        // In the future, will need to contain info ; probably just a sleep() operation at first
        BlockStmt methodBody = new BlockStmt();

        // Will need to be fetched from the descriptor.
        // ..."Type.getArgumentTypes(desc)" could be useful, it's what the ASM lib uses internally at some point.
        // https://asm.ow2.io/faq.html#Q7 The explanation for the syntax
        Type returnType = new VoidType();

        // Will need to be fetched from the descriptor as well.
        NodeList<Parameter> parameters = new NodeList<>();

        classCb.addMethod(methodName, returnType, parameters, methodBody, modifiers);
    }

    /**
     * @param scope A string defining the method's scope (public, private...)
     * @return a NodeList of Modifier objects, corresponding to the input scope
     */
    private NodeList<Modifier> getModifiersListFromScope(String scope) {
        NodeList<Modifier> modifiers;

        switch (scope) {
            case "pub":
                modifiers = new NodeList<>(Modifier.publicModifier());
                break;
            case "pri":
                modifiers = new NodeList<>(Modifier.privateModifier());
                break;
            case "pro":
                modifiers = new NodeList<>(Modifier.protectedModifier());
                break;
            default:
                modifiers = new NodeList<>();
        }
        return modifiers;
    }
}
