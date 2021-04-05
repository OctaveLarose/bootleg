package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
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

    ClassBuilder(String name, int methodsNbr, CompilationUnit cu) {
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

    public void addLinkedMethod(ClassOrInterfaceDeclaration classSpecToLink) {
//        outputClass.addMethod(this.generateLinkedMethod(generateRandomName(10), classSpecToLink));
    }

    public void addBasicMethod(String name) {
        MethodDeclaration method = this.outputClass.addMethod(name);

        method.setModifiers(PUBLIC, STATIC);

        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement("System.out.println(\"Hello world!\");");
        method.setBody(methodBody);

        method.setType(int.class);

        NodeList<Parameter> parameters = new NodeList<>();
        Parameter parameter = new Parameter();
        parameter.setType(String[].class);
        parameter.setName("args");
        method.setParameters(parameters);
    }

//    private MethodDeclaration generateLinkedMethod(String name, TypeSpec classSpecToLink) {
//        List<MethodDeclaration> methodsSpecs = classSpecToLink.methodSpecs;
//
//        // TODO: check how imports will be handled in that case. If the input class isn't imported, it won't compile
//        MethodDeclaration.Builder methodBuilder = MethodDeclaration.methodBuilder(name)
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .returns(void.class)
//                .addParameter(ClassName.bestGuess(classSpecToLink.name), "inputClass")
//                .addStatement("inputClass." + methodsSpecs.get(2).name + "()");
//
//        return methodBuilder.build();
//    }
}
