package com.github.octavelarose.codegenerator.builders.classes;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.ast_visitors.ClassNameCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Builds a basic class, mostly to try out the API and generate basic codebases.
 */
public class BasicClassBuilder extends ClassBuilder {

    public BasicClassBuilder(String name, int methodsNbr) {
        super(name);

        this.setModifiers(new NodeList<>(Modifier.publicModifier()));
        this.addConstructor();

        for (int i = 0; i < methodsNbr; i++)
            this.addBasicMethod(generateRandomName(i + 5));
    }

    /**
     * Generates a random name. Used to name methods, among others. May need to be moved to a "Utils" class.
     *
     * @param nbrCharacters Number of characters of the output string.
     * @return A string made up of nbrCharacters random characters.
     */
    private String generateRandomName(int nbrCharacters) {
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(nbrCharacters);
        for (int i = 0; i < nbrCharacters; i++) {
            int randomLimitedInt = 'a' + (int) (random.nextFloat() * ('z' - 'a' + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }

    /**
     * Adds a basic method that just contains a basic print operation.
     * @param name The name of the method.
     */
    public void addBasicMethod(String name) {
        NodeList<Modifier> modifiers = new NodeList<>(Modifier.publicModifier());

        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement("System.out.println(\"" + generateRandomName(6) + "\");");

        Parameter parameter = new Parameter().setType(int.class).setName("args");
        NodeList<Parameter> parameters = new NodeList<>(parameter);

        VoidType returnType = new VoidType();

        this.addMethod(name, returnType, parameters, methodBody, modifiers);
    }

    /**
     * Adds a basic method that takes as input another class, and calls a random method of this class.
     *
     * @param methodName         The name of the method.
     * @param classBuilderToLink The ClassBuilder that contains the method parameter class.
     * @throws BuildFailedException To be thrown if the build fails (invalid input class, mostly)
     */
    public void addBasicLinkedMethod(String methodName, BasicClassBuilder classBuilderToLink) throws BuildFailedException {
        CompilationUnit cuClassToLink = classBuilderToLink.getCompilationUnit();

        // TODO: come on, we can make that bit cleaner. We're big boys
        List<String> classNames = new ArrayList<>();
        VoidVisitor<List<String>> classNameVisitor = new ClassNameCollector();
        classNameVisitor.visit(cuClassToLink, classNames);
        String className = classNames.get(0);

        Optional<ClassOrInterfaceDeclaration> classToLinkOptional = cuClassToLink.getClassByName(className);
        if (classToLinkOptional.isEmpty())
            throw new BuildFailedException("Cannot find class to link in linked method input CU");
        ClassOrInterfaceDeclaration classToLink = classToLinkOptional.get();

        Parameter parameter = new Parameter()
                .setType(String.valueOf(classToLink.getName()))
                .setName("inputClass");
        NodeList<Parameter> parameters = new NodeList<>(parameter);

        // TODO only call public methods... or protected if same package. A bit of a hassle and a job for future me
        BlockStmt methodBody = new BlockStmt()
                .addStatement("inputClass." + classToLink.getMethods().get(2).getName() + "(3);");

        this.addMethod(
                methodName,
                new VoidType(),
                parameters,
                methodBody,
                new NodeList<>(Modifier.publicModifier())
        );

        Optional<PackageDeclaration> pkgDeclaration = cuClassToLink.getPackageDeclaration();
        if (pkgDeclaration.isPresent()) {
            // TODO: It doesn't use it since it's the same pkg in my test case (I believe). Need to make sure
            String importDeclaration = pkgDeclaration.get().getNameAsString() + "." + className;
            this.addImport(importDeclaration);
        } else
            throw new BuildFailedException("Attempted to create a linked method with a class with no pkg declaration.");
    }
}
