package com.github.octavelarose.codegenerator.builders;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.octavelarose.codegenerator.builders.code_visitors.ClassNameCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;


public class ClassBuilder {
    ClassOrInterfaceDeclaration outputClass;
    CompilationUnit cu;

    public ClassBuilder(String name, int methodsNbr) {
        this.cu = new CompilationUnit();
        this.outputClass = cu.addClass(name);
        this.outputClass.setPublic(true);

        // Basic, completely empty constructor
        this.outputClass.addConstructor();

        for (int i = 0; i < methodsNbr; i++)
            this.addBasicMethod(generateRandomName(i + 5));
    }

    public ClassOrInterfaceDeclaration build() {
        return this.outputClass;
    }

    private String generateRandomName(int nbrCharacters) {
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(nbrCharacters);
        for (int i = 0; i < nbrCharacters; i++) {
            int randomLimitedInt = 'a' + (int) (random.nextFloat() * ('z' - 'a' + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }

    public void addBasicMethod(String name) {
        MethodDeclaration method = this.outputClass.addMethod(name, PUBLIC);

        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement("System.out.println(\"" + generateRandomName(6) + "\");");
        method.setBody(methodBody);

        method.setType(void.class);

        Parameter parameter = new Parameter().setType(int.class).setName("args");
        NodeList<Parameter> parameters = new NodeList<>(parameter);
        method.setParameters(parameters);
    }

    public void addBasicLinkedMethod(String name, ClassBuilder classBuilderToLink) throws BuildFailedException {
        MethodDeclaration method = this.outputClass.addMethod(name, PUBLIC);
        CompilationUnit cuClassToLink = classBuilderToLink.getCompilationUnit();

        List<String> classNames = new ArrayList<>();
        VoidVisitor<List<String>> classNameVisitor = new ClassNameCollector();
        classNameVisitor.visit(cuClassToLink, classNames);
        String className = classNames.get(0);

        Optional<ClassOrInterfaceDeclaration> classToLinkOptional = cuClassToLink.getClassByName(className);
        if (classToLinkOptional.isEmpty())
            throw new BuildFailedException("Cannot find class to link in linked method input CU");
        ClassOrInterfaceDeclaration classToLink = classToLinkOptional.get();

        method.setType(void.class);

        Parameter parameter = new Parameter()
                .setType(String.valueOf(classToLink.getName()))
                .setName("inputClass");
        NodeList<Parameter> parameters = new NodeList<>(parameter);
        method.setParameters(parameters);

        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement("inputClass." + classToLink.getMethods().get(2).getName() + "(3);");
        method.setBody(methodBody);

        Optional<PackageDeclaration> pkgDeclaration = cuClassToLink.getPackageDeclaration();
        if (pkgDeclaration.isPresent()) {
            // TODO: It doesn't like it since it's the same pkg in my test case (I believe)
            String importDeclaration = pkgDeclaration.get().getNameAsString() + "." + className;
            this.cu.addImport(importDeclaration);
        } else
            throw new BuildFailedException("Attempted to create a linked method with a class with no pkg declaration.");
    }

    public void setPackageDeclaration(String pkgDeclaration) {
        this.cu.setPackageDeclaration(pkgDeclaration);
    }

    public CompilationUnit getCompilationUnit() {
        return this.cu;
    }
}
