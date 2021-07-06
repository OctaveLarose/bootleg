package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.octavelarose.codegenerator.builders.BuildConstants;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.instructions.DummyValueCreator;
import com.github.octavelarose.codegenerator.builders.classes.instructions.MethodCallInstructionWriter;
import com.github.octavelarose.codegenerator.builders.programs.asm_types.ASMTypeParsingUtils;
import com.github.octavelarose.codegenerator.builders.programs.filereader.CTFileParser;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.github.octavelarose.codegenerator.builders.programs.filereader.CTFileParser.*;

/**
 * CallTrace Parser Program Builder.
 * Generates a program from a calltrace file of a format I defined myself.
 */
public class CTParserProgramBuilder implements ProgramBuilder {
    private final String ctFileName;

    public CTParserProgramBuilder(String ctFileName) {
        this.ctFileName = ctFileName;
    }

    public HashMap<String, ClassBuilder> build() throws BuildFailedException {
        System.out.println("Generating a program from the calltrace file: " + this.ctFileName);
        List<List<String>> fileLines = new CTFileParser(ctFileName).parse().getParsedCT();
        return buildFromCtLines(fileLines);
    }

    private HashMap<String, ClassBuilder> buildFromCtLines(List<List<String>> fileLines) throws BuildFailedException {
        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();
        Stack<Pair<ClassBuilder, CallableDeclaration.Signature>> callStack = new Stack<>();
        int i = 0;

        for (List<String> methodArr: fileLines) {
            i++;

            // We ignore lambda calls for now. TODO: look into why some are capitalized and some aren't
            if (methodArr.get(FULLNAME).contains("Lambda") || methodArr.get(FULLNAME).contains("lambda"))
                continue;

            if (!this.isFunctionEntry(methodArr.get(DIRECTION))) {
                callStack.pop();
                continue;
            }

//            System.out.println(i + " : " + methodArr);

            String[] splitFullName = methodArr.get(FULLNAME).split("\\.");
            String className = splitFullName[0];
            String methodName = splitFullName[1];

            // TODO static initializers, defined by <clinit>, are NOT handled so we pretend it's a regular public method
            if (methodName.equals("<clinit>")) {
                methodName = "staticInit";
                methodArr.set(SCOPE, methodArr.get(SCOPE).concat("/pub"));
            }

            ClassBuilder classCb = getOrCreateClassBuilder(classBuilders, className);

            if (classCb.hasMethod(methodName)) {
                callStack.push(new Pair<>(classCb, classCb.getMethodFromName(methodName).getSignature()));
                continue;
            }

            CallableDeclaration<?> methodNode = this.addNewMethodToClassFromCTInfo(methodName, methodArr, classCb);

            if (callStack.empty()) {
                System.out.println("Entry point: " + methodArr.get(FULLNAME));
            } else {
                MethodCallInstructionWriter mciw = new MethodCallInstructionWriter()
                        .setCaller(callStack.lastElement().a, callStack.lastElement().b)
                        .setCallee(classCb, methodNode.getSignature());
                mciw.writeMethodCallInCaller();
            }

            callStack.push(new Pair<>(classCb, methodNode.getSignature()));
        }

        return classBuilders;
    }

    /**
     * @param dirStr The string defining the method entry/exit.
     * @return true if it represents a function entry, false otherwise.
     */
    private boolean isFunctionEntry(String dirStr) {
        return dirStr.equals(">");
    }

    /**
     * Fetches a ClassBuilder with a given name from the already instantiated ClassBuilder list, or creates it accordingly
     * @param classBuilders The HashMap containing the ClassBuilders
     * @param className The name of the class wrapped in the ClassBuilder
     * @return The already existing, or newly created ClassBuilder object
     */
    private ClassBuilder getOrCreateClassBuilder(HashMap<String, ClassBuilder> classBuilders, String className) {
        ClassBuilder classCb;
        String PKG_NAME = "com.abc.random";

        if (classBuilders.containsKey(className)) {
            classCb = classBuilders.get(className);
        } else {
            if (!className.contains("/"))
                classCb = new BasicClassBuilder(className, 0, 0, PKG_NAME);
            else {
                List<String> splitClassPath = Arrays.asList(className.split("/"));
                String pkgPath = PKG_NAME + "." + String.join(".", splitClassPath.subList(0, splitClassPath.size() - 1));
//                System.out.println(splitClassPath.get(splitClassPath.size() - 1));
//                System.out.println(pkgPath);
                classCb = new BasicClassBuilder(splitClassPath.get(splitClassPath.size() - 1), 0, 0, pkgPath);
            }
            classBuilders.put(className, classCb);
        }

        return classCb;
    }


    /**
     * Adds a new method to a class, setting adequate parameters beforehand.
     * @param methodName The name of the method.
     * @param methodArr Info about the method in general.
     * @param classCb The class(builder) to which it needs to be added.
     */
    private CallableDeclaration<?> addNewMethodToClassFromCTInfo(String methodName,
                                                                 List<String> methodArr,
                                                                 ClassBuilder classCb) throws BuildFailedException {
        NodeList<Modifier> modifiers = this.getModifiersListFromScope(methodArr.get(SCOPE));

        String descriptor = methodArr.get(DESCRIPTOR);
        String[] splitDescriptor = descriptor.split("\\)");
        String paramsStr = splitDescriptor[0].substring(1);

        Type returnType = ASMTypeParsingUtils.getTypeFromStr(splitDescriptor[1]);

        // In the future, should ideally contain "advanced" operations. TODO a sleep() operation for starters
        BlockStmt methodBody = new BlockStmt();

        methodBody.addStatement(new NameExpr("System.out.println(\"" + "Current method: " + methodArr.get(FULLNAME) + "\")"));
        if (!returnType.isVoidType())
            methodBody.addStatement(new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));

        NodeList<Parameter> parameters = new NodeList<>();

        for (Type paramType: ASMTypeParsingUtils.getTypesFromParametersStr(paramsStr)) {
            // TODO: check for duplicate param names, just in case (more of a hassle than it seems)
            String paramName = RandomUtils.generateRandomName(3);
            parameters.add(new Parameter(paramType, paramName));
        }

        if (methodName.equals(BuildConstants.CONSTRUCTOR_NAME))
            return classCb.addConstructor(parameters, methodBody, modifiers);
        else
            return classCb.addMethod(methodName, returnType, parameters, methodBody, modifiers);
    }

    /**
     * Returns modifiers given a string defining a scope.
     * Needs a definition of the syntax I use somewhere, since it's my own standard.
     * @param scope A string defining the method's scope (public, private...).
     * @return a NodeList of Modifier objects, corresponding to the input scope
     */
    private NodeList<Modifier> getModifiersListFromScope(String scope) {
        NodeList<Modifier> modifiers = new NodeList<>();
        String[] splitScope = scope.split("/");

        for (String modStr: splitScope) {
            if (modStr.equals("pub"))
                modifiers.add(Modifier.publicModifier());
            if (modStr.equals("pri"))
                modifiers.add(Modifier.privateModifier());
            if (modStr.equals("pro"))
                modifiers.add(Modifier.protectedModifier());
            if (modStr.equals("sta"))
                modifiers.add(Modifier.staticModifier());
        }

        return modifiers;
    }
}
