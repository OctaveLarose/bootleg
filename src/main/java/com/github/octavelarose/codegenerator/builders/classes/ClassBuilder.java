package com.github.octavelarose.codegenerator.builders.classes;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.codegenerator.builders.BuildConstants;

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

    /**
     * Adds a constructor to the class. We consider constructors to be separate from methods.
     * @param parameters The constructor's parameters
     * @param methodBody The constructor's body, i.e content, i.e code.
     * @param modifiers  The constructor's modifiers (public, protected, static...)
     * @return The constructor object.
     */
    public ConstructorDeclaration addConstructor(NodeList<Parameter> parameters,
                                                 BlockStmt methodBody,
                                                 NodeList<Modifier> modifiers) {
        ConstructorDeclaration cs = this.outputClass.addConstructor();
        cs.setModifiers(modifiers);
        cs.setBody(methodBody);
        cs.setParameters(parameters);
        return cs;
    }

    /**
     * Adds a method to the class.
     *  @param name       The method's name.
     * @param returnType The method's return value type.
     * @param parameters The method's parameters
     * @param methodBody The method's body, i.e content, i.e code.
     * @param modifiers  The method's modifiers (public, protected, static...)
     * @return The method object.
     */
    public MethodDeclaration addMethod(String name,
                                         Type returnType,
                                         NodeList<Parameter> parameters,
                                         BlockStmt methodBody,
                                         NodeList<Modifier> modifiers) {
        MethodDeclaration method = this.outputClass.addMethod(name);
        method.setModifiers(modifiers);
        method.setBody(methodBody);
        method.setType(returnType);
        method.setParameters(parameters);
        return method;
    }

    public void setModifiers(NodeList<Modifier> modifiers) {
        this.outputClass.setModifiers(modifiers);
    }

    public List<MethodDeclaration> getMethods() {
        return this.outputClass.getMethods();
    }

    public void addField(String name,
                         Type fieldType,
                         Modifier.Keyword... modifiers) {
        this.outputClass.addField(fieldType, name, modifiers);
    }

    public void addField(String name,
                         Type fieldType,
                         Expression initializer,
                         Modifier.Keyword... modifiers) {
        this.outputClass.addFieldWithInitializer(fieldType, name, initializer, modifiers);
    }

    /**
     * Adds an import statement to the CU.
     * Since a class may rely on other classes, it may not compile if the CU doesn't take care of the right imports.
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
            return null; // Should throw instead, but it's unreachable, so whatever
    }

    /**
     * Sets the package declaration.
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

    public String getName() {return this.outputClass.getName().asString();}

    /**
     * @param methodName The name of the method to look for.
     * @return true if the method is present in the class, false otherwise.
     */
    public boolean hasMethod(String methodName) {
        // TODO: In practice, we'd need to check the method name + the parameters, to account for overloading
        // Might be better to feed the DiSL (ASM) method signature format to it as a string and have it parse it?
        if (methodName.equals(BuildConstants.CONSTRUCTOR_NAME))
            return !this.outputClass.getConstructors().isEmpty();

        for (MethodDeclaration m: this.outputClass.getMethods()) {
            if (m.getName().asString().equals(methodName))
                return true;
        }
        return false;
    }

    /**
     * @return The class' constructors.
     */
    public List<ConstructorDeclaration> getConstructors() {
        return this.outputClass.getConstructors();
    }

    // TODO description
    public CallableDeclaration<?> getMethodFromSignature(CallableDeclaration.Signature sig) {
        // TODO figure out whatever is going on there with the signature objects not matching
        // Basically matching signature objects doesn't always work even when they seem virtually identical, so I compare the string versions instead
        for (ConstructorDeclaration m: this.outputClass.getConstructors()) {
//            if (m.getSignature() == sig)
            if (m.getSignature().asString().equals(sig.asString()))
                return m;
        }
        for (MethodDeclaration m: this.outputClass.getMethods()) {
//            if (m.getSignature() == sig)
            if (m.getSignature().asString().equals(sig.asString()))
                return m;
        }
/*        System.out.println("AAAA");
//        System.out.println(this.outputClass.getMethods());
        System.out.println(this.outputClass.getMethods().get(1).getSignature());
        System.out.println(sig);
        System.out.println("Are they the same: " + (sig == this.outputClass.getMethods().get(1).getSignature()));
        System.out.println("Same class? " + (sig.getClass() == this.outputClass.getMethods().get(1).getSignature().getClass()));
        System.out.println("Same name? " + (sig.getName().equals(this.outputClass.getMethods().get(1).getSignature().getName())));
//        System.out.println(sig.getParameterTypes() + " " + this.outputClass.getMethods().get(1).getSignature().getParameterTypes());
        System.out.println("Same parameter types ? " + (sig.getParameterTypes().equals(this.outputClass.getMethods().get(1).getSignature().getParameterTypes())));
        System.out.println("AAAA");*/
        return null;
    }

    /**
     * Returns a method given its name.
     * TODO: this is NOT scalable as overloading exists, and so it should at best return a list of methods.
     * @param methodName The name of the method.
     * @return The method with the given name
     */
    public CallableDeclaration<?> getMethodFromName(String methodName) {
        if (methodName.equals("<init>")) {
            if (this.outputClass.getConstructors().isEmpty())
                return null;
            else
                return this.outputClass.getConstructors().get(0);
        }
        for (MethodDeclaration m: this.outputClass.getMethods()) {
            if (m.getName().asString().equals(methodName))
                return m;
        }
        return null;
    }
}
