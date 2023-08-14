package wyk.instrumentation;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String argentArgs, Instrumentation inst) {
        inst.addTransformer(new MethodNameTransformer());
    }
}
