package com.github.octavelarose.codegenerator.builders.classes.instructions;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.NodeList;
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

import java.util.Optional;

/**
 * Writes a call to one method in another, i.e "methodName(3, "hello", 1.2);"
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

    /**
     * Modifies the caller's method to add a call to a callee method, and all that entails for it to run.
     * (all that entails being import statements, callee class instantiations, for instance).
     * @throws BuildFailedException Accessing a method/class failed, or modifying its contents failed.
     */
    public void writeMethodCallInCaller() throws BuildFailedException {
        BlockStmt methodBody = this.getCallerMethodBody();
        NodeList<Expression> dummyParamVals = DummyValueCreator.getDummyParameterValuesAsExprs(calleeMethod.getParameters());

        if (callerClass == calleeClass) {
            this.addLocalMethodCall(methodBody, dummyParamVals);
        } else {
            if (calleeMethod instanceof ConstructorDeclaration)
                this.addCalleeClassConstructorCall(methodBody, dummyParamVals);
            else {
                this.addForeignMethodCall(methodBody, dummyParamVals);

                // Ugly safeguard. If an object is returned from a method call, or if it's in a field, it can't be detected
                // So for now I'll just create a new instance of it in every method that needs it. TODO improve, but how?
                if (!isClassInstantiationInMethod(methodBody, calleeClass.getName())) {
                    dummyParamVals = DummyValueCreator.getDummyParameterValuesAsExprs(calleeClass.getConstructors().get(0).getParameters());
                    this.addCalleeClassConstructorCall(methodBody, dummyParamVals);
                }
            }
        }
    }

    /**
     * @return The caller method's body, containing the method instructions.
     * @throws BuildFailedException If the method's type can't be inferred (i.e it isn't a method/constructor)
     */
    private BlockStmt getCallerMethodBody() throws BuildFailedException {
        BlockStmt methodBody;

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
            throw new BuildFailedException("Couldn't find method body, as this is neither a classic method nor a constructor");

        return methodBody;
    }

    /**
     * Adds a call to a method from another class to the given method body.
     * @param methodBody The body of the method to be appended.
     * @param dummyParamVals Dummy values for the method parameters.
     */
    private void addForeignMethodCall(BlockStmt methodBody, NodeList<Expression> dummyParamVals) {
        methodBody.addStatement(Math.max(0, methodBody.getStatements().size() - 1),
                new MethodCallExpr(
                        new NameExpr(calleeClass.getName().toLowerCase()),
                        calleeMethod.getName(),
                        dummyParamVals)
        );
    }

    /**
     * Add a call to a method in the same class, so with a "this.(...)" statement.
     * @param methodBody The body of the method to be appended.
     * @param dummyParamVals Dummy values for the method parameters.
     */
    private void addLocalMethodCall(BlockStmt methodBody, NodeList<Expression> dummyParamVals) {
        methodBody.addStatement(Math.max(0, methodBody.getStatements().size() - 1),
                new MethodCallExpr(
                        new ThisExpr(),
                        calleeMethod.getName(),
                        dummyParamVals)
        );
    }

    /**
     * Adds a call to a constructor, i.e instantiates the callee class and puts it in a new local variable.
     * @param methodBody The body of the method to be appended.
     * @param dummyParamVals Dummy values for the method parameters.
     * @throws BuildFailedException If the class with the given name couldn't be accessed.
     */
    private void addCalleeClassConstructorCall(BlockStmt methodBody, NodeList<Expression> dummyParamVals) throws BuildFailedException {
        // TODO make this a method call to a class since it's duplicated a few times here and there in my codebase
        Optional<ClassOrInterfaceType> classWithName = new JavaParser()
                .parseClassOrInterfaceType(calleeClass.getName())
                .getResult();

        if (classWithName.isEmpty())
            throw new BuildFailedException("Couldn't parse class " + classWithName);

        // TODO add import statement else the generated code won't run if they're not in the same package
        methodBody.addStatement(0, new VariableDeclarationExpr(
                        new VariableDeclarator(classWithName.get(), calleeClass.getName().toLowerCase(),
                                new ObjectCreationExpr()
                                        .setType(classWithName.get()).setArguments(dummyParamVals))
                )
        );
    }

    /**
     * Is there a local variable in the method that corresponds to an instantiation of a given class?
     * @param methodBody The body of the method.
     * @param className The name of the class.
     * @return true if a variable was instantiated with the type className, false otherwise.
     */
    private boolean isClassInstantiationInMethod(BlockStmt methodBody, String className) {
        for (Statement methodLine: methodBody.getStatements()) {
            // A bit awkward. Right now we add a return stmt at the start, and those need to be ignored
            // I guess this could be changed to "if it isn't an expression statement" instead
            if (methodLine.isReturnStmt())
                continue;

            if (!(methodLine.asExpressionStmt().getExpression() instanceof VariableDeclarationExpr))
                continue;

            VariableDeclarationExpr varDecExpr = (VariableDeclarationExpr) methodLine.asExpressionStmt().getExpression();
            for (VariableDeclarator varDec: varDecExpr.getVariables())
                if (className.equals(varDec.getType().asString()))
                    return true;
        }
        return false;
    }
}
