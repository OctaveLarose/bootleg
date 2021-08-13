package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.calltraces.asm_types.ASMBytecodeParsingUtils;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.DummyValueCreator;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.LocalVariableFetcher;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.visitors.VarInstantiatorVisitor;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.List;
import java.util.Optional;

/**
 * Creates and manages a method body, i.e a BlockStmt object.
 */
public abstract class MethodBodyEditor {
    protected final BlockStmt instrsBlock = new BlockStmt();
    protected ReturnStmt returnStmt;

    // Used to return values of local variables.
    protected LocalVariableFetcher varFetcher = new LocalVariableFetcher(instrsBlock);

    /**
     * Default constructor, creates a BlockStmt instance.
     */
    public MethodBodyEditor() {}

    /**
     * Generates the method body. Not meant to be called by itself, and rather wrapped in another method by a subclass.
     * @return The method body.
     */
    protected BlockStmt generateMethodBody() {
        NodeList<Statement> concatStmts = new NodeList<>();

        for (Statement stmt: this.instrsBlock.getStatements())
            concatStmts.add(stmt);
        if (this.returnStmt != null)
            concatStmts.add(this.returnStmt);

        return new BlockStmt(concatStmts);
    }

    /**
     * Adds a statement to the method body.
     * @param exprStmt The statement as an ExpressionStmt.
     */
    public void addStatement(ExpressionStmt exprStmt) {
        this.instrsBlock.addStatement(exprStmt);
    }

    /**
     * Adds a return statement.
     * @param returnStmt A return statement object.
     */
    public void setReturnStatement(ReturnStmt returnStmt) {
        this.returnStmt = returnStmt;
    }

    /**
     * Adds a random return statement of a given type, unless the type fed is void in which case none is necessary.
     * @param returnType The return type of the method.
     */
    public void setRandomReturnStatement(Type returnType) {
        if (!returnType.isVoidType())
            this.setReturnStatement(new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));
    }

    /**
     * Adds a return statement of a given type, making sure a local variable is returned.
     * If none are eligible, then a random value is returned instead.
     * @param returnType The return type of the method.
     */
    public boolean setReturnStatementFromLocalVar(Type returnType) {
        Optional<VariableDeclarator> localVar = this.varFetcher.getLocalVarOrParamOfType(returnType);

        if (localVar.isPresent())
            this.setReturnStatement(new ReturnStmt(new NameExpr(localVar.get().getName())));
        else if (!returnType.isClassOrInterfaceType())
            this.setRandomReturnStatement(returnType);
        else
            return false;

        return true;
    }

    /**
     * Adds a return statement as a new class instantiation. If the class has no constructors, set it to a null expression.
     * @param classCb The class
     */
    public void setReturnStatementAsNewClass(ClassBuilder classCb) {
        if (classCb.getConstructors().size() == 0)
            this.returnStmt = new ReturnStmt(new NullLiteralExpr());
        else
            this.returnStmt = new ReturnStmt(new ObjectCreationExpr()
                    .setType(classCb.getImportStr())
                    .setArguments(DummyValueCreator.getDummyParameterValuesAsExprs(classCb.getConstructors().get(0).getParameters())));
    }


    /**
     * @return true if the method has a return statement, false otherwise.
     */
    public boolean hasReturnStatement() {
        return this.returnStmt != null;
    }

    /**
     * Sets the method's parameters.
     * @param methodParameters The method parameters
     */
    public void setMethodParameters(NodeList<Parameter> methodParameters) {
        this.varFetcher.setMethodParameters(methodParameters);
    }

    /**
     * Processes a list of arithmetic operations (ADD, SUB, etc...) and adds them to the method body.
     * @param methodOps The operations list (ex: ["IADD", "DSUB", "DDIV", ...])
     */
    public void processOperationStatements(List<String> methodOps) throws BuildFailedException {
        for (String opStr: methodOps) {
            Type opType = ASMBytecodeParsingUtils.getTypeFromBytecodePrefix(opStr.charAt(0));
            AssignExpr.Operator operator = ASMBytecodeParsingUtils.getAssignOperatorFromBytecodeStr(opStr.substring(1));

            Optional<VariableDeclarator> localVarName = this.varFetcher.getLocalVarOrParamOfType(opType);

            if (localVarName.isEmpty()) {
                this.addStatement(new ExpressionStmt(
                        new VariableDeclarationExpr(
                            new VariableDeclarator(opType, RandomUtils.generateRandomName(5),
                                new NameExpr(DummyValueCreator.getDummyParamValueFromType(opType))))
                ));
            } else {
                this.addStatement(new ExpressionStmt(
                        new AssignExpr(
                            new NameExpr(localVarName.get().getName()),
                            new NameExpr(DummyValueCreator.getDummyParamValueFromType(opType)),
                            operator)
                ));
            }
        }
    }

    /**
     * Accepts a visitor that will instantiate a new variable in the class context.
     * @param varInstVisitor The visitor object that contains the variable instantiation logic.
     * @throws BuildFailedException If instantiating the variable goes wrong.
     */
    public void accept(VarInstantiatorVisitor varInstVisitor) throws BuildFailedException {
        varInstVisitor.visit(this, this.varFetcher);
    }
}
