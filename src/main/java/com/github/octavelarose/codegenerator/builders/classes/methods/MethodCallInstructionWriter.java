package com.github.octavelarose.codegenerator.builders.classes.methods;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.methods.bodies.CallableMethodBodyEditor;

/**
 * Writes a call to one method in another, i.e "methodName(3, "hello", 1.2);"
 */
public class MethodCallInstructionWriter {
    ClassBuilder callerClass;
    ClassBuilder calleeClass;
    CallableDeclaration<?> callerMethod;
    CallableDeclaration<?> calleeMethod;

    public enum IsCalleeMethodStatic {
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
     * @throws BuildFailedException If one of the input values (calle(r/e) classes/methods) are null.
     */
    private void checkCallerAndCalleeValues() throws BuildFailedException {
        if (this.callerClass == null || this.calleeClass == null)
            throw new BuildFailedException("Invalid caller or callee class.");
        if (this.callerMethod == null || this.calleeMethod == null)
            throw new BuildFailedException("Invalid caller or callee method.");
    }

    /**
     * Modifies the caller's method to add a call to a callee method, and all that entails for it to run.
     * (all that entails being import statements, callee class instantiations, for instance).
     * @throws BuildFailedException Accessing a method/class failed, or modifying its contents failed.
     */
    public void writeMethodCallInCaller() throws BuildFailedException {
        checkCallerAndCalleeValues();

        CallableMethodBodyEditor cmbe = new CallableMethodBodyEditor(callerMethod, callerClass);

        IsCalleeMethodStatic isCalleeMethodStatic = calleeMethod.getModifiers()
                .stream()
                .anyMatch(s -> s.getKeyword() == Modifier.Keyword.STATIC)
                ? IsCalleeMethodStatic.YES : IsCalleeMethodStatic.NO;

        if (calleeMethod instanceof ConstructorDeclaration) {
            cmbe.addConstructorCallToLocalVar(calleeClass, calleeMethod.getParameters());
        } else {
            cmbe.addMethodCallToLocalVar(
                    (MethodDeclaration)calleeMethod,
                    calleeClass,
                    isCalleeMethodStatic
            );
        }

        cmbe.setBodyToCallable();
    }
}
