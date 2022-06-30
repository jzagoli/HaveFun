package value;

import gen.HaveFunParser;

import java.util.List;

public class Function {

    private final List<String> parameters;
    private final HaveFunParser.ComContext body;
    private final HaveFunParser.ExpContext retExp;

    public Function(List<String> parameters, HaveFunParser.ComContext body, HaveFunParser.ExpContext retExp) {
        this.parameters = parameters;
        this.body = body;
        this.retExp = retExp;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public HaveFunParser.ComContext getBody() {
        return body;
    }

    public HaveFunParser.ExpContext getRetExp() {
        return retExp;
    }
}
