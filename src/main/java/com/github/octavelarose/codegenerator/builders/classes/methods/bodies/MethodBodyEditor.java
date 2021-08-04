package com.github.octavelarose.codegenerator.builders.classes.methods.bodies;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.methods.DummyValueCreator;
import com.github.octavelarose.codegenerator.builders.programs.asm_types.ASMBytecodeParsingUtils;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

import java.util.List;
import java.util.Optional;

/**
 * Creates and manages a method body, i.e a BlockStmt object.
 */
public abstract class MethodBodyEditor {
    // We're assuming methods are divided into three parts: the instantiation of local variables, various calculations, and end return statements.
    protected final BlockStmt varsInsnBlock = new BlockStmt();
    protected final BlockStmt regularInstrsBlock = new BlockStmt();
    protected final BlockStmt returnStmtBlock = new BlockStmt();

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

        for (Statement stmt: this.varsInsnBlock.getStatements())
            concatStmts.add(stmt);
        for (Statement stmt: this.regularInstrsBlock.getStatements())
            concatStmts.add(stmt);
        for (Statement stmt: this.returnStmtBlock.getStatements())
            concatStmts.add(stmt);

        return new BlockStmt(concatStmts);
    }

    /**
     * Adds a variable instantiation statement.
     * @param exprStmt The variable instantiation statement.
     */
    public void addVarInsnStatement(ExpressionStmt exprStmt) {
        this.varsInsnBlock.addStatement(0, exprStmt);
    }

    /**
     * Adds a regular operation statement.
     * @param expr The regular operation statement/expression. TODO make statement
     */
    public void addRegularStatement(Statement expr) {
        this.regularInstrsBlock.addStatement(expr);
    }

    /**
     * Adds a return statement.
     * @param returnStmt A return statement object.
     */
    public void addReturnStatement(ReturnStmt returnStmt) {
        this.returnStmtBlock.addStatement(returnStmt);
    }

    /**
     * Adds a random return statement of a given type, unless the type fed is void in which case none is necessary.
     * @param returnType The return type of the method.
     */
    public void addRandomReturnStatement(Type returnType) {
        if (!returnType.isVoidType())
            this.returnStmtBlock.addStatement(new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));
    }

    /**
     * Adds a return statement of a given type, making sure a local variable is returned.
     * If none are eligible, then a random value is returned instead.
     * @param returnType The return type of the method.
     */
    public void addReturnStatementFromLocalVar(Type returnType) {
        Optional<VariableDeclarator> localVar = this.getLocalVarOrParamOfType(returnType);

        if (localVar.isPresent())
            this.addReturnStatement(new ReturnStmt(new NameExpr(localVar.get().getName())));
        else
            this.addRandomReturnStatement(returnType);
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
                this.addVarInsnStatement(new ExpressionStmt(
                        new VariableDeclarationExpr(
                            new VariableDeclarator(opType, RandomUtils.generateRandomName(5),
                                new NameExpr(DummyValueCreator.getDummyParamValueFromType(opType))))
                ));
            } else {
                this.addRegularStatement(new ExpressionStmt(
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
        if (this.methodParameters != null && this.methodParameters.isNonEmpty()) {
            for (Parameter param : this.methodParameters) {
                if (param.getType().equals(wantedType))
                    return Optional.of(new VariableDeclarator(param.getType(), param.getName()));
            }
        }

        for (Statement stmt: this.varsInsnBlock.getStatements()) {
            VariableDeclarationExpr expr = stmt.asExpressionStmt().getExpression().asVariableDeclarationExpr();
            if (expr.getVariable(0).getType().equals(wantedType))
                return Optional.of(expr.getVariable(0));
        }

        return Optional.empty();
    }
}
