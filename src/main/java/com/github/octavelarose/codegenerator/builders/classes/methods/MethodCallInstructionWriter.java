package com.github.octavelarose.codegenerator.builders.classes.methods;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.methods.bodies.CallableMethodBodyEditor;
import com.github.octavelarose.codegenerator.builders.utils.JPTypeUtils;

import java.util.List;

/**
 * Writes a call to one method in another, i.e "methodName(3, "hello", 1.2);"
 */
public class MethodCallInstructionWriter {
    ClassBuilder callerClass;
    ClassBuilder calleeClass;
    CallableDeclaration<?> callerMethod;
    CallableDeclaration<?> calleeMethod;

    private enum IsCalleeMethodStatic {
        YES,
        NO
    }

    /**
     * @param callerClass The caller class.
     * @param callerMethodSignature The caller method signature.
     * @return The MethodCallInstructionWriter object.
     */
    public MethodCallInstructionWriter setCaller(ClassBuilder callerClass, CallableDeclaration.Signature callerMethodSignature) throws BuildFailedException {
        this.callerClass = callerClass;

        if (this.callerClass == null)
            throw new BuildFailedException("Caller class is null.");

        this.callerMethod = callerClass.getMethodFromSignature(callerMethodSignature);

        if (this.callerMethod == null)
            throw new BuildFailedException("Couldn't fetch caller method from caller class.");

        return this;
    }

    /**
     * @param calleeClass The callee class.
     * @param calleeMethodSignature The callee method signature.
     * @return The MethodCallInstructionWriter object.
     */
    public MethodCallInstructionWriter setCallee(ClassBuilder calleeClass, CallableDeclaration.Signature calleeMethodSignature) throws BuildFailedException {
        this.calleeClass = calleeClass;

        if (this.calleeClass == null)
            throw new BuildFailedException("Callee class is null.");

        this.calleeMethod = calleeClass.getMethodFromSignature(calleeMethodSignature);

        if (this.calleeMethod == null)
            throw new BuildFailedException("Couldn't fetch callee method from callee class.");

        return this;
    }

    /**
     * Modifies the caller's method to add a call to a callee method, and all that entails for it to run.
     * (all that entails being import statements, callee class instantiations, for instance).
     * @throws BuildFailedException Accessing a method/class failed, or modifying its contents failed.
     */
    public void writeMethodCallInCaller() throws BuildFailedException {
        checkCallerAndCalleeValues();

        CallableMethodBodyEditor mbc = new CallableMethodBodyEditor(callerMethod);
        NodeList<Expression> dummyParamVals = DummyValueCreator.getDummyParameterValuesAsExprs(calleeMethod.getParameters());

        IsCalleeMethodStatic isCalleeMethodStatic = calleeMethod.getModifiers()
                .stream()
                .anyMatch(s -> s.getKeyword() == Modifier.Keyword.STATIC)
                ? IsCalleeMethodStatic.YES : IsCalleeMethodStatic.NO;

        if (calleeMethod instanceof ConstructorDeclaration) {
            this.addCalleeClassConstructorCall(mbc, dummyParamVals);
        } else {
            if (callerClass == calleeClass)
                this.addLocalMethodCall(mbc, dummyParamVals, isCalleeMethodStatic);
            else {
                this.addForeignMethodCall(mbc, dummyParamVals, isCalleeMethodStatic);
                this.doSafeguardInstantiation(mbc, isCalleeMethodStatic);
            }
        }

        mbc.setBodyToCallable();
    }

    /**
     * Debatable safeguard. If an object is returned from a method call, or if it's in a field, it can't be detected
     * So for now I'll just create a new instance of it in every method that needs it. TODO improve, but how?
     * @param cmbc The body of the method that needs a class to checked for class instantiations and possibly appended with one
     * @param isCalleeMethodStatic Whether or not the method called is static. If it is, no instantiation is necessary.
     * @throws BuildFailedException If the class we're trying to instantiate has no constructors, we can't do anything.
     */
    private void doSafeguardInstantiation(CallableMethodBodyEditor cmbc,
                                          IsCalleeMethodStatic isCalleeMethodStatic) throws BuildFailedException {
        if (!cmbc.isClassInstantiationInMethodBody(calleeClass.getName())
                && isCalleeMethodStatic == IsCalleeMethodStatic.NO) {
            List<ConstructorDeclaration> constructors = calleeClass.getConstructors();

            if (constructors.size() == 0)
                throw new BuildFailedException("Can't instantiate a new instance of class "
                        + calleeClass.getName()
                        + " in our safeguard code, as it has no constructors");

            this.addCalleeClassConstructorCall(
                    cmbc,
                    DummyValueCreator.getDummyParameterValuesAsExprs(constructors.get(0).getParameters())
            );
        }
    }


    /**
     * @throws BuildFailedException If one of the input values (calle(r/e) classes/methods) are null.
     */
    private void checkCallerAndCalleeValues() throws BuildFailedException {
        if (this.callerClass == null || this.calleeClass == null)
            throw new BuildFailedException("Invalid caller or callee class.");
        if (this.callerMethod == null || this.calleeMethod == null)
            throw new BuildFailedException("Invalid caller or callee method.");
    }

    /**
     * Adds a call to a method from another class to the given method body.
     * @param cmbc The body of the method to be appended.
     * @param dummyParamVals Dummy values for the method parameters.
     */
    private void addForeignMethodCall(CallableMethodBodyEditor cmbc,
                                      NodeList<Expression> dummyParamVals,
                                      IsCalleeMethodStatic isCalleeMethodStatic) {
        Expression callerExpr = (isCalleeMethodStatic == IsCalleeMethodStatic.NO)
                ? new NameExpr(calleeClass.getName().toLowerCase()) : new NameExpr(calleeClass.getName());

        cmbc.addRegularStatement(new ExpressionStmt(
                new MethodCallExpr(callerExpr, calleeMethod.getName(), dummyParamVals))
        );
    }

    /**
     * Add a call to a method in the same class, so with a "this.(...)" statement.
     * @param cmbc The body of the method to be appended.
     * @param dummyParamVals Dummy values for the method parameters.
     */
    private void addLocalMethodCall(CallableMethodBodyEditor cmbc,
                                    NodeList<Expression> dummyParamVals,
                                    IsCalleeMethodStatic isCalleeMethodStatic) {
        Expression callerExpr = (isCalleeMethodStatic == IsCalleeMethodStatic.NO)
                ? new ThisExpr() : new NameExpr(calleeClass.getName());

        cmbc.addRegularStatement(new ExpressionStmt(
                new MethodCallExpr(callerExpr, calleeMethod.getName(), dummyParamVals))
        );
    }

    /**
     * Adds a call to a constructor, i.e instantiates the callee class and puts it in a new local variable.
     * @param cmbc The body of the method to be appended.
     * @param dummyParamVals Dummy values for the method parameters.
     * @throws BuildFailedException If the class with the given name couldn't be accessed.
     */
    private void addCalleeClassConstructorCall(CallableMethodBodyEditor cmbc,
                                               NodeList<Expression> dummyParamVals) throws BuildFailedException {
        ClassOrInterfaceType classWithName;

        try {
            classWithName = JPTypeUtils.getClassTypeFromName(calleeClass.getName().replace("/", "."));
        } catch (ParseException e) {
            throw new BuildFailedException(e.getMessage());
        }

        callerClass.addImport(calleeClass.getImportStr());
        cmbc.addVarInsnStatement(new ExpressionStmt(new VariableDeclarationExpr(
                        new VariableDeclarator(classWithName, calleeClass.getName().toLowerCase(),
                                new ObjectCreationExpr().setType(classWithName).setArguments(dummyParamVals))
                ))
        );
    }
}
