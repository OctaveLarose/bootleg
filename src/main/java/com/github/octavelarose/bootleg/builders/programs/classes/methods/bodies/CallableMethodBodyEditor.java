package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;

/**
 * Method body editor tailored to callable instances (i.e methods and constructors).
 */
public class CallableMethodBodyEditor extends MethodBodyEditor {
    private final CallableDeclaration<?> method;

    /**
     * A constructor that takes in a method object.
     * @param method The method object to get the existing method body from.
     */
    public CallableMethodBodyEditor(CallableDeclaration<?> method, ClassBuilder parentClass) throws BuildFailedException {
        BlockStmt methodBody = this.getMethodBodyOfCallable(method);

        this.method = method;

        // Not very good, what if there's a return statement in the middle of the function?
        // Will do the job fine so far since we're assuming all methods only have a return statement at the end
        for (Statement stmt: methodBody.getStatements()) {
            if (stmt instanceof ReturnStmt)
                this.returnStmt = (ReturnStmt) stmt;
            else
                this.instrsBlock.addStatement(stmt);
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
}
