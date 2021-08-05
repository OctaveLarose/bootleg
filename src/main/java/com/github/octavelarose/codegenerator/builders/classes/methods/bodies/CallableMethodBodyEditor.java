package com.github.octavelarose.codegenerator.builders.classes.methods.bodies;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.octavelarose.codegenerator.builders.BuildConstants;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.methods.DummyValueCreator;
import com.github.octavelarose.codegenerator.builders.classes.methods.MethodCallInstructionWriter.IsCalleeMethodStatic;
import com.github.octavelarose.codegenerator.builders.utils.JPTypeUtils;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

import java.util.List;
import java.util.Optional;

public class CallableMethodBodyEditor extends MethodBodyEditor {
    private final CallableDeclaration<?> method;
    private final ClassBuilder parentClass;

    /**
     * A constructor that takes in a method object.
     * @param method The method object to get the existing method body from.
     */
    public CallableMethodBodyEditor(CallableDeclaration<?> method, ClassBuilder parentClass) throws BuildFailedException {
        BlockStmt methodBody = this.getMethodBodyOfCallable(method);

        this.method = method;
        this.parentClass = parentClass;

        // Not very good, what if there's a return statement in the middle of the function?
        // Will do the job fine so far since we're assuming all methods will have this distinct var insn/calculations/return statement structure
        for (Statement stmt: methodBody.getStatements()) {
            if (stmt instanceof ExpressionStmt && ((ExpressionStmt)stmt).getExpression().isVariableDeclarationExpr())
                this.varsInsnBlock.addStatement(stmt);
            else if (stmt instanceof ReturnStmt)
                this.returnStmtBlock.addStatement(stmt);
            else
                this.regularInstrsBlock.addStatement(stmt);
        }

        this.setMethodParameters(method.getParameters());
    }

    /**
     * @return The caller method's body, containing the method instructions.
     * @throws BuildFailedException If the method's type can't be inferred (i.e it isn't a method/constructor)
     */
    private BlockStmt getMethodBodyOfCallable(CallableDeclaration<?> method) throws BuildFailedException {
        BlockStmt methodBody;

        if (method instanceof MethodDeclaration) {
            MethodDeclaration md = ((MethodDeclaration) method);
            if (md.getBody().isEmpty()) { // Should never happen since methods are always instantiated with an empty block
                methodBody = new BlockStmt();
                md.setBody(methodBody);
            } else {
                methodBody = md.getBody().get();
            }
        } else if (method instanceof ConstructorDeclaration)
            methodBody = ((ConstructorDeclaration) method).getBody();
        else
            throw new BuildFailedException("Couldn't find method body, as this is neither a classic method nor a constructor");

        return methodBody;
    }

    /**
     * Is there a local variable in the method that corresponds to an instantiation of a given class?
     * Used for a
     * @param className The name of the class.
     * @return true if a variable was instantiated with the type className, false otherwise.
     */
    public boolean isClassInstantiationInMethodBody(String className) {
        for (Statement methodLine: this.generateMethodBody().getStatements()) {
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

    /**
     * Sets the fabricated body to the wrapped callable.
     */
    public void setBodyToCallable() throws BuildFailedException {
        if (this.method instanceof ConstructorDeclaration) {
            ((ConstructorDeclaration) this.method).setBody(this.generateMethodBody());
        }
        else if (this.method instanceof MethodDeclaration)
            ((MethodDeclaration) this.method).setBody(this.generateMethodBody());
        else
            throw new BuildFailedException("Couldn't set method body, as this is neither a classic method nor a constructor");
    }

    /**
     * Generates a new statement from a method call, a var. instantiation statement or a regular statement if void is returned
     * @param method A method instance
     * @param calleeClass The class the method belongs to
     * @param isCalleeMethodStatic Whether or not the method is static
     */
    public void addMethodCallToLocalVar(MethodDeclaration method,
                                        ClassBuilder calleeClass,
                                        IsCalleeMethodStatic isCalleeMethodStatic) throws BuildFailedException {
        String calleeClassName = calleeClass.getName();
        NodeList<Expression> dummyParamVals = DummyValueCreator.getDummyParameterValuesAsExprs(method.getParameters());
        MethodCallExpr methodCallExpr = new MethodCallExpr()
                .setName(method.getName())
                .setArguments(dummyParamVals);

        if (isCalleeMethodStatic == IsCalleeMethodStatic.YES) {
            methodCallExpr.setScope(new NameExpr(calleeClassName));
        } else {
            if (calleeClassName.equals(this.parentClass.getName()))
                methodCallExpr.setScope(new ThisExpr());
            else {
                Optional<VariableDeclarator> localVarOfType = this.getLocalVarOrParamOfTypeObjFromStr(calleeClass.getImportStr());

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

        if (method.getType().isVoidType()) {
            this.addRegularStatement(new ExpressionStmt(methodCallExpr));
            return;
        }

        this.addVarInsnStatement(new ExpressionStmt(new VariableDeclarationExpr(
                new VariableDeclarator(method.getType(),
                        RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                        methodCallExpr))
        ));
    }

    /**
     * Creates a new variable of a given class type, being given a class.
     * @param inputClass The class which needs a new instance
     * @return The variable in question.
     * @throws BuildFailedException If the class can't be instantiated because it has no constructors,
     */
    private VariableDeclarator createNewVarOfTypeObj(ClassBuilder inputClass) throws BuildFailedException {
        List<ConstructorDeclaration> constructors = inputClass.getConstructors();

        if (constructors.size() == 0)
            throw new BuildFailedException("Can't instantiate a new instance of class "
                    + inputClass.getName()
                    + " in our safeguard code, as it has no constructors");

        var dummyParamVals = DummyValueCreator.getDummyParameterValuesAsExprs(constructors.get(0).getParameters());

        try {
            ClassOrInterfaceType classType = JPTypeUtils.getClassTypeFromName(inputClass.getName().replace("/", "."));
            var varDeclarator = new VariableDeclarator(classType,
                    RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                    new ObjectCreationExpr().setType(classType).setArguments(dummyParamVals));

            parentClass.addImport(inputClass.getImportStr());

            // Added to the start to make sure it's instantiated before the operations that need it, since those operations may be in variable instantiations themselves
            this.addVarInsnStatementToStart(new ExpressionStmt(new VariableDeclarationExpr(varDeclarator)));

            return varDeclarator;
        } catch (ParseException e) {
            throw new BuildFailedException("ParseException: " + e.getMessage());
        }
    }
}
