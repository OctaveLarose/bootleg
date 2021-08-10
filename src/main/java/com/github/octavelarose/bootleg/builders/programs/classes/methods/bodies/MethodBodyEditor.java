package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.calltraces.asm_types.ASMBytecodeParsingUtils;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.values.DummyValueCreator;
import com.github.octavelarose.bootleg.builders.programs.utils.JPTypeUtils;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Creates and manages a method body, i.e a BlockStmt object.
 */
public abstract class MethodBodyEditor {
    protected final BlockStmt instrsBlock = new BlockStmt();
    protected ReturnStmt returnStmt;

    protected NodeList<Parameter> methodParameters;

    /**
     * Default constructor, creates a BlockStmt instance.
     */
    public MethodBodyEditor() {}

    /**
     * Generates the method body. Not meant to be called by itself, and rather wrapped in another method by a subclass.
     * @return The method body.
     */
    protected BlockStmt generateMethodBody() {
        NodeList<Statement> concatStmts = new NodeList<>();

        for (Statement stmt: this.instrsBlock.getStatements())
            concatStmts.add(stmt);
        if (this.returnStmt != null)
            concatStmts.add(this.returnStmt);

        return new BlockStmt(concatStmts);
    }

    /**
     * Adds a statement to the method body.
     * @param exprStmt The statement as an ExpressionStmt.
     */
    public void addStatement(ExpressionStmt exprStmt) {
        this.instrsBlock.addStatement(exprStmt);
    }

    /**
     * Adds a statement to the start of the method body.
     * @param exprStmt The statement as an ExpressionStmt.
     */
    public void addStatementToStart(ExpressionStmt exprStmt) {
        this.instrsBlock.addStatement(0, exprStmt);
    }

    /**
     * Adds a return statement.
     * @param returnStmt A return statement object.
     */
    public void setReturnStatement(ReturnStmt returnStmt) {
        this.returnStmt = returnStmt;
    }

    /**
     * Adds a random return statement of a given type, unless the type fed is void in which case none is necessary.
     * @param returnType The return type of the method.
     */
    public void setRandomReturnStatement(Type returnType) {
        if (!returnType.isVoidType())
            this.setReturnStatement(new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));
    }

    /**
     * Adds a return statement of a given type, making sure a local variable is returned.
     * If none are eligible, then a random value is returned instead.
     * @param returnType The return type of the method.
     */
    public boolean setReturnStatementFromLocalVar(Type returnType) {
        Optional<VariableDeclarator> localVar = this.getLocalVarOrParamOfType(returnType);

        if (localVar.isPresent())
            this.setReturnStatement(new ReturnStmt(new NameExpr(localVar.get().getName())));
        else if (!returnType.isClassOrInterfaceType())
            this.setRandomReturnStatement(returnType);
        else
            return false;

        return true;
    }

    /**
     * Adds a return statement as a new class instantiation.
     * @param classCb The class
     */
    public void setReturnStatementAsNewClass(ClassBuilder classCb) {
        if (classCb.getConstructors().size() == 0)
            this.returnStmt = new ReturnStmt(new NullLiteralExpr());
        else
            this.returnStmt = new ReturnStmt(new ObjectCreationExpr()
                    .setType(classCb.getImportStr())
                    .setArguments(DummyValueCreator.getDummyParameterValuesAsExprs(classCb.getConstructors().get(0).getParameters())));
    }


    /**
     * @return true if the method has a return statement, false otherwise.
     */
    public boolean hasReturnStatement() {
        return this.returnStmt != null;
    }

    /**
     * Sets the method's parameters. Need to be used by operations, ideally
     * @param methodParameters The method parameters
     */
    public void setMethodParameters(NodeList<Parameter> methodParameters) {
        this.methodParameters = methodParameters;
    }

    /**
     * Processes a list of arithmetic operations (ADD, SUB, etc...) and adds them to the method body.
     * @param methodOps The operations list (ex: ["IADD", "DSUB", "DDIV", ...])
     */
    public void processOperationStatements(List<String> methodOps) throws BuildFailedException {
        for (String opStr: methodOps) {
            Type opType = ASMBytecodeParsingUtils.getTypeFromBytecodePrefix(opStr.charAt(0));
            AssignExpr.Operator operator = ASMBytecodeParsingUtils.getAssignOperatorFromBytecodeStr(opStr.substring(1));

            Optional<VariableDeclarator> localVarName = this.getLocalVarOrParamOfType(opType);

            if (localVarName.isEmpty()) {
                this.addStatement(new ExpressionStmt(
                        new VariableDeclarationExpr(
                            new VariableDeclarator(opType, RandomUtils.generateRandomName(5),
                                new NameExpr(DummyValueCreator.getDummyParamValueFromType(opType))))
                ));
            } else {
                this.addStatement(new ExpressionStmt(
                        new AssignExpr(
                            new NameExpr(localVarName.get().getName()),
                            new NameExpr(DummyValueCreator.getDummyParamValueFromType(opType)),
                            operator)
                ));
            }
        }
    }

    /**
     * @param wantedType The type of the variable being queried
     * @return The name of a local variable / parameter of that given type
     */
    protected Optional<VariableDeclarator> getLocalVarOrParamOfType(Type wantedType) {
        List<VariableDeclarator> candidateVars = new ArrayList<>();

        if (this.methodParameters != null && this.methodParameters.isNonEmpty()) {
            for (Parameter param : this.methodParameters) {
                if (param.getType().equals(wantedType))
                    candidateVars.add(new VariableDeclarator(param.getType(), param.getName()));
            }
        }

        for (Statement stmt: this.instrsBlock.getStatements()) {
            Expression stmtExpr = stmt.asExpressionStmt().getExpression();
            if (stmtExpr.isVariableDeclarationExpr()) {
                VariableDeclarationExpr expr = stmt.asExpressionStmt().getExpression().asVariableDeclarationExpr();
                if (expr.getVariable(0).getType().equals(wantedType))
                    candidateVars.add(expr.getVariable(0));
            }
        }

        if (candidateVars.isEmpty())
            return Optional.empty();
        else
            return Optional.of(candidateVars.get(new Random().nextInt(candidateVars.size())));
    }

    /**
     * @param objName The name of the object, as a string.
     * @return The name of a local variable / parameter of that given type
     */
    protected Optional<VariableDeclarator> getLocalVarOrParamOfTypeObjFromStr(String objName) {
        try {
            return this.getLocalVarOrParamOfType(JPTypeUtils.getClassTypeFromName(objName));
        } catch (ParseException e) {
            System.err.println(e.getMessage()); // Ugly, but should never happen (famous last words)
            return Optional.empty();
        }
    }
}
