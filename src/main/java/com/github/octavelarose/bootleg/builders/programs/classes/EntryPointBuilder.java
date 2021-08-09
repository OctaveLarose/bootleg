package com.github.octavelarose.bootleg.builders.programs.classes;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import com.github.octavelarose.bootleg.builders.programs.utils.JPTypeUtils;

import java.util.List;

/**
 * The program entry point, i.e main class that contains the main function.
 */
public class EntryPointBuilder extends ClassBuilder {
    List<ClassBuilder> classesToInvoke;

    public EntryPointBuilder(String name, String pkgDeclarationStr, List<ClassBuilder> classesToInvoke) {
        super(name);
        this.classesToInvoke = classesToInvoke;

        this.setPackageDeclaration(pkgDeclarationStr);
        this.generateEntryPointFunction();
    }

    private void generateEntryPointFunction() {
        BlockStmt methodBody = getMainFunctionBody();
        ClassOrInterfaceType stringType;

        try {
            stringType = JPTypeUtils.getClassTypeFromName("String");
        } catch (ParseException e) {
            System.err.println("Failed to get the class type for a string, which should never happen.");
            return;
        }

        Parameter parameter = new Parameter()
                .setType(new ArrayType(stringType))
                .setName("args");
        NodeList<Parameter> parameters = new NodeList<>(parameter);

        this.addMethod(
                "main",
                new VoidType(),
                parameters,
                methodBody,
                new NodeList<>(Modifier.publicModifier(), Modifier.staticModifier())
        );

        for (ClassBuilder cb : this.classesToInvoke) {
            this.addImport(cb.getImportStr());
        }
    }

    private BlockStmt getMainFunctionBody() {
        BlockStmt mainFunctionBody = new BlockStmt()
                .addStatement("TestClass testClass = new TestClass();")
                .addStatement("HelperClass helperClass = new HelperClass();");

        mainFunctionBody.addStatement("System.out.println(\"START OF GENERATED CODE OUTPUT.\");");


        List<MethodDeclaration> methodsTest = classesToInvoke.get(0).getMethods();
        List<MethodDeclaration> methodsHelper = classesToInvoke.get(1).getMethods();

        for (MethodDeclaration md : methodsHelper) {
            String statement = "helperClass" + "." + md.getNameAsString() + "(" + "3" + ");";
            mainFunctionBody.addStatement(statement);
        }

        for (MethodDeclaration md : methodsTest) {
            if (md.getParameters().get(0).getName().asString().equals("inputClass")) {
                mainFunctionBody.addStatement("testClass" + "." + md.getNameAsString() + "(" + "helperClass" + ");");
            } else {
                mainFunctionBody.addStatement("testClass" + "." + md.getNameAsString() + "(" + "3" + ");");
            }
        }

        mainFunctionBody.addStatement("System.out.println(\"END OF GENERATED CODE OUTPUT.\");");

        return mainFunctionBody;
    }

}
