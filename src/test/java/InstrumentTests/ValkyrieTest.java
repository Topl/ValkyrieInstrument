package InstrumentTests;

import InstrumentClasses.ProgramController;
import InstrumentClasses.TokenClasses.ArbitInstance;
import InstrumentClasses.TokenClasses.AssetInstance;
import InstrumentClasses.Valkyrie;
import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            "function transferArbits() {" +
            "   var fromAddress = 'a';" +
            "   var toAddress = 'def';" +
            "   res = Valkyrie_transferArbits(fromAddress, toAddress, 10, 0);}" +
            "function Valkyrie_createAssets(issuer, to, amount, assetCode, fee, data) {" +
            "   res = ValkyrieReserved.createAssets(issuer, to , amount, assetCode, fee, data);" +
            "   return res; }; " +
            "function Valkyrie_transferAssets(issuer, from, to, amount, assetCode, fee) {" +
            "   var res = ValkyrieReserved.transferAssets(issuer, from, to , amount, assetCode, fee);" +
            "   return res; }; " +
            "function Valkyrie_transferArbits(from, to, amount, fee) {" +
            "   var res = ValkyrieReserved.transferArbits(from, to , amount, fee);" +
            "   return res; }; ";
    @Test
    void build() {

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();


        assertTrue(Valkyrie.getController(context.getEngine()) != null);
        assertTrue(ProgramController.find(context.getEngine()) != null);
    }

    @Test
    void testCreate() {

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

        ProgramController controller = ProgramController.find(context.getEngine());
        context.eval("js", testValkyrie);
        context.eval("js", "create()");
        assertFalse(controller.getNewAssetInstances().isEmpty());
        assertTrue(context.getBindings("js").getMember("res").asBoolean());
        controller.clear();
    }

    @Test
    void testTransferAssets() {
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

        ProgramController controller = ProgramController.find(context.getEngine());
        context.eval("js", testValkyrie);
        context.eval("js", "create()");
        context.eval("js", "transferAssets()");

        assertFalse(controller.getNewAssetInstances().isEmpty());
        assertTrue(context.getBindings("js").getMember("res").asBoolean());

        assertEquals(1, controller.getNewAssetInstances().size());
        assertTrue(controller.getNewAssetInstances().get(0).publicKey.equals("def"));
        assertEquals(10, controller.getNewAssetInstances().get(0).amount);
        controller.clear();
    }

    @Test
    void transferAssetsWithInputBox() {
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

        byte[] inputBox = {0, 1};

        ProgramController controller = ProgramController.find(context.getEngine());

        AssetInstance assetInstance = new AssetInstance("a", "bc", "testAssets", new Long(12), "", inputBox);
        ArrayList<AssetInstance> assetBoxesForUse = new ArrayList();
        assetBoxesForUse.add(assetInstance);
        controller.setAssetBoxesForUse(assetBoxesForUse);

        context.eval("js", testValkyrie);
        context.eval("js", "transferAssets()");

        assertTrue(context.getBindings("js").getMember("res").asBoolean());

        assertEquals(1, controller.getBoxesToRemove().size());
        assertEquals(inputBox, controller.getBoxesToRemove().get(0));

        assertEquals(2, controller.getNewAssetInstances().size());
        assertTrue(controller.getNewAssetInstances().get(0).publicKey.equals("def"));
        assertTrue(controller.getNewAssetInstances().get(1).publicKey.equals("a"));
        assertEquals(assetInstance.amount, controller.getNewAssetInstances().get(0).amount + controller.getNewAssetInstances().get(1).amount);
        controller.clear();
    }

    @Test
    void transferArbitsWithInputBox() {
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

        byte[] inputBox = {0, 1};

        ProgramController controller = ProgramController.find(context.getEngine());

        ArbitInstance arbitInstance = new ArbitInstance("a", new Long(12), inputBox);
        ArrayList<ArbitInstance> arbitBoxesForUse = new ArrayList();
        arbitBoxesForUse.add(arbitInstance);
        controller.setArbitBoxesForUse(arbitBoxesForUse);

        context.eval("js", testValkyrie);
        context.eval("js", "transferArbits()");

        assertTrue(context.getBindings("js").getMember("res").asBoolean());

        assertEquals(1, controller.getBoxesToRemove().size());
        assertEquals(inputBox, controller.getBoxesToRemove().get(0));

        assertEquals(2, controller.getNewArbitInstances().size());
        assertTrue(controller.getNewArbitInstances().get(0).publicKey.equals("def"));
        assertTrue(controller.getNewArbitInstances().get(1).publicKey.equals("a"));
        assertEquals(arbitInstance.amount, controller.getNewArbitInstances().get(0).amount + controller.getNewArbitInstances().get(1).amount);
        controller.clear();

    }

}
