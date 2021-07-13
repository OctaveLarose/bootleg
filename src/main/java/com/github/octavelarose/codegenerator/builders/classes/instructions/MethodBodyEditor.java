package com.github.octavelarose.codegenerator.builders.classes.instructions;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;

/**
 * Creates and manages a method body, i.e a BlockStmt object.
 */
public class MethodBodyEditor {
    // We're assuming methods are divided into three parts: the instantiation of local variables, various calculations, and end return statements.
    // That's empirical but I believe this separation can be justified with some research by smarter people than me who use that as a predicate
    private final BlockStmt varsInsnBlock = new BlockStmt();
    private final BlockStmt regularInstrsBlock = new BlockStmt();
    private final BlockStmt returnStmtBlock = new BlockStmt();

    /**
     * Default constructor, creates a BlockStmt instance.
     */
    public MethodBodyEditor() {}

    /**
     * A constructor that takes in a method object.
     * @param method The method object to get the existing method body from.
     */
    public MethodBodyEditor(CallableDeclaration<?> method) throws BuildFailedException {
        BlockStmt methodBody = this.getMethodBodyOfCallable(method);

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
     * @return The method body.
     */
    public BlockStmt getMethodBody() {
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
        System.out.println(expr);
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

    /**
     * Adds one or many default statements to the methodBody. These are statements common to every method.
     * As of 12/07/21 they print the current name of the method, but in the future methods should perform more advanced operations by default.
     * @param methodFullName The full name of the method, needed to print it.
     * @return A this instance.
     */
    public MethodBodyEditor addDefaultStatements(String methodFullName) {
        // In the future, should ideally contain "advanced" operations. TODO a sleep() operation for starters
        this.regularInstrsBlock.addStatement(new NameExpr("System.out.println(\"" + "Current method: " + methodFullName + "\")"));
        return this;
    }

    /**
     * TODO make subclass CallableMethodBodyCreator I think. And do this javadoc
     * @param callerMethod The callable method or constructor instance.
     */
    public void setBodyToCallable(CallableDeclaration<?> callerMethod) {
        if (callerMethod instanceof ConstructorDeclaration) {
            ((ConstructorDeclaration) callerMethod).setBody(this.getMethodBody());
        }
        else if (callerMethod instanceof MethodDeclaration)
            ((MethodDeclaration) callerMethod).setBody(this.getMethodBody());
    }
}