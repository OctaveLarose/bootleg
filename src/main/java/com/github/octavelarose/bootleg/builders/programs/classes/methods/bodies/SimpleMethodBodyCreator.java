package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

/**
 * Creates a simple method body, with default statements inside and which returns the BlockStmt object directly.
 */
public class SimpleMethodBodyCreator extends MethodBodyEditor {
    /**
     * @return The method body object.
     */
    public BlockStmt getMethodBody() {
        return this.generateMethodBody();
    }

    /**
     * Adds one or many default statements to the methodBody. These are statements common to every method.
     * As of 12/07/21 they print the current name of the method, but in the future methods should perform more advanced operations by default.
     * @param methodFullName The full name of the method, needed to print it.
     * @return A this instance.
     */
    public SimpleMethodBodyCreator addDefaultStatements(String methodFullName) {
        // In the future, should ideally contain "advanced" operations.
        this.instrsBlock.addStatement(new NameExpr("System.out.println(\"" + "Current method: " + methodFullName + "\")"));
        return this;
    }
}
