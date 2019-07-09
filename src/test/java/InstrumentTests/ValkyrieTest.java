package InstrumentTests;

import InstrumentClasses.ProgramController;
import InstrumentClasses.TokenClasses.ArbitInstance;
import InstrumentClasses.TokenClasses.AssetInstance;
import InstrumentClasses.TokenClasses.TokenInstance;
import InstrumentClasses.Valkyrie;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

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
            //To be appended to context
            "function Valkyrie_createAssets(issuer, to, amount, assetCode, fee, data) {" +
            "   res = ValkyrieReserved.createAssets(issuer, to , amount, assetCode, fee, data);" +
            "   if (res === true)" +
            "       return res; " +
            "   else throw new Error(res);};" +
            "function Valkyrie_transferAssets(issuer, from, to, amount, assetCode, fee) {" +
            "   var res = ValkyrieReserved.transferAssets(issuer, from, to , amount, assetCode, fee);" +
            "   if (res === true)" +
            "       return res; " +
            "   else throw new Error(res);};" +
            "function Valkyrie_transferArbits(from, to, amount, fee) {" +
            "   var res = ValkyrieReserved.transferArbits(from, to , amount, fee);" +
            "   if(res === true) " +
            "       return res; " +
            "   else throw new Error(res);}; ";
    @Test
    void build() {

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .allowAllAccess(true)
                .build();

        assertTrue(Valkyrie.getController(context.getEngine()) != null);
        assertTrue(ProgramController.find(context.getEngine()) != null);
        context.close();
    }

    @Test
    void testCreate() {

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .build();

        ProgramController controller = ProgramController.find(context.getEngine());
        context.eval("js", testValkyrie);
        context.eval("js", "create()");
        assertFalse(controller.getNewAssetInstances().isEmpty());
        assertTrue(context.getBindings("js").getMember("res").asBoolean());
        assertTrue(controller.didExecuteCorrectly);
        context.close();
    }

    @Test
    void testTransferAssets() {
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
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
        assertTrue(controller.didExecuteCorrectly);
        context.close();
    }

    @Test
    void transferAssetsWithInputBox() {
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
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
        assertTrue(controller.didExecuteCorrectly);
        context.close();
    }

    @Test
    void transferArbitsWithInputBox() {
        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .build();

        byte[] inputBox = {0, 1};

        ProgramController controller = ProgramController.find(context.getEngine());

        ArbitInstance arbitInstance = new ArbitInstance("a", new Long(12), inputBox);
        ArrayList<TokenInstance> arbitBoxesForUse = new ArrayList();
        arbitBoxesForUse.add(arbitInstance);
        controller.setTokenBoxesForUse(arbitBoxesForUse);

        context.eval("js", testValkyrie);
        context.eval("js", "transferArbits()");

        assertTrue(context.getBindings("js").getMember("res").asBoolean());

        assertEquals(1, controller.getBoxesToRemove().size());
        assertEquals(inputBox, controller.getBoxesToRemove().get(0));

        assertEquals(2, controller.getNewArbitInstances().size());
        assertTrue(controller.getNewArbitInstances().get(0).publicKey.equals("def"));
        assertTrue(controller.getNewArbitInstances().get(1).publicKey.equals("a"));
        assertEquals(arbitInstance.amount, controller.getNewArbitInstances().get(0).amount + controller.getNewArbitInstances().get(1).amount);
        assertTrue(controller.didExecuteCorrectly);
        context.close();
    }

    @Test
    void createWithNegativeAmountShouldFail() {
        Long amount = new Long(-10);
        Long fee = new Long(0);

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .build();

        ProgramController controller = ProgramController.find(context.getEngine());
        context.eval("js", createTestScriptWithParams(amount, fee, new Long(0) , new Long(0)));
        context.eval("js", "create()");
        assertEquals("ValkyrieError: Negative amount for asset creation", context.getBindings("js").getMember("res").asString());
        assertFalse(controller.didExecuteCorrectly);
        context.close();
    }

    @Test
    void createWithNegativeFeeShouldFail() {
        Long amount = new Long(0);
        Long fee = new Long(-10);

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .build();

        ProgramController controller = ProgramController.find(context.getEngine());
        context.eval("js", createTestScriptWithParams(amount, fee, new Long(0) , new Long(0)));
        context.eval("js", "create()");
        context.eval("js", "create()");
        assertEquals("ValkyrieError: Negative fee for asset creation", context.getBindings("js").getMember("res").asString());
        assertTrue(controller.getNewAssetInstances().isEmpty());
        assertFalse(controller.didExecuteCorrectly);
        context.close();
    }

    @Test
    void invalidTransferShouldNotMaintainCreate() {
        Long createAmount = new Long(10);
        Long createFee = new Long(0);

        Long transferAmount = new Long(20);
        Long transferFee = new Long(0);

        Context context = Context
                .newBuilder("js")
                .option("Valkyrie", "true")
                .build();

        ProgramController controller = ProgramController.find(context.getEngine());
        context.eval("js", createTestScriptWithParams(createAmount, createFee, transferAmount, transferFee));
        context.eval("js", "createAndTransferAssets()");
        assertFalse(controller.didExecuteCorrectly);
        context.close();

    }


    //Helper
    private String createTestScriptWithParams(Long createAmount, Long createFee, Long transferAmount, Long transferFee) {
        return
            "issuer = 'b';" +
            "issuer = 'bc';" +
            "function create() { " +
            "   var toAddress = 'a';" +
            "   try {"+
            "       res = Valkyrie_createAssets(issuer, toAddress, "+createAmount+", 'testAssets', "+createFee+", ''); " +
            "   } catch (err) {}" +
            "   a = 2 + 2; }" +
            "function transferAssets() {" +
            "   var fromAddress = 'a';" +
            "   var toAddress = 'def';" +
            "   try {"+
            "       res = Valkyrie_transferAssets(issuer, fromAddress, toAddress, "+transferAmount+", 'testAssets', "+transferFee+");" +
            "   } catch (err) {}}" +
            "function transferArbits() {" +
            "   var fromAddress = 'a';" +
            "   var toAddress = 'def';" +
            "   res = Valkyrie_transferArbits(fromAddress, toAddress, 10, 0);}" +
            "function createAndTransferAssets() {" +
            "   var toAddress = 'a';" +
            "   try {"+
            "       res = Valkyrie_createAssets(issuer, toAddress, "+createAmount+", 'testAssets', "+createFee+", ''); " +
            "   } catch (err) {}" +
            "   var fromAddress = 'a';" +
            "   var toAddress = 'def';" +
            "   try {"+
            "       res = Valkyrie_transferAssets(issuer, fromAddress, toAddress, "+transferAmount+", 'testAssets', "+transferFee+");" +
            "   } catch (err) {}}" +
            //To be appended to context
            "function Valkyrie_createAssets(issuer, to, amount, assetCode, fee, data) {" +
            "   res = ValkyrieReserved.createAssets(issuer, to , amount, assetCode, fee, data);" +
            "   if (res === true)" +
            "       return res; " +
            "   else throw new Error(res);};" +
            "function Valkyrie_transferAssets(issuer, from, to, amount, assetCode, fee) {" +
            "   var res = ValkyrieReserved.transferAssets(issuer, from, to , amount, assetCode, fee);" +
            "   if (res === true)" +
            "       return res; " +
            "   else throw new Error(res);};" +
            "function Valkyrie_transferArbits(from, to, amount, fee) {" +
            "   var res = ValkyrieReserved.transferArbits(from, to , amount, fee);" +
            "   if(res === true) " +
            "       return res; " +
            "   else throw new Error(res);}; ";
    }

}
