package org.jruby.ir.instructions;

import java.util.Map;
import org.jruby.ir.IRMethod;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.transformations.inlining.InlinerInfo;
import org.jruby.ir.targets.JVM;

public class ReturnInstr extends ReturnBase {
    public final IRMethod methodToReturnFrom;

    public ReturnInstr(Operand returnValue, IRMethod methodToReturnFrom) {
        super(Operation.RETURN, returnValue);
        this.methodToReturnFrom = methodToReturnFrom;
    }

    public ReturnInstr(Operand returnValue) {
        this(returnValue, null);
    }

    @Override
    public String toString() { 
        return getOperation() + "(" + returnValue + (methodToReturnFrom == null ? "" : ", <" + methodToReturnFrom.getName() + ">") + ")";
    }

    @Override
    public Instr cloneForInlining(InlinerInfo ii) {
        return new ReturnInstr(returnValue.cloneForInlining(ii), methodToReturnFrom);
    }

    @Override
    public Instr cloneForInlinedScope(InlinerInfo ii) {
        if (methodToReturnFrom == null) {
            Variable v = ii.getCallResultVariable();
            return v == null ? null : new CopyInstr(v, returnValue.cloneForInlining(ii));
        } else if (ii.getInlineHostScope() == methodToReturnFrom) {
            // Convert to a regular return instruction
            return new ReturnInstr(returnValue.cloneForInlining(ii));
        } else {
            return cloneForInlining(ii);
        }
    }

    public void compile(JVM jvm) {
        jvm.emit(getOperands()[0]);
        jvm.method().returnValue();
    }
}
