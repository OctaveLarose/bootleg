package com.github.octavelarose.bootleg.builders.programs;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.octavelarose.bootleg.builders.BuildConstants;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.BasicClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.MethodCallInstructionWriter;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.CallableMethodBodyEditor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.SimpleMethodBodyCreator;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.asm_types.ASMTypeParsingUtils;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.calltraces.CTMethodInfo;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.fileparsers.ArithmeticOperationsFileParser;
import com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.fileparsers.CTFileParser;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.github.octavelarose.bootleg.builders.BuildConstants.PARAM_NAME_LENGTH;

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
    public CTParserProgramBuilder setOperationsFileName(String opsFileName) throws BuildFailedException {
        System.out.println("Optional operations file provided: " + opsFileName);
        this.methodOperations = new ArithmeticOperationsFileParser(opsFileName).parse().getParsedArithmeticOps();
        return this;
    }

    public HashMap<String, ClassBuilder> build() throws BuildFailedException {
        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();
        Stack<Pair<ClassBuilder, CallableDeclaration.Signature>> callStack = new Stack<>();
        int idx = 0; // Used for debugging

        for (List<String> methodArr: this.callFileLines) {
            idx++;

            CTMethodInfo ctMethodInfo = new CTMethodInfo(methodArr);
            if (this.methodOperations != null && this.methodOperations.get(ctMethodInfo.get(CTMethodInfo.FULLNAME)) != null)
                ctMethodInfo.setMethodOperations(this.methodOperations.get(ctMethodInfo.get(CTMethodInfo.FULLNAME)));

            // We ignore lambda calls for now.
            if (ctMethodInfo.isLambda())
                continue;

            ctMethodInfo.modifyIfStaticInit();

            ClassBuilder classCb = getOrCreateClassBuilder(classBuilders, ctMethodInfo.getClassName());

            // If it's a method exit, we add a return statement and we go to the next one.
            if (!ctMethodInfo.isFunctionEntry()) {
                CallableMethodBodyEditor cmbe = new CallableMethodBodyEditor(classCb.getMethodFromName(ctMethodInfo.getMethodName()), classCb);
                Type methodReturnType = ASMTypeParsingUtils.getTypeFromStr(ctMethodInfo.getReturnTypeStr());
                if (!methodReturnType.isVoidType() && !cmbe.hasReturnStatement()) {
                    cmbe.setReturnStatementFromLocalVar(methodReturnType); // TODO If there's none available, we RETURN A CONSTRUCTOR CALL
                    cmbe.setBodyToCallable();
                }
                callStack.pop();
                continue;
            }

            // If the method already exists, we don't need to generate it and just modify the call stack.
            String methodName = ctMethodInfo.getMethodName();
            if (classCb.hasMethod(methodName)) {
                callStack.push(new Pair<>(classCb, classCb.getMethodFromName(methodName).getSignature()));
                continue;
            }

            CallableDeclaration<?> methodNode = this.addNewMethodToClassFromCTInfo(ctMethodInfo, classCb);

            if (callStack.empty()) {
                System.out.println("Entry point: " + ctMethodInfo.get(CTMethodInfo.FULLNAME));
            } else {
                MethodCallInstructionWriter mciw = new MethodCallInstructionWriter()
                        .setCaller(callStack.lastElement().a, callStack.lastElement().b)
                        .setCallee(classCb, methodNode.getSignature())
                        .setOtherClassesContext(classBuilders);
                mciw.writeMethodCallInCaller();
            }

            callStack.push(new Pair<>(classCb, methodNode.getSignature()));
        }

        return classBuilders;
    }

    /**
     * Fetches a ClassBuilder with a given name from the already instantiated ClassBuilder list, or creates it accordingly
     * @param classBuilders The HashMap containing the ClassBuilders
     * @param className The name of the class wrapped in the ClassBuilder
     * @return The already existing, or newly created ClassBuilder object
     */
    private ClassBuilder getOrCreateClassBuilder(HashMap<String, ClassBuilder> classBuilders, String className) {
        ClassBuilder classCb;

        if (classBuilders.containsKey(className)) {
            classCb = classBuilders.get(className);
        } else {
            if (!className.contains("/"))
                classCb = new BasicClassBuilder(className);
            else {
                List<String> splitClassPath = Arrays.asList(className.split("/"));
                String pkgPath = String.join(".", splitClassPath.subList(0, splitClassPath.size() - 1));
                classCb = new BasicClassBuilder(splitClassPath.get(splitClassPath.size() - 1), 0, 0, pkgPath);
            }
            classBuilders.put(className, classCb);
        }

        return classCb;
    }


    /**
     * Adds a new method to a class, setting adequate parameters beforehand.
     * @param ctMethodInfo The class wrapping the CT call info / method info.
     * @param classCb The class(builder) to which it needs to be added.
     */
    private CallableDeclaration<?> addNewMethodToClassFromCTInfo(CTMethodInfo ctMethodInfo,
                                                                 ClassBuilder classCb) throws BuildFailedException {
        String paramsStr = ctMethodInfo.getParamsStr();
        Type returnType = ASMTypeParsingUtils.getTypeFromStr(ctMethodInfo.getReturnTypeStr());
        NodeList<Modifier> modifiers = ctMethodInfo.getScopeModifiersList();

        SimpleMethodBodyCreator smbc = new SimpleMethodBodyCreator()
                                .addDefaultStatements(ctMethodInfo.get(CTMethodInfo.FULLNAME));

        NodeList<Parameter> parameters = new NodeList<>();

        for (Type paramType: ASMTypeParsingUtils.getTypesFromParametersStr(paramsStr)) {
            // We don't check for duplicate parameter names since the odds are very low with a long enough length
            String paramName = RandomUtils.generateRandomName(PARAM_NAME_LENGTH);
            parameters.add(new Parameter(paramType, paramName));
        }

        if (ctMethodInfo.hasMethodOperations()) {
            smbc.setMethodParameters(parameters);
            smbc.processOperationStatements(ctMethodInfo.getMethodOperations());
        }

        BlockStmt methodBody = smbc.getMethodBody();

        if (ctMethodInfo.getMethodName().equals(BuildConstants.CONSTRUCTOR_NAME))
            return classCb.addConstructor(parameters, methodBody, modifiers);
        else
            return classCb.addMethod(ctMethodInfo.getMethodName(), returnType, parameters, methodBody, modifiers);
    }
}
