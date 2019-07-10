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

    String publicKey1 = "6sYyiTguyQ455w2dGEaNbrwkAWAEYV1Zk6FtZMknWDKQ";
    String publicKey2 = "A9vRt6hw7w4c7b4qEkQHYptpqBGpKM5MGoXyrkGCbrfb";
    String publicKey3 = "F6ABtYMsJABDLH2aj7XVPwQr5mH7ycsCE4QGQrLeB3xU";
    //Helper
    private String createTestScriptWithParams(Long createAmount, Long createFee, Long transferAmount, Long transferFee) {
        return String.format(
            "issuer = '%1$s';" +
            "function create() { " +
            "   var toAddress = '%2$s';" +
            "   try {"+
            "       var res = Valkyrie_createAssets(issuer, toAddress, "+createAmount+", 'testAssets', "+createFee+", ''); " +
            "   } catch (err) {}" +
            "   a = 2 + 2; }" +
            "function transferAssets() {" +
            "   var fromAddress = '%2$s';" +
            "   var toAddress = '%3$s';" +
            //"   try {"+
            "       var res = Valkyrie_transferAssets(issuer, fromAddress, toAddress, "+transferAmount+", 'testAssets', "+transferFee+");}" +
            //"   } catch (err) {}}" +
            "function transferArbits() {" +
            "   var fromAddress = '%2$s';" +
            "   var toAddress = '%3$s';" +
            "   var res = Valkyrie_transferArbits(fromAddress, toAddress, 10, 0);}" +
            "function createAndTransferAssets() {" +
            "   var toAddress1 = '%2$s';" +
            "   try {"+
            "       var res1 = Valkyrie_createAssets(issuer, toAddress1, "+createAmount+", 'testAssets', "+createFee+", ''); " +
            "   } catch (err) {}" +
            "   var fromAddress = '%2$s';" +
            "   var toAddress2 = '%3$s';" +
            //"   try {"+
            "       var res2 = Valkyrie_transferAssets(issuer, fromAddress, toAddress2, "+transferAmount+", 'testAssets', "+transferFee+");}" +
            //"   } catch (err) {}}" +
            //To be appended to context
            "function Valkyrie_createAssets(issuer, to, amount, assetCode, fee, data) {" +
            "   var res = ValkyrieReserved.createAssets(issuer, to , amount, assetCode, fee, data);" +
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
            "   else throw new Error(res);}; ", publicKey1, publicKey2, publicKey3);
    }

    private String testValkyrieScript = createTestScriptWithParams(new Long(10), new Long(0), new Long(10), new Long(0));

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
        context.eval("js", testValkyrieScript);
        context.eval("js", "create()");
        assertFalse(controller.getNewAssetInstances().isEmpty());
//        assertTrue(context.getBindings("js").getMember("res").asBoolean());
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
        context.eval("js", testValkyrieScript);
        context.eval("js", "createAndTransferAssets()");
//        context.eval("js", "transferAssets()");

        assertFalse(controller.getNewAssetInstances().isEmpty());
//        assertTrue(context.getBindings("js").getMember("res").asBoolean());

        assertEquals(1, controller.getNewAssetInstances().size());
        assertTrue(controller.getNewAssetInstances().get(0).publicKey.equals(publicKey3));
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

        AssetInstance assetInstance = new AssetInstance(publicKey2, publicKey1, "testAssets", new Long(12), "", inputBox);
        ArrayList<AssetInstance> assetBoxesForUse = new ArrayList();
        assetBoxesForUse.add(assetInstance);
        controller.setAssetBoxesForUse(assetBoxesForUse);

        context.eval("js", testValkyrieScript);
        context.eval("js", "transferAssets()");

//        assertTrue(context.getBindings("js").getMember("res").asBoolean());

        assertEquals(1, controller.getBoxesToRemove().size());
        assertEquals(inputBox, controller.getBoxesToRemove().get(0));

        assertEquals(2, controller.getNewAssetInstances().size());
        assertTrue(controller.getNewAssetInstances().get(0).publicKey.equals(publicKey3));
        assertTrue(controller.getNewAssetInstances().get(1).publicKey.equals(publicKey2));
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

        ArbitInstance arbitInstance = new ArbitInstance(publicKey2, new Long(12), inputBox);
        ArrayList<TokenInstance> arbitBoxesForUse = new ArrayList();
        arbitBoxesForUse.add(arbitInstance);
        controller.setTokenBoxesForUse(arbitBoxesForUse);

//        ArrayList<ArbitInstance> arbitBoxesForUse = new ArrayList();
//        arbitBoxesForUse.add(arbitInstance);
//        controller.setArbitBoxesForUse(arbitBoxesForUse);

        context.eval("js", testValkyrieScript);
        context.eval("js", "transferArbits()");

//        assertTrue(context.getBindings("js").getMember("res").asBoolean());

        assertEquals(1, controller.getBoxesToRemove().size());
        assertEquals(inputBox, controller.getBoxesToRemove().get(0));

        assertEquals(2, controller.getNewArbitInstances().size());
        assertTrue(controller.getNewArbitInstances().get(0).publicKey.equals(publicKey3));
        assertTrue(controller.getNewArbitInstances().get(1).publicKey.equals(publicKey2));
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
        assertTrue(controller.getNewAssetInstances().isEmpty());
        assertFalse(controller.didExecuteCorrectly);
        context.close();
    }

    //Should wrap context evals in try blocks for when developers dont handle Valkyrie exceptions
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
        try {
            context.eval("js", "createAndTransferAssets()");
        }
        catch (Exception e) {
        }
        finally {
            assertFalse(controller.didExecuteCorrectly);
            context.close();
        }
    }

}
