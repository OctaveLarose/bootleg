package com.github.octavelarose.codegenerator.builders.classes.methods.bodies;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.codegenerator.builders.classes.methods.DummyValueCreator;

/**
 * Creates and manages a method body, i.e a BlockStmt object.
 */
public abstract class MethodBodyEditor {
    // We're assuming methods are divided into three parts: the instantiation of local variables, various calculations, and end return statements.
    // That's empirical but I believe this separation can be justified with some research by smarter people than me who use that as a predicate
    protected final BlockStmt varsInsnBlock = new BlockStmt();
    protected final BlockStmt regularInstrsBlock = new BlockStmt();
    protected final BlockStmt returnStmtBlock = new BlockStmt();

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
     * @param expr The variable instantiation statement/expression. TODO make statement
     * @return A this instance.
     */
    public MethodBodyEditor addVarInsnStatement(Expression expr) {
        this.varsInsnBlock.addStatement(expr);
        return this;
    }

    /**
     * Adds a regular operation statement.
     * @param expr The regular operation statement/expression. TODO make statement
     * @return A this instance.
     */
    public MethodBodyEditor addRegularStatement(Expression expr) {
        this.regularInstrsBlock.addStatement(expr);
        return this;
    }

    /**
     * Adds a return statement to the end of a method, unless the type fed is void in which case none is necessary.
     * @param returnType The return type of the method.
     * @return A this instance.
     */
    public MethodBodyEditor addReturnStatement(Type returnType) {
        if (!returnType.isVoidType())
            this.returnStmtBlock.addStatement(new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));
        return this;
    }
}
