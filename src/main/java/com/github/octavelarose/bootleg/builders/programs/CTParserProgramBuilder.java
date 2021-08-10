package com.github.octavelarose.bootleg.builders.programs;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.utils.Pair;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.calltraces.CTMethodInfo;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.calltraces.CallInterpreter;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.fileparsers.ArithmeticOperationsFileParser;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.fileparsers.CTFileParser;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * CallTrace Parser Program Builder.
 * Generates a program from a calltrace file of a format I defined myself.
 */
public class CTParserProgramBuilder implements ProgramBuilder {
    private final List<List<String>> callFileLines;
    private HashMap<String, List<String>> methodOperations;

    public CTParserProgramBuilder(String ctFileName) throws BuildFailedException {
        System.out.println("Generating a program from the calltrace file: " + ctFileName);
        this.callFileLines = new CTFileParser(ctFileName).parse().getParsedCT();
    }

    /**
     * Provide an optional file describing operations executed by a method.
     * @param opsFileName The name of the operations file
     * @throws BuildFailedException If parsing the operations file fails.
     */
    public void setOperationsFileName(String opsFileName) throws BuildFailedException {
        System.out.println("Optional operations file provided: " + opsFileName);
        this.methodOperations = new ArithmeticOperationsFileParser(opsFileName).parse().getParsedArithmeticOps();
    }

    /**
     * @param shouldPrintMethodNames Represents whether the method names should be printed when entering each method.
     */
    public void shouldPrintMethodNames(boolean shouldPrintMethodNames) {
        CallInterpreter.setShouldPrintMethodNames(shouldPrintMethodNames);
    }

    public HashMap<String, ClassBuilder> build() throws BuildFailedException {
        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();
        Stack<Pair<ClassBuilder, CallableDeclaration.Signature>> callStack = new Stack<>();

        for (List<String> methodArr: this.callFileLines) {
            CTMethodInfo ctMethodInfo = new CTMethodInfo(methodArr);
            if (this.methodOperations != null && this.methodOperations.get(ctMethodInfo.get(CTMethodInfo.FULLNAME)) != null)
                ctMethodInfo.setMethodOperations(this.methodOperations.get(ctMethodInfo.get(CTMethodInfo.FULLNAME)));

            CallInterpreter ctInterpreter = new CallInterpreter(ctMethodInfo, classBuilders, callStack);
            ctInterpreter.execute();
        }

        return classBuilders;
    }
}
