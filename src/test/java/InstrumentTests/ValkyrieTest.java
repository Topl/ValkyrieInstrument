package InstrumentTests;

import InstrumentClasses.ProgramController;
import InstrumentClasses.Valkyrie;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Instrument;
import org.junit.jupiter.api.Test;

public class ValkyrieTest {

    @Test
    void build() {
        String testValkyrie =
                "issuer = 'b';"+
                        "issuer = 'bc';" +
                        "function create() { " +
                        "var toAddress = 'a';" +
                        "res = Valkyrie_createAssets(issuer, toAddress, 10, 'testAssets', 0, ''); " +
                        "a = 2 + 2; }" +
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

        System.out.println(Valkyrie.getController(context.getEngine()));

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
