package com.github.octavelarose.bootleg.builders.programs.classes.methods;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.CallableMethodBodyEditor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.visitors.ConstructorCallResultInstVisitor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.visitors.MethodCallResultInstVisitor;

import java.util.HashMap;

/**
 * Writes a call to one method in another, i.e "parentClass.methodName(3, "hello", 1.2);"
 * Plenty of caveats, like needing to instantiate a parent class if none is present, the different syntax for static methods, etc.
 */
public class MethodCallInstructionWriter {
    ClassBuilder callerClass;
    ClassBuilder calleeClass;
    CallableDeclaration<?> callerMethod;
    CallableDeclaration<?> calleeMethod;

    // Needed for context when a method takes other classes as arguments, and those can't be fetched from local variables/the method context, ...
    // ...hence they need to be instantiated. Which requires their constructors, which requires access to the class instances.
    HashMap<String, ClassBuilder> classesContext;

    /**
     * @param callerClass The caller class.
     * @param callerMethodSignature The caller method signature.
     * @return The MethodCallInstructionWriter object.
     */
    public MethodCallInstructionWriter setCaller(ClassBuilder callerClass, CallableDeclaration.Signature callerMethodSignature) {
        this.callerClass = callerClass;
        this.callerMethod = callerClass.getMethodFromSignature(callerMethodSignature);
        return this;
    }

    /**
     * @param calleeClass The callee class.
     * @param calleeMethodSignature The callee method signature.
     * @return The MethodCallInstructionWriter object.
     */
    public MethodCallInstructionWriter setCallee(ClassBuilder calleeClass, CallableDeclaration.Signature calleeMethodSignature) {
        this.calleeClass = calleeClass;
        this.calleeMethod = calleeClass.getMethodFromSignature(calleeMethodSignature);
        return this;
    }

    /**
     * Sets the class context.
     * @param classBuilders The other classes' fetched from the calltrace so far.
     * @return A this instance.
     */
    public MethodCallInstructionWriter setOtherClassesContext(HashMap<String, ClassBuilder> classBuilders) {
        this.classesContext = classBuilders;
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

        boolean isCalleeMethodStatic = calleeMethod.getModifiers()
                .stream()
                .anyMatch(s -> s.getKeyword() == Modifier.Keyword.STATIC);

        if (calleeMethod instanceof ConstructorDeclaration) {
            cmbe.accept(new ConstructorCallResultInstVisitor()
                    .setCallerClass(calleeClass)
                    .setParameters(calleeMethod.getParameters())
                    .setClassesContext(classesContext));
        } else {
            cmbe.accept(new MethodCallResultInstVisitor()
                    .setCalleeMethod((MethodDeclaration)calleeMethod)
                    .setMethodClass(calleeClass)
                    .setIsLocalMethodCall(callerClass.getName().equals(calleeClass.getName()))
                    .setIsMethodStatic(isCalleeMethodStatic)
                    .setClassesContext(classesContext));
        }

        cmbe.setBodyToCallable();
    }
}
