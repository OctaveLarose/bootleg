package com.github.octavelarose.codegenerator.javapoet;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Random;

public class ClassBuilder {
    TypeSpec.Builder outputClass;

    ClassBuilder(String name, int methodsNbr) {
        this.outputClass = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (int i = 0; i < methodsNbr; i++)
            outputClass.addMethod(this.generateBasicMethod(generateRandomName(i + 5)));
    }

    public TypeSpec build() {
        return outputClass.build();
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

    public void addLinkedMethod(TypeSpec classSpecToLink) {
        outputClass.addMethod(this.generateLinkedMethod(generateRandomName(10), classSpecToLink));
    }

    private MethodSpec generateBasicMethod(String name) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello world!");

        return methodBuilder.build();
    }

    private MethodSpec generateLinkedMethod(String name, TypeSpec classSpecToLink) {
        List<MethodSpec> methodsSpecs = classSpecToLink.methodSpecs;

        // TODO: check how imports will be handled in that case. If the input class isn't imported, it won't compile
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.bestGuess(classSpecToLink.name), "inputClass")
                .addStatement("inputClass." + methodsSpecs.get(2).name + "()");

        return methodBuilder.build();
    }
}
