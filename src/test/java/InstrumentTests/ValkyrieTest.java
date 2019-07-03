package InstrumentTests;

import InstrumentClasses.ProgramController;
import InstrumentClasses.Valkyrie;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Instrument;
import org.junit.jupiter.api.Test;

public class ValkyrieTest {


    String test =
            "function add(){a = addResult(); function addResult(){return 2 + 2}}";

    String testValkyrie =
            "issuer = 'b';" +
            "issuer = 'bc';" +
            "function create() { " +
            "   var toAddress = 'a';" +
            "   res = Valkyrie_createAssets(issuer, toAddress, 10, 'testAssets', 0, ''); " +
            "   a = 2 + 2; }" +
            "function transferAssets() {" +
            "   var fromAddress = 'a';" +
            "   var toAddress = 'def';" +
            "   res = Valkyrie_transferAssets(issuer, fromAddress, toAddress, 10, 'testAssets', 0);}" +
            "function Valkyrie_createAssets(issuer, to, amount, assetCode, fee, data) {" +
            "   res = ValkyrieReserved.createAssets(issuer, to , amount, assetCode, fee, data);" +
            "   return res; }; " +
            "function Valkyrie_transferAssets(issuer, from, to, amount, assetCode, fee) {" +
            "   res = ValkyrieReserved.transferAssets(issuer, from, to , amount, assetCode, fee);" +
            "   return res; }; ";
    @Test
    void build() {

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();


        assert (Valkyrie.getController(context.getEngine()) != null);
        assert(ProgramController.find(context.getEngine()) != null);
    }

//    @Test
//    void testCreate() {
//
//        Context context = Context
//                .newBuilder("js")
//                .option("Valkyrie", "true")
//                .allowAllAccess(true)
//                .build();
//
//        Instrument valkyrieInstrument = context.getEngine().getInstruments().get("Valkyrie");
//
//        ProgramController controller = ProgramController.find(context.getEngine());
//        context.eval("js", testValkyrie);
//        context.eval("js", "create()");
//        assert (!controller.getNewAssetInstances().isEmpty());
//
//        assert (context.getBindings("js").getMember("res").as(boolean.class));
//    }

    @Test
    void testTransferAssets() {
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

        Instrument valkyrieInstrument = context.getEngine().getInstruments().get("Valkyrie");

        ProgramController controller = ProgramController.find(context.getEngine());
        context.eval("js", testValkyrie);
        context.eval("js", "create()");
        context.eval("js", "transferAssets()");

        assert (!controller.getNewAssetInstances().isEmpty());
        assert (context.getBindings("js").getMember("res").as(boolean.class));

        assert(controller.getNewAssetInstances().size() == 1);
        assert(controller.getNewAssetInstances().get(0).publicKey.equals("def"));
    }

}
