package com.github.octavelarose.codegenerator.builders.classes;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;

import java.util.List;


/**
 * Builds a class: returns a JavaParser CompilationUnit that contains the class itself.
 */
public abstract class ClassBuilder {
    protected final CompilationUnit cu;
    protected final ClassOrInterfaceDeclaration outputClass;

    public ClassBuilder(String name) {
        this.cu = new CompilationUnit();
        this.outputClass = cu.addClass(name);
    }

    public void addConstructor(NodeList<Modifier> modifiers) {
        // Needs to take in parameters in the future.
        ConstructorDeclaration cs = this.outputClass.addConstructor();
        cs.setModifiers(modifiers);
    }

    public void setModifiers(NodeList<Modifier> modifiers) {
        this.outputClass.setModifiers(modifiers);
    }

    /**
     * Adds a method to the class.
     *
     * @param name       The method's name.
     * @param returnType The method's return value type.
     * @param parameters The method's parameters
     * @param methodBody The method's body, i.e content, i.e code.
     * @param modifiers  The method's modifiers (public, protected, static...)
     */
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

    public List<MethodDeclaration> getMethods() {
        return this.outputClass.getMethods();
    }

    public void addField(String name,
                         Type fieldType,
                         Modifier.Keyword... modifiers) {
        this.outputClass.addField(fieldType, name, modifiers);
    }

    /**
     * Adds an import statement to the CU.
     * Since a class may rely on other classes, it may not compile if the CU doesn't take care of the right imports.
     *
     * @param importStr The value of the import statement, minus the "import " and end semicolon.
     */
    public void addImport(String importStr) {
        this.cu.addImport(importStr);
    }


    /**
     * @return The class' package declaration.
     */
    public String getImportStr() {
        if (this.cu.getPackageDeclaration().isPresent())
            return this.cu.getPackageDeclaration().get().getNameAsString() + "." + this.outputClass.getName();
        else
            return null; // Should throw instead, but... it's unreachable, so
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
