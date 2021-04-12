package com.github.octavelarose.codegenerator.builders;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;

import static com.github.javaparser.ast.Modifier.Keyword.*;

import java.util.Random;


public class ClassBuilder {
    ClassOrInterfaceDeclaration outputClass;
    CompilationUnit cu;

    public ClassBuilder(String name, int methodsNbr, CompilationUnit cu) {
        this.cu = cu;
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

    public void addBasicLinkedMethod(String name, ClassOrInterfaceDeclaration classToLink) {
        MethodDeclaration method = this.outputClass.addMethod(name, PUBLIC);

        method.setType(void.class);

        Parameter parameter = new Parameter()
                .setType(String.valueOf(classToLink.getName()))
                .setName("inputClass");
        NodeList<Parameter> parameters = new NodeList<>(parameter);
        method.setParameters(parameters);

        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement("inputClass." + classToLink.getMethods().get(2).getName() + "();");
        method.setBody(methodBody);

        // TODO get package declaration from class compilation unit instead
        cu.addImport("com." + classToLink.getName().asString());
    }

    public void setPackageDeclaration(String pkgDeclaration) {
        this.cu.setPackageDeclaration(pkgDeclaration);
    }
}
