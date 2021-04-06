package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;

import static com.github.javaparser.ast.Modifier.Keyword.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Random;


public class ClassBuilder {
    ClassOrInterfaceDeclaration outputClass;
    CompilationUnit cu;

    ClassBuilder(String name, int methodsNbr, CompilationUnit cu) {
        this.cu = cu;
        this.outputClass = cu.addClass(name);
        this.outputClass.setPublic(true);

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
        MethodDeclaration method = this.outputClass.addMethod(name);

        method.setModifiers(PUBLIC, STATIC);

        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement("System.out.println(\"Hello world!\");");
        method.setBody(methodBody);

        method.setType(int.class);

        NodeList<Parameter> parameters = new NodeList<>();
        // TODO declare type in constructor, there's got to be a way.
        Parameter parameter = new Parameter();
        parameter.setType(String[].class);
        parameter.setName("args");
        parameters.add(parameter);
        method.setParameters(parameters);
    }

    public void addBasicLinkedMethod(String name, ClassOrInterfaceDeclaration classToLink) {
        // TODO: check how imports will be handled. If the input class isn't imported at the top, it won't compile
        MethodDeclaration method = this.outputClass.addMethod(name);

        method.setModifiers(PUBLIC, STATIC);

        method.setType(void.class);

        NodeList<Parameter> parameters = new NodeList<>();
        Parameter param = new Parameter();
        param.setType(String.valueOf(classToLink.getName()));
        param.setName("inputClass");
        parameters.add(param);
        method.setParameters(parameters);

        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement("inputClass." + classToLink.getMethods().get(2).getName() + "();");
        method.setBody(methodBody);

        // TODO get package declaration from class compilation unit instead
        cu.addImport("com." + classToLink.getName().asString());
        cu.addImport("java.util.List");
    }

    public void setPackageDeclaration(String pkgDeclaration) {
        this.cu.setPackageDeclaration(pkgDeclaration);
    }
}
