package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.codegenerator.builders.BuildConstants;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.programs.asm_types.CTTypeUtils;
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

    private boolean isFunctionEntry(String dirStr) {
        return dirStr.equals(">");
    }

    public HashMap<String, ClassBuilder> build() throws BuildFailedException {
        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();
        String PKG_NAME = "com.abc.random";

        // Using a hardcoded calltrace for now, should be input arg in the future
        String filename = "./input_data/calltrace_Mandelbrot.txt";
        List<List<String>> fileLines = CTFileReader.getFileLines(filename);

        Stack<CallableDeclaration<?>> callStack = new Stack<>();

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
                CallableDeclaration<?> methodNode = this.addNewMethodToClass(methodName, methodArr, classCb);
                if (callStack.empty())
                    ; // System.out.println("First method call: " + methodNode.getName());
                else
                    addCallToMethodInMethod(callStack.lastElement(), methodNode);
                callStack.push(methodNode);
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
    private CallableDeclaration<?> addNewMethodToClass(String methodName,
                                                    List<String> methodArr,
                                                    ClassBuilder classCb) throws BuildFailedException {
        NodeList<Modifier> modifiers = this.getModifiersListFromScope(methodArr.get(SCOPE));

        // In the future, will need to contain info ; probably just a sleep() operation at first
        BlockStmt methodBody = new BlockStmt();

        String descriptor = methodArr.get(DESCRIPTOR);
        String[] splitDescriptor = descriptor.split("\\)");

        String paramsStr = splitDescriptor[0].substring(1);
        String returnValueStr = splitDescriptor[1];

        Type returnType = CTTypeUtils.getTypeFromStr(returnValueStr);

        NodeList<Parameter> parameters = new NodeList<>();

        for (Type paramType: CTTypeUtils.getTypesFromParametersStr(paramsStr)) {
            // TODO: check for duplicate param names, just in case (more of a hassle than it seems)
            String paramName = RandomUtils.generateRandomName(3);
            parameters.add(new Parameter(paramType, paramName));
        }

        if (methodName.equals(BuildConstants.CONSTRUCTOR_NAME))
            return classCb.addConstructor(parameters, methodBody, modifiers);
        else
            return classCb.addMethod(methodName, returnType, parameters, methodBody, modifiers);
    }

    private void addCallToMethodInMethod(CallableDeclaration<?> callerMethod, CallableDeclaration<?> calleeMethod) throws BuildFailedException {
        BlockStmt methodBody;

        // System.out.println(callerMethod.getName() + " calls " + calleeMethod.getName());

        if (callerMethod instanceof MethodDeclaration) {
            MethodDeclaration md = ((MethodDeclaration) callerMethod);
            if (md.getBody().isEmpty()) {
                methodBody = new BlockStmt();
                md.setBody(methodBody);
            } else {
                methodBody = md.getBody().get();
            }
        } else if (callerMethod instanceof ConstructorDeclaration)
            methodBody = ((ConstructorDeclaration) callerMethod).getBody();
        else
            throw new BuildFailedException("Method is neither a classic method nor a constructor");

        // String methodCallStatementStr = calleeMethod.getSignature().asString() + ";";
        String methodCallStatementStr = calleeMethod.getName().asString() + "()" + ";";
        // System.out.println(methodCallStatementStr);
        methodBody.addStatement(methodCallStatementStr);
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
