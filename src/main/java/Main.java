import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import org.graalvm.polyglot.Context;

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


        PrintStream myOut = new PrintStream(new FileOutputStream(File.createTempFile("myOut", ".txt")));

        System.out.println(TruffleInstrument.class.isAssignableFrom(Valkyrie.class));

        Context context1 = Context.create("js");
        System.out.println(context1.getEngine().getInstruments().get("Valkyrie").getOptions());


        Context context = Context
                .newBuilder("js")
//                .out(myOut)
                .option("Valkyrie", "true")
                .option("Valkyrie.Valkyrie", "true")
                .allowAllAccess(true)
                .build();
//        Context context = Context.create("js").;
        System.out.println(context.getEngine().getInstruments().get("Valkyrie").getOptions());
        context.getEngine().getInstruments().get("Valkyrie").lookup(Object.class);

//        context.getBindings("js").putMember("Valkyrie", ValkyrieInstrumentation.class);
        context.eval("js", test);
        context.eval("js", "add()");
        System.out.println(context.getBindings("js").getMember("a"));
        System.out.println(context.getEngine().getInstruments().keySet());
        System.out.println(context.getEngine().getInstruments().get("Valkyrie"));

//        context.eval(Source.create("js", testValkyrie));
//        valk.run(Source.create("js", testValkyrie), context);
        myOut.flush();
        myOut.close();
    }

}
