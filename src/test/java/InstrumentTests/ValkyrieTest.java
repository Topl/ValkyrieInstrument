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


        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

//        ProgramController controller = new ProgramController(context);

        assert (Valkyrie.getController(context.getEngine()) != null);

//        //Setup controller
//        ProgramController controller = ProgramController.find(context.getEngine());
//        assert (controller != null);

        Instrument valkyrieInstrument = context.getEngine().getInstruments().get("Valkyrie");

//        System.out.println(valkyrieInstrument);
//        System.out.println(valkyrieInstrument.getOptions());
//        System.out.println(valkyrieInstrument.lookup(ProgramController.class));
//
//        ProgramController controller = valkyrieInstrument.lookup(ProgramController.class);

        ProgramController controller = ProgramController.find(context.getEngine());
        System.out.println(controller);
        context.eval("js", testValkyrie);
        context.eval("js", "create()");
        assert(!controller.getNewAssetInstances().isEmpty());

        assert(context.getBindings("js").getMember("res").as(boolean.class));

    }

}
