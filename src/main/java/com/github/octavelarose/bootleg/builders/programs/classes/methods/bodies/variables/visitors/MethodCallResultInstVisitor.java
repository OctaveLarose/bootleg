package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.visitors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.octavelarose.bootleg.builders.BuildConstants;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.MethodBodyEditor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.LocalVariableFetcher;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.HashMap;
import java.util.Optional;

/**
 * Puts the result of a method call into a local variable.
 */
public class MethodCallResultInstVisitor extends VarInstantiatorVisitor {
    // The method called.
    private MethodDeclaration calleeMethod;

    // The class the method belongs to.
    private ClassBuilder calleeClass;

    // Whether or not the method is static.
    private boolean isCalleeMethodStatic;

    // Whether or not the method call is to a class from the current context (i.e whether it must be called from "this")
    private boolean isLocalMethodCall;

    // The other classes in our system.
    private HashMap<String, ClassBuilder> classesContext;

    public MethodCallResultInstVisitor setCalleeMethod(MethodDeclaration calleeMethod) {
        this.calleeMethod = calleeMethod;
        return this;
    }

    public MethodCallResultInstVisitor setMethodClass(ClassBuilder calleeClass) {
        this.calleeClass = calleeClass;
        return this;
    }

    public MethodCallResultInstVisitor setIsLocalMethodCall(boolean isLocalMethodCall) {
        this.isLocalMethodCall = isLocalMethodCall;
        return this;
    }

    public MethodCallResultInstVisitor setIsMethodStatic(boolean isMethodStatic) {
        this.isCalleeMethodStatic = isMethodStatic;
        return this;
    }

    public MethodCallResultInstVisitor setClassesContext(HashMap<String, ClassBuilder> classesContext) {
        this.classesContext = classesContext;
        return this;
    }

    @Override
    public void visit(MethodBodyEditor methodBodyEditor, LocalVariableFetcher localVariableFetcher) throws BuildFailedException {
        super.visit(methodBodyEditor, localVariableFetcher);
        this.addMethodCallToLocalVar();
    }

    /**
     * Generates a new statement from a method call, a var. instantiation statement or a regular statement if void is returned
     */
    private void addMethodCallToLocalVar() throws BuildFailedException {
        String calleeClassName = calleeClass.getName();
        NodeList<Expression> dummyParamVals = this.getParamValuesFromContext(calleeMethod.getParameters(), classesContext);
        MethodCallExpr methodCallExpr = new MethodCallExpr()
                .setName(calleeMethod.getName())
                .setArguments(dummyParamVals);

        if (isCalleeMethodStatic) {
            methodCallExpr.setScope(new NameExpr(calleeClassName));
        } else {
            if (isLocalMethodCall)
                methodCallExpr.setScope(new ThisExpr());
            else {
                Optional<VariableDeclarator> localVarOfType = this.localVariableFetcher.getLocalVarOrParamOfTypeObjFromStr(calleeClass.getImportStr());

                if (localVarOfType.isPresent())
                    methodCallExpr.setScope(new NameExpr(localVarOfType.get().getName()));
                else {
                    // We instantiate a new class of the given type if none is present to access the method from.
                    // This safeguard shouldn't exist, since there should always be an option to find an instance of one in the input real program (else it wouldn't run).
                    // This is needed (as of 05/08/21) since for instance, a class instance could only be present in a field, and those aren't implemented yet.
                    VariableDeclarator newVar = this.createNewVarOfTypeObj(calleeClass);
                    methodCallExpr.setScope(new NameExpr(newVar.getName()));
                }
            }
        }

        if (calleeMethod.getType().isVoidType())
            methodBodyEditor.addStatement(new ExpressionStmt(methodCallExpr));
        else
            methodBodyEditor.addStatement(new ExpressionStmt(new VariableDeclarationExpr(
                    new VariableDeclarator(calleeMethod.getType(),
                            RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                            methodCallExpr))
            ));
    }
}

