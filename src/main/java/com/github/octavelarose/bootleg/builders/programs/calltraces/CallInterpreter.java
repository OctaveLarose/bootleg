package com.github.octavelarose.bootleg.builders.programs.calltraces;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.octavelarose.bootleg.builders.BuildConstants;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.calltraces.asm_types.ASMParsingException;
import com.github.octavelarose.bootleg.builders.programs.calltraces.asm_types.ASMTypeParsingUtils;
import com.github.octavelarose.bootleg.builders.programs.classes.BasicClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.MethodCallInstructionWriter;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.CallableMethodBodyEditor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.SimpleMethodBodyCreator;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.github.octavelarose.bootleg.builders.BuildConstants.PARAM_NAME_LENGTH;

/**
 * Interprets a single call.
 */
public class CallInterpreter {
    final CTMethodInfo ctMethodInfo;
    final HashMap<String, ClassBuilder> classBuilders;
    final Stack<Pair<ClassBuilder, CallableDeclaration.Signature>> callStack;

    static boolean shouldPrintMethodNames = true;

    private ClassBuilder classCb;

    public CallInterpreter(CTMethodInfo ctMethodInfo,
                           HashMap<String, ClassBuilder> classBuilders,
                           Stack<Pair<ClassBuilder, CallableDeclaration.Signature>> callStack) {
        this.ctMethodInfo = ctMethodInfo;
        this.classBuilders = classBuilders;
        this.callStack = callStack;
    }

    public static void setShouldPrintMethodNames(boolean shouldPrintMethodNames) {
        CallInterpreter.shouldPrintMethodNames = shouldPrintMethodNames;
    }

    /**
     * Does operations related to interpreting a single call from our calltrace.
     * @throws BuildFailedException If something goes wrong in the program building during operations related to the current method call.
     */
    public void execute() throws BuildFailedException {
        // We ignore lambda calls for now.
        if (ctMethodInfo.isLambda())
            return;

        ctMethodInfo.modifyIfStaticInit();

        this.classCb = getOrCreateClassBuilder(classBuilders, ctMethodInfo.getClassName());

        // If it's a method exit, we add a return statement and we go to the next one.
        if (!ctMethodInfo.isFunctionEntry()) {
            this.addReturnStatement();
            callStack.pop();
            return;
        }

        // If the method already exists, we don't need to generate it and just modify the call stack.
        String methodName = ctMethodInfo.getMethodName();
        if (classCb.hasMethod(methodName)) {
            callStack.push(new Pair<>(classCb, classCb.getMethodFromName(methodName).getSignature()));
            return;
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

    /**
     * Adds a return statement at the end of the method body.
     * @throws BuildFailedException If something goes wrong when modifying the method body.
     */
    private void addReturnStatement() throws BuildFailedException {
        CallableMethodBodyEditor cmbe = new CallableMethodBodyEditor(classCb.getMethodFromName(ctMethodInfo.getMethodName()), classCb);
        Type methodReturnType = ASMTypeParsingUtils.getTypeFromStr(ctMethodInfo.getReturnTypeStr());

        if (methodReturnType.isVoidType() || cmbe.hasReturnStatement())
            return;

        if (!cmbe.setReturnStatementFromLocalVar(methodReturnType)) {
            String className = ctMethodInfo.getReturnTypeStr().substring(1, ctMethodInfo.getReturnTypeStr().length() - 1);
            if (className.startsWith("java/"))
                cmbe.setReturnStatement(new ReturnStmt(new NullLiteralExpr()));
            else
                cmbe.setReturnStatementAsNewClass(classBuilders.get(className));
        }

        cmbe.setBodyToCallable();
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
        String methodName = ctMethodInfo.getMethodName();
        Type returnType = ASMTypeParsingUtils.getTypeFromStr(ctMethodInfo.getReturnTypeStr());
        NodeList<Parameter> parameters = this.getParameters();
        BlockStmt methodBody = this.getInitialMethodBody(parameters);
        NodeList<Modifier> modifiers = ctMethodInfo.getScopeModifiersList();

        if (methodName.equals(BuildConstants.CONSTRUCTOR_NAME))
            return classCb.addConstructor(parameters, methodBody, modifiers);
        else
            return classCb.addMethod(methodName, returnType, parameters, methodBody, modifiers);
    }

    /**
     * @return The parameters of the method corresponding to the call.
     * @throws ASMParsingException If the parsing of the parameter string fails.
     */
    private NodeList<Parameter> getParameters() throws ASMParsingException {
        NodeList<Parameter> parameters = new NodeList<>();
        for (Type paramType: ASMTypeParsingUtils.getTypesFromParametersStr(ctMethodInfo.getParamsStr())) {
            // We don't check for duplicate parameter names since the odds are very low with a long enough length
            String paramName = RandomUtils.generateRandomName(PARAM_NAME_LENGTH);
            parameters.add(new Parameter(paramType, paramName));
        }

        return parameters;
    }

    /**
     * Generates an initial method body, which usually only contains a print operation with the method's name.
     * @param parameters The method parameters, which may need to be used by the method body generator.
     * @return A series of instructions corresponding to the method's body
     * @throws BuildFailedException If the format of the operations are invalid.
     */
    private BlockStmt getInitialMethodBody(NodeList<Parameter> parameters) throws BuildFailedException {
        SimpleMethodBodyCreator smbc = new SimpleMethodBodyCreator();

        if (shouldPrintMethodNames)
            smbc.addDefaultStatements(ctMethodInfo.get(CTMethodInfo.FULLNAME));

        if (ctMethodInfo.hasMethodOperations()) {
            smbc.setMethodParameters(parameters);
            smbc.processOperationStatements(ctMethodInfo.getMethodOperations());
        }

        return smbc.getMethodBody();
    }
}
