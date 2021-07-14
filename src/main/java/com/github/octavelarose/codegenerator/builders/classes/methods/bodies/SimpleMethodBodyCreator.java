package com.github.octavelarose.codegenerator.builders.classes.methods.bodies;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;

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

    // TODO try implementing it using generics. ("public class SimpleMethodBodyCreator extends MethodBodyEditor<SimpleMethodBodyCreator>")
    // Right now, code duplication because can't chain methods that return this because of inheritance, so this is a hack
    public SimpleMethodBodyCreator addReturnStatement(Type returnType) {
        super.addReturnStatement(returnType);
        return this;
    }

    /**
     * Adds one or many default statements to the methodBody. These are statements common to every method.
     * As of 12/07/21 they print the current name of the method, but in the future methods should perform more advanced operations by default.
     * @param methodFullName The full name of the method, needed to print it.
     * @return A this instance.
     */
    public SimpleMethodBodyCreator addDefaultStatements(String methodFullName) {
        // In the future, should ideally contain "advanced" operations. TODO a sleep() operation for starters
        this.regularInstrsBlock.addStatement(new NameExpr("System.out.println(\"" + "Current method: " + methodFullName + "\")"));
        return this;
    }
}
