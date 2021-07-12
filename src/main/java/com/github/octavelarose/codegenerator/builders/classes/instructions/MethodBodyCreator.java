package com.github.octavelarose.codegenerator.builders.classes.instructions;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;

/**
 * Creates and manages a method body, i.e a BlockStmt object.
 */
public class MethodBodyCreator {
    BlockStmt methodBody;

    /**
     * Default constructor, creates a BlockStmt instance.
     */
    public MethodBodyCreator() {
        this.methodBody = new BlockStmt();
    }

    /**
     * A constructor that takes in an already existing method body.
     * @param methodBody The existing method body.
     */
    public MethodBodyCreator(BlockStmt methodBody) {
        this.methodBody = methodBody;
    }

    /**
     * @return The method body.
     */
    public BlockStmt getMethodBody() {
        return this.methodBody;
    }

    /**
     * Adds one or many default statements to the methodBody. These are statements common to every method.
     * As of 12/07/21 they print the current name of the method, but in the future methods should perform more advanced operations by default.
     * @param methodFullName The full name of the method, needed to print it.
     * @return A this instance.
     */
    public MethodBodyCreator addDefaultStatements(String methodFullName) {
        // In the future, should ideally contain "advanced" operations. TODO a sleep() operation for starters
        methodBody.addStatement(new NameExpr("System.out.println(\"" + "Current method: " + methodFullName + "\")"));
        return this;
    }

    /**
     * Adds a return statement to the end of a method, unless the type fed is void in which case none is necessary.
     * @param returnType The return type of the method.
     * @return A this instance.
     */
    public MethodBodyCreator addReturnStatement(Type returnType) {
        if (!returnType.isVoidType())
            methodBody.addStatement(methodBody.getStatements().size(),
                    new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));
        return this;
    }
}
