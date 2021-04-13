package com.github.octavelarose.codegenerator.builders.classes;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;


public abstract class ClassBuilder {
    CompilationUnit cu;
    ClassOrInterfaceDeclaration outputClass;

    public ClassBuilder(String name) {
        this.cu = new CompilationUnit();
        this.outputClass = cu.addClass(name);
    }

    public void addConstructor() {
        // Needs to take in parameters in the future.
        this.outputClass.addConstructor();
    }

    public void setModifiers(NodeList<Modifier> modifiers) {
        this.outputClass.setModifiers(modifiers);
    }

    public void addMethod(String name,
                          Type returnType,
                          NodeList<Parameter> parameters,
                          BlockStmt methodBody,
                          NodeList<Modifier> modifiers) {
        MethodDeclaration method = this.outputClass.addMethod(name);
        method.setModifiers(modifiers);
        method.setBody(methodBody);
        method.setType(returnType);
        method.setParameters(parameters);
    }

    public void addField() {
        // Currently unused.
        // FieldDeclaration field = this.outputClass.addField(...);
    }

    /**
     * Sets the package declaration.
     *
     * @param pkgDeclaration The package declaration.
     */
    public void setPackageDeclaration(String pkgDeclaration) {
        this.cu.setPackageDeclaration(pkgDeclaration);
    }

    /**
     * @return The CompilationUnit object that contains all the class information.
     */
    public CompilationUnit getCompilationUnit() {
        return this.cu;
    }
}
