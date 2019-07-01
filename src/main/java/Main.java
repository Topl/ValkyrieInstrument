import java.io.IOException;

import InstrumentClasses.ProgramController;
import InstrumentClasses.Valkyrie;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Instrument;

class Main {
    public static void main(String args[]) throws IOException {
        String testValkyrie =
                "this.assetCreated = {};" +
                        "issuer = 'b';"+
                        "issuer = 'bc';" +
                        "function create() { " +
                            "var toAddress = 'a';" +
                            "res = Valkyrie_createAssets(issuer, toAddress, 10, 'testAssets', 0, ''); " +
//                            "res2 = Valkyrie_createAssets(issuer, issuer, issuer, issuer, issuer, issuer);" +
                            "a = 2 + 2; }" +
////                                "this.Valkyrie.createAssets = function(a, b, c, d, e, f){return true};";
//                        "this.createAssets = function(issuer, to, amount, assetCode, fee, data) {" +
//                            "this.assetCreated.issuer = issuer; " +
//                            "this.assetCreated.to = to; " +
//                            "this.assetCreated.amount = amount; " +
//                            "this.assetCreated.assetCode = assetCode; " +
//                            "this.assetCreated.fee = fee; " +
//                            "this.assetCreated.data = data; " +
//                            "return assetCreated; }; " +
                        "function Valkyrie_createAssets(issuer, to, amount, assetCode, fee, data) {" +
                        "res = ValkyrieReserved.createAssets(issuer, to , amount, assetCode, fee, data);" +
                        "return res; }; ";

        String test =
                "function add(){a = addResult(); function addResult(){return 2 + 2}}" ;


//        TruffleInstrument.Env env = engine.getInstruments().get("Valkyrie").lookup(Valkyrie.class).getEnv();
//        assert(env.getInstruments().containsKey("Valkyrie") == true);
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

//        ProgramController controller = new ProgramController(context);

//        context.getEngine().getInstruments().get("Valkyrie").lookup(ProgramController.class);

//        //Setup controller
//        ProgramController controller = ProgramController.find(context.getEngine());
//        assert (controller != null);

        Instrument valkyrieInstrument = context.getEngine().getInstruments().get("Valkyrie");

//        System.out.println(valkyrieInstrument);
//        System.out.println(valkyrieInstrument.getOptions());
//        System.out.println(valkyrieInstrument.lookup(ProgramController.class));
//
//        ProgramController controller = valkyrieInstrument.lookup(ProgramController.class);

        ProgramController controller2 = ProgramController.find(context.getEngine());
        System.out.println(controller2);
        context.eval("js", testValkyrie);
        context.eval("js", "create()");
        System.out.println(controller2.getNewAssetInstances());
//        System.out.println(context.getBindings("js").getMemberKeys());
        System.out.println(context.getBindings("js").getMember("res"));
//        System.out.println(ProgramController.getNewAssetInstances());



    }

}
