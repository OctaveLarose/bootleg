package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.visitors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.octavelarose.bootleg.builders.BuildConstants;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.MethodCallInstructionWriter;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.MethodBodyEditor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.LocalVariableFetcher;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.HashMap;
import java.util.Optional;

public class MethodCallResultInstVisitor extends VarInstantiatorVisitor {
    private final MethodDeclaration calleeMethod;
    private final ClassBuilder calleeClass;
    private final String callerClassName;
    private final MethodCallInstructionWriter.IsCalleeMethodStatic isCalleeMethodStatic;
    private final HashMap<String, ClassBuilder> otherClasses;

    public MethodCallResultInstVisitor(MethodDeclaration calleeMethod,
                                       ClassBuilder calleeClass,
                                       String callerClassName,
                                       MethodCallInstructionWriter.IsCalleeMethodStatic isCalleeMethodStatic,
                                       HashMap<String, ClassBuilder> otherClasses) {
        this.calleeMethod = calleeMethod;
        this.calleeClass = calleeClass;
        this.callerClassName = callerClassName;
        this.isCalleeMethodStatic = isCalleeMethodStatic;
        this.otherClasses = otherClasses;
    }

    @Override
    public void visit(MethodBodyEditor methodBodyEditor, LocalVariableFetcher localVariableFetcher) throws BuildFailedException {
        super.visit(methodBodyEditor, localVariableFetcher);
        this.addMethodCallToLocalVar(calleeMethod, calleeClass, isCalleeMethodStatic, otherClasses);
    }

    /**
     * Generates a new statement from a method call, a var. instantiation statement or a regular statement if void is returned
     * @param method A method instance
     * @param calleeClass The class the method belongs to
     * @param isCalleeMethodStatic Whether or not the method is static
     * @param otherClasses The other classes we created so far.
     */
    public void addMethodCallToLocalVar(MethodDeclaration method,
                                        ClassBuilder calleeClass,
                                        MethodCallInstructionWriter.IsCalleeMethodStatic isCalleeMethodStatic,
                                        HashMap<String, ClassBuilder> otherClasses) throws BuildFailedException {
        String calleeClassName = calleeClass.getName();
        NodeList<Expression> dummyParamVals = this.getParamValuesFromContext(method.getParameters(), otherClasses);
        MethodCallExpr methodCallExpr = new MethodCallExpr()
                .setName(method.getName())
                .setArguments(dummyParamVals);

        if (isCalleeMethodStatic == MethodCallInstructionWriter.IsCalleeMethodStatic.YES) {
            methodCallExpr.setScope(new NameExpr(calleeClassName));
        } else {
            if (calleeClassName.equals(this.callerClassName))
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

        if (method.getType().isVoidType())
            methodBodyEditor.addStatement(new ExpressionStmt(methodCallExpr));
        else
            methodBodyEditor.addStatement(new ExpressionStmt(new VariableDeclarationExpr(
                    new VariableDeclarator(method.getType(),
                            RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                            methodCallExpr))
            ));
    }
}

