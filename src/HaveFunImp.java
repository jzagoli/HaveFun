import value.*;
import gen.*;

import java.util.LinkedList;
import java.util.List;

public class HaveFunImp extends HaveFunBaseVisitor<Value> {

    private final Conf conf;

    public HaveFunImp(Conf conf) {
        this.conf = conf;
    }

    private ComValue visitCom(HaveFunParser.ComContext ctx) {
        return (ComValue) visit(ctx);
    }

    private ExpValue<?> visitExp(HaveFunParser.ExpContext ctx) {
        return (ExpValue<?>) visit(ctx);
    }

    private int visitNatExp(HaveFunParser.ExpContext ctx) {
        try {
            return ((NatValue) visitExp(ctx)).toJavaValue();
        } catch (ClassCastException e) {
            System.err.println("Type mismatch exception!");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());
            System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
            System.err.println(ctx.getText());
            System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<");
            System.err.println("> Natural expression expected.");
            System.exit(1);
        }

        return 0; // unreachable code
    }

    private boolean visitBoolExp(HaveFunParser.ExpContext ctx) {
        try {
            return ((BoolValue) visitExp(ctx)).toJavaValue();
        } catch (ClassCastException e) {
            System.err.println("Type mismatch exception!");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());
            System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
            System.err.println(ctx.getText());
            System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<");
            System.err.println("> Boolean expression expected.");
            System.exit(1);
        }

        return false; // unreachable code
    }

    @Override
    public ComValue visitProg(HaveFunParser.ProgContext ctx) {
        for (HaveFunParser.FunContext funCtx : ctx.fun()) {
            visitFun(funCtx);
        }
        return visitCom(ctx.com());
    }

    @Override
    public ComValue visitIf(HaveFunParser.IfContext ctx) {
        return visitBoolExp(ctx.exp())
                ? visitCom(ctx.com(0))
                : visitCom(ctx.com(1));
    }

    @Override
    public ComValue visitAssign(HaveFunParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        ExpValue<?> v = visitExp(ctx.exp());

        conf.update(id, v);

        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitFun(HaveFunParser.FunContext ctx) {
        String name = ctx.ID(0).getText();
        if (conf.isFunctionDefined(name)) {
            System.err.println("Fun " + name + " already defined!");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());
            System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
            System.err.println(ctx.getText());
            System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<");
            System.exit(1);
        }
        List<String> parameters = new LinkedList<>();
        // can't do a foreach because one ID is the function name and I cannot remove it from the list
        for (int i = 1; i < ctx.ID().size(); i++) {
            String paramName = ctx.ID(i).getText();
            if (parameters.contains(paramName)) {
                System.err.println("Parameter name " + paramName + " clashes with previous parameters");
                System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());
                System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
                System.err.println(ctx.getText());
                System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<");
                System.exit(1);
            } else {
                parameters.add(paramName);
            }
        }
        HaveFunParser.ComContext body = ctx.com();
        HaveFunParser.ExpContext retExp = ctx.exp();
        Function function = new Function(parameters, body, retExp);
        conf.defineFunction(name, function);

        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitSkip(HaveFunParser.SkipContext ctx) {
        return ComValue.INSTANCE;
    }

    @Override
    public ComValue visitSeq(HaveFunParser.SeqContext ctx) {
        visitCom(ctx.com(0));
        return visitCom(ctx.com(1));
    }

    @Override
    public ComValue visitWhile(HaveFunParser.WhileContext ctx) {
        if (!visitBoolExp(ctx.exp()))
            return ComValue.INSTANCE;

        visitCom(ctx.com());

        return visitWhile(ctx);
    }

    @Override
    public ComValue visitOut(HaveFunParser.OutContext ctx) {
        System.out.println(visitExp(ctx.exp()));
        return ComValue.INSTANCE;
    }

    @Override
    public NatValue visitNat(HaveFunParser.NatContext ctx) {
        return new NatValue(Integer.parseInt(ctx.NAT().getText()));
    }

    @Override
    public BoolValue visitBool(HaveFunParser.BoolContext ctx) {
        return new BoolValue(Boolean.parseBoolean(ctx.BOOL().getText()));
    }

    @Override
    public ExpValue<?> visitParExp(HaveFunParser.ParExpContext ctx) {
        return visitExp(ctx.exp());
    }

    @Override
    public NatValue visitPow(HaveFunParser.PowContext ctx) {
        int base = visitNatExp(ctx.exp(0));
        int exp = visitNatExp(ctx.exp(1));

        return new NatValue((int) Math.pow(base, exp));
    }

    @Override
    public BoolValue visitNot(HaveFunParser.NotContext ctx) {
        return new BoolValue(!visitBoolExp(ctx.exp()));
    }

    @Override
    public NatValue visitDivMulMod(HaveFunParser.DivMulModContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case HaveFunParser.DIV -> new NatValue(left / right);
            case HaveFunParser.MUL -> new NatValue(left * right);
            case HaveFunParser.MOD -> new NatValue(left % right);
            default -> null;
        };
    }

    @Override
    public NatValue visitPlusMinus(HaveFunParser.PlusMinusContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case HaveFunParser.PLUS -> new NatValue(left + right);
            case HaveFunParser.MINUS -> new NatValue(Math.max(left - right, 0));
            default -> null;
        };
    }

    @Override
    public BoolValue visitEqExp(HaveFunParser.EqExpContext ctx) {
        ExpValue<?> left = visitExp(ctx.exp(0));
        ExpValue<?> right = visitExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case HaveFunParser.EQQ -> new BoolValue(left.equals(right));
            case HaveFunParser.NEQ -> new BoolValue(!left.equals(right));
            default -> null; // unreachable code
        };
    }

    @Override
    public ExpValue<?> visitId(HaveFunParser.IdContext ctx) {
        String id = ctx.ID().getText();

        if (!conf.contains(id)) {
            System.err.println("Variable " + id + " used but never instantiated");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());

            System.exit(1);
        }

        return conf.get(id);
    }

    @Override
    public ExpValue<?> visitFunCall(HaveFunParser.FunCallContext ctx) {
        String name = ctx.ID().getText();
        if (!conf.isFunctionDefined(name)) {
            System.err.println("Function " + name + " used but never declared");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());
            System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
            System.err.println(ctx.getText());
            System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<");
            System.exit(1);
        }
        Function function = conf.getFunction(name);
        if (function.getParameters().size() != ctx.exp().size()) {
            System.err.println("Function " + name + " called with the wrong number of arguments");
            System.err.println("@" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine());
            System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>");
            System.err.println(ctx.getText());
            System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<");
            System.exit(1);
        }
        List<ExpValue<?>> actualParams = new LinkedList<>();
        for (HaveFunParser.ExpContext expCtx : ctx.exp()) {
            actualParams.add(visitExp(expCtx));
        }
        conf.activateRecord(function.getParameters(),actualParams);
        if (function.getBody() != null) {
            visitCom(function.getBody());
        }
        ExpValue<?> retValue = visitExp(function.getRetExp());
        conf.deactivateRecord();
        return retValue;
    }

    @Override
    public BoolValue visitCmpExp(HaveFunParser.CmpExpContext ctx) {
        int left = visitNatExp(ctx.exp(0));
        int right = visitNatExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case HaveFunParser.GEQ -> new BoolValue(left >= right);
            case HaveFunParser.LEQ -> new BoolValue(left <= right);
            case HaveFunParser.LT  -> new BoolValue(left < right);
            case HaveFunParser.GT  -> new BoolValue(left > right);
            default -> null;
        };
    }

    @Override
    public BoolValue visitLogicExp(HaveFunParser.LogicExpContext ctx) {
        boolean left = visitBoolExp(ctx.exp(0));
        boolean right = visitBoolExp(ctx.exp(1));

        return switch (ctx.op.getType()) {
            case HaveFunParser.AND -> new BoolValue(left && right);
            case HaveFunParser.OR -> new BoolValue(left || right);
            default -> null;
        };
    }
}
