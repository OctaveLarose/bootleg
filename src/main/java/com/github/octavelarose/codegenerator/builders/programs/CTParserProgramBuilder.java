package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.octavelarose.codegenerator.builders.BuildConstants;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.instructions.DummyValueCreator;
import com.github.octavelarose.codegenerator.builders.classes.instructions.MethodCallInstructionWriter;
import com.github.octavelarose.codegenerator.builders.programs.asm_types.ASMTypeParsingUtils;
import com.github.octavelarose.codegenerator.builders.programs.filereader.CTFileReader;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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

    private final String ctFileName;

    public CTParserProgramBuilder(String ctFileName) {
        this.ctFileName = ctFileName;
    }

    private boolean isFunctionEntry(String dirStr) {
        return dirStr.equals(">");
    }

    public HashMap<String, ClassBuilder> build() throws BuildFailedException {
        System.out.println("Generating a program from the calltrace file: " + this.ctFileName);

        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();
        String PKG_NAME = "com.abc.random";

        List<List<String>> fileLines = CTFileReader.getFileLines(ctFileName);

        Stack<Pair<ClassBuilder, CallableDeclaration.Signature>> callStack = new Stack<>();

        for (List<String> methodArr: fileLines) {
            if (!this.isFunctionEntry(methodArr.get(DIRECTION))) {
                callStack.pop();
                continue;
            }

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

            if (!classCb.hasMethod(methodName)) {
                CallableDeclaration<?> methodNode = this.addNewMethodToClassFromCTInfo(methodName, methodArr, classCb);
                if (callStack.empty()) {
                    System.out.println("Entry point: " + methodArr.get(FULLNAME));
                } else {
                    MethodCallInstructionWriter mciw = new MethodCallInstructionWriter(
                            callStack.lastElement().a, callStack.lastElement().b,
                            classCb, methodNode.getSignature());
                    mciw.writeMethodCallInCaller();
                }

                callStack.push(new Pair<>(classCb, methodNode.getSignature()));
            }
        }

        return classBuilders;
    }

    /**
     * Adds a new method to a class, setting adequate parameters beforehand.
     * @param methodName The name of the method.
     * @param methodArr Info about the method in general.
     * @param classCb The class(builder) to which it needs to be added.
     */
    private CallableDeclaration<?> addNewMethodToClassFromCTInfo(String methodName,
                                                                 List<String> methodArr,
                                                                 ClassBuilder classCb) throws BuildFailedException {
        NodeList<Modifier> modifiers = this.getModifiersListFromScope(methodArr.get(SCOPE));

        String descriptor = methodArr.get(DESCRIPTOR);
        String[] splitDescriptor = descriptor.split("\\)");

        String paramsStr = splitDescriptor[0].substring(1);
        Type returnType = ASMTypeParsingUtils.getTypeFromStr(splitDescriptor[1]);

        // In the future, should ideally contain "advanced" operations. Needs a sleep() operation for starters
        BlockStmt methodBody = new BlockStmt();

        methodBody.addStatement(new NameExpr("System.out.println(\"" + "Current method: " + methodArr.get(FULLNAME) + "\")"));
        if (!returnType.isVoidType())
            methodBody.addStatement(new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));

        NodeList<Parameter> parameters = new NodeList<>();

        for (Type paramType: ASMTypeParsingUtils.getTypesFromParametersStr(paramsStr)) {
            // TODO: check for duplicate param names, just in case (more of a hassle than it seems)
            String paramName = RandomUtils.generateRandomName(3);
            parameters.add(new Parameter(paramType, paramName));
        }

        if (methodName.equals(BuildConstants.CONSTRUCTOR_NAME))
            return classCb.addConstructor(parameters, methodBody, modifiers);
        else
            return classCb.addMethod(methodName, returnType, parameters, methodBody, modifiers);
    }

    /**
     * Returns modifiers given a string defining a scope.
     * Needs a definition of the syntax I use somewhere, since it's my own standard.
     * @param scope A string defining the method's scope (public, private...).
     * @return a NodeList of Modifier objects, corresponding to the input scope
     */
    private NodeList<Modifier> getModifiersListFromScope(String scope) {
        NodeList<Modifier> modifiers = new NodeList<>();
        String[] splitScope = scope.split("/");

        for (String modStr: splitScope) {
            if (modStr.equals("pub"))
                modifiers.add(Modifier.publicModifier());
            if (modStr.equals("pri"))
                modifiers.add(Modifier.privateModifier());
            if (modStr.equals("pro"))
                modifiers.add(Modifier.protectedModifier());
            if (modStr.equals("sta"))
                modifiers.add(Modifier.staticModifier());
        }

        return modifiers;
    }
}
