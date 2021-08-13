package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.bootleg.builders.programs.utils.JPTypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Returns values of local variables. Input method parameters are also local variables.
 */
public class LocalVariableFetcher {
    private final BlockStmt instrsBlock;
    private NodeList<Parameter> methodParameters;

    public LocalVariableFetcher(BlockStmt instrsBlock) { this.instrsBlock = instrsBlock; }

    /**
     * Sets the method's parameters, which are special local variables.
     * @param methodParameters The method parameters
     */
    public void setMethodParameters(NodeList<Parameter> methodParameters) {
        this.methodParameters = methodParameters;
    }

    /**
     * @param wantedType The type of the variable being queried
     * @return The name of a random local variable / parameter of that given type
     */
    public Optional<VariableDeclarator> getLocalVarOrParamOfType(Type wantedType) {
        List<VariableDeclarator> candidateVars = new ArrayList<>();

        if (this.methodParameters != null && this.methodParameters.isNonEmpty()) {
            for (Parameter param : this.methodParameters) {
                if (param.getType().equals(wantedType))
                    candidateVars.add(new VariableDeclarator(param.getType(), param.getName()));
            }
        }

        for (Statement stmt: this.instrsBlock.getStatements()) {
            Expression stmtExpr = stmt.asExpressionStmt().getExpression();
            if (stmtExpr.isVariableDeclarationExpr()) {
                VariableDeclarationExpr expr = stmt.asExpressionStmt().getExpression().asVariableDeclarationExpr();
                if (expr.getVariable(0).getType().equals(wantedType))
                    candidateVars.add(expr.getVariable(0));
            }
        }

        if (candidateVars.isEmpty())
            return Optional.empty();
        else
            return Optional.of(candidateVars.get(new Random().nextInt(candidateVars.size())));
    }

    /**
     * @param objName The name of the object, as a string.
     * @return The name of a local variable / parameter of that given type
     */
    public Optional<VariableDeclarator> getLocalVarOrParamOfTypeObjFromStr(String objName) {
        try {
            return this.getLocalVarOrParamOfType(JPTypeUtils.getClassTypeFromName(objName));
        } catch (ParseException e) {
            System.err.println(e.getMessage()); // Ugly, but should never happen (famous last words)
            return Optional.empty();
        }
    }
}
