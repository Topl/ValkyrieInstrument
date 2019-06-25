package Option;

import java.io.IOException;

import Option.Valkyrie;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

class Main {
    public static void main(String args[]) throws IOException {
        String testValkyrie =
                "this.assetCreated = {}; \n" +
                        "this.createAssets = function(issuer, to, amount, assetCode, fee, data) { \n" +
                        "this.issuer = issuer; \n" +
                        "this.to = to; \n" +
                        "this.amount = amount; \n" +
                        "this.assetCode = assetCode; \n" +
                        "this.fee = fee; \n" +
                        "this.data = data; \n" +
                        "return assetCreated; } \n" +
                        "function create() { \n" +
                        "var res = createAssets('a', 'b', 10, 'testAssets', 0, ''); \n" +
                        "a = 2 + 2; }";

        String test =
                "function add(){a=2+2;}" ;


        System.out.println(TruffleInstrument.class.isAssignableFrom(Valkyrie.class));

        Engine engine = Engine.create();

//        TruffleInstrument.Env env = engine.getInstruments().get("Valkyrie").lookup(Valkyrie.class).getEnv();
//        assert(env.getInstruments().containsKey("Valkyrie") == true);
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

        System.out.println(context.getEngine().getInstruments().get("Valkyrie").getOptions());
        context.getEngine().getInstruments().get("Valkyrie").lookup(Object.class);

        context.eval("js", test);
        context.eval("js", "add()");
        System.out.println(context.getBindings("js").getMember("a"));
        System.out.println(context.getEngine().getInstruments().keySet());
        System.out.println(context.getEngine().getInstruments().get("Valkyrie"));

    }

}
