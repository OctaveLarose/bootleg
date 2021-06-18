package com.github.octavelarose.codegenerator.builders.classes.instructions;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Writes a call to one method in another, i.e "methodName(3, "hello", 1.2);"
 * TODO: needs to take classes into account, currently only method names and no classes are mentioned/instantiated.
 */
public class MethodCallInstructionWriter {
    ClassBuilder callerClass;
    ClassBuilder calleeClass;
    CallableDeclaration<?> callerMethod;
    CallableDeclaration<?> calleeMethod;

    public MethodCallInstructionWriter(ClassBuilder callerClass, CallableDeclaration.Signature callerMethodSignature,
                                       ClassBuilder calleeClass, CallableDeclaration.Signature calleeMethodSignature) {
        this.callerMethod = callerClass.getMethodFromSignature(callerMethodSignature);
        this.calleeMethod = calleeClass.getMethodFromSignature(calleeMethodSignature);
        this.callerClass = callerClass;
        this.calleeClass = calleeClass;
    }

    public void writeMethodCallInCaller() throws BuildFailedException {
        BlockStmt methodBody;

        // System.out.println(callerMethod.getName() + " calls " + calleeMethod.getName());

        if (callerMethod instanceof MethodDeclaration) {
            MethodDeclaration md = ((MethodDeclaration) callerMethod);
            if (md.getBody().isEmpty()) { // Should never happen since methods are always instantiated with an empty block
                methodBody = new BlockStmt();
                md.setBody(methodBody);
            } else {
                methodBody = md.getBody().get();
            }
        } else if (callerMethod instanceof ConstructorDeclaration)
            methodBody = ((ConstructorDeclaration) callerMethod).getBody();
        else
            throw new BuildFailedException("Method is neither a classic method nor a constructor");

        String methodCallStatementStr = getMethodSignatureWithValues(calleeMethod.getSignature().asString());

        // TODO make this a method call to a class since it's duplicated a few times here and there in my codebase
        Optional<ClassOrInterfaceType> classWithName = new JavaParser()
                .parseClassOrInterfaceType(calleeClass.getName())
                .getResult();

        if (classWithName.isEmpty())
            throw new BuildFailedException("Couldn't parse class " + classWithName);

        // TODO argument handling, also for the constructor call.
        if (callerClass == calleeClass) {
            methodBody.addStatement(
                    new MethodCallExpr(
                            new ThisExpr(),
                            calleeMethod.getName())
            );
        } else {
//            System.out.println(callerClass.getName() + ":" + callerMethod.getName() + ", "
//                    + calleeClass.getName() + ":" + calleeMethod.getName());

            if (calleeMethod instanceof ConstructorDeclaration) {
                // TODO add import statement else it won't run
                methodBody.addStatement(0, new VariableDeclarationExpr(
                        new VariableDeclarator(classWithName.get(), calleeClass.getName().toLowerCase(),
                                new ObjectCreationExpr().setType(classWithName.get())))
                );
            } else {
                methodBody.addStatement(
                        new MethodCallExpr(
                                new NameExpr(calleeClass.getName().toLowerCase()),
                                calleeMethod.getName())
                );
            }

            // Ugly safeguard. If an object is returned from a method call, or if it's in a field, it can't be detected
            // So for now I'll just create a new instance of it in every method that needs it. TODO improve, but how?
            if (!isClassInstantiationInMethod(methodBody, calleeClass.getName())) {
                methodBody.addStatement(0, new VariableDeclarationExpr(
                        new VariableDeclarator(classWithName.get(), calleeClass.getName().toLowerCase(),
                                new ObjectCreationExpr().setType(classWithName.get())))
                );
            }
        }
    }

    /**
     * Is there a local variable in the method that corresponds to an instantiation of a given class?
     * @param methodBody The body of the method.
     * @param className The name of the class.
     * @return true if a variable was instantiated with the type className, false otherwise.
     */
    private boolean isClassInstantiationInMethod(BlockStmt methodBody, String className) {
        for (Statement methodLine: methodBody.getStatements()) {
            if (!(methodLine.asExpressionStmt().getExpression() instanceof VariableDeclarationExpr))
                continue;

            VariableDeclarationExpr varDecExpr = (VariableDeclarationExpr) methodLine.asExpressionStmt().getExpression();
            for (VariableDeclarator varDec: varDecExpr.getVariables())
                if (className.equals(varDec.getType().asString()))
                    return true;
        }
        return false;
    }

    private String getMethodSignatureWithValues(String methodCallSigStr) {
        String paramTypes = methodCallSigStr.substring(
                methodCallSigStr.indexOf("(") + 1,
                methodCallSigStr.length() - 1);
        List<String> splitParamTypes = Arrays.asList(paramTypes.split(", "));
        List<String> paramVals = new ArrayList<>();

        if (splitParamTypes.isEmpty())
            return "";

        for (String paramType: splitParamTypes)
            paramVals.add(getParamValueStrFromType(paramType));

        return methodCallSigStr.substring(0, methodCallSigStr.indexOf("("))
                + "("
                + String.join(", ", paramVals)
                + ")"
                + ";";
    }

    private String getParamValueStrFromType(String typeStr) {
        if (typeStr.endsWith("[]"))
            return "new " + typeStr.substring(0, typeStr.length() - 2) + "[]{}";

        switch (typeStr) {
            case "":
                return "";
            case "boolean":
                return String.valueOf(RandomUtils.generateRandomBool());
            case "int":
            case "long":
            case "short":
                return String.valueOf(RandomUtils.generateRandomInt(10000));
            default: // We assume it's an object
                return "null";
        }
    }
}
