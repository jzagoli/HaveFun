import value.ExpValue;
import value.Function;

import java.util.*;

public class Conf {

    private final Deque<Frame> stack = new LinkedList<>();
    private final Map<String, Function> functions = new HashMap<>();

    public Conf() {
        Frame main = new Frame();
        stack.addFirst(main);
    }

    // FUNCTIONS METHODS

    public boolean isFunctionDefined(String name) {return functions.containsKey(name);}

    public void defineFunction(String name, Function function) {functions.put(name,function);}

    public Function getFunction(String name) {return functions.get(name);}

    public void activateRecord(List<String> formalParams, List<ExpValue<?>> actualParams) {
        Frame frame = new Frame();
        for (int i = 0; i < formalParams.size(); i++) {
            frame.put(formalParams.get(i),actualParams.get(i));
        }
        stack.addFirst(frame);
    }

    public void deactivateRecord() {
        stack.removeFirst();
    }

    // ID METHODS

    public boolean contains(String id) {
        assert stack.peekFirst() != null;
        return stack.peekFirst().containsKey(id);
    }

    public ExpValue<?> get(String id) {
        assert stack.peekFirst() != null;
        return stack.peekFirst().get(id);
    }

    public void update(String id, ExpValue<?> v) {
        assert stack.peekFirst() != null;
        stack.peekFirst().put(id, v);
    }

    // Renaming the HashMap
    private static class Frame extends HashMap<String, ExpValue<?>>{}

    /*
    Stack Method	Equivalent Deque Method
    push(e)	        addFirst(e)
    pop()	        removeFirst()
    peek()	        peekFirst()
    */

}
