package com.github.octavelarose.codegenerator.builders.classes;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType;

/**
 * The program entry point, i.e main class that contains the main function.
 */
public class EntryPointBuilder extends ClassBuilder {

    public EntryPointBuilder(String name, String pkgDeclarationStr) {
        super(name);
        this.setPackageDeclaration(pkgDeclarationStr);
        this.generateEntryPointFunction();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void generateEntryPointFunction() {
        BlockStmt methodBody = getMainFunctionBody();

        // TODO: why is it so hard to get a String class? Surely we can improve upon this
        Parameter parameter = new Parameter()
                .setType(new ArrayType(new JavaParser().parseClassOrInterfaceType("String").getResult().get()))
                .setName("args");
        NodeList<Parameter> parameters = new NodeList<>(parameter);

        this.addMethod(
                "main",
                new PrimitiveType(PrimitiveType.Primitive.INT),
                parameters,
                methodBody,
                new NodeList<>(Modifier.publicModifier())
        );
    }

    private BlockStmt getMainFunctionBody() {
        return new BlockStmt().addStatement("return (0);");
    }

}
