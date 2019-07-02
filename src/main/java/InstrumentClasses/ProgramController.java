package InstrumentClasses;

import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Map;


public class ProgramController implements Closeable {

    public Context context;

    //Input map of boxId to box for all boxes available for use during program execution
    //Bifrost boxes from wallet should be cast into instances of local token classes
    private static Map<byte[], AssetInstance> assetBoxesForUse;
    private static Map<byte[], ArbitInstance> arbitBoxesForUse;

    private  ArrayList<AssetInstance> newAssets;
    private static ArrayList<ArbitInstance> newArbits;

    private static ArrayList<byte[]> boxesToRemove;

    private SourceSectionFilter filter = null;

    private final Instrumenter instrumenter;

    private final TruffleInstrument.Env env;

    ProgramController(TruffleInstrument.Env env) {
        newAssets = new ArrayList<AssetInstance>();
        boxesToRemove = new ArrayList<byte[]>();
        this.env = env;
        this.instrumenter = null;
    }

    ProgramController(Instrumenter instrumenter) {
        newAssets = new ArrayList<AssetInstance>();
        boxesToRemove = new ArrayList<byte[]>();
        this.instrumenter = instrumenter;
        this.env = null;
    }

//    public ProgramController(Context context) {
//        this.context = context;
//        this.newAssets = new ArrayList<AssetInstance>();
//        this.boxesToRemove = new ArrayList<byte[]>();
//    }

    public void eval(String source) {
        context.eval("js", source);
        System.out.println("Finished eval");
    }

    static {
        Valkyrie.setFactory(new ServiceFactory<ProgramController>() {
            @Override
            public ProgramController create(TruffleInstrument.Env env) {
                return new ProgramController(env);
            }
        });
    }

    /*
    Methods to be used on scala side for initializing controller
     */

    public static synchronized void setFilter(SourceSectionFilter filter) {
        filter = filter;
    }

    public static ProgramController find(Engine engine) {
        return Valkyrie.getController(engine);
    }

    public void setArbitBoxesForUse(Map<byte[], ArbitInstance> arbitInstances) {
        this.arbitBoxesForUse = arbitInstances;
    }

    public void setAssetBoxesForUse(Map<byte[], AssetInstance> assetInstances) {
        this.assetBoxesForUse = assetInstances;
    }

    public  ArrayList<AssetInstance> getNewAssetInstances() {
        System.out.println("Entered getNewAssets in PC");
        return newAssets;
    }

    public void close() {

    }



    /*
    Methods to be used by instrument for updating values for caught Valkyrie functions
    */
    protected  void createAssets(String issuer, String to, Long amount, String assetCode, Long fee, String data) {
        System.out.println("Entered createAssets in PC");
        newAssets.add(new AssetInstance(to, issuer, assetCode, amount, data));
        System.out.println(newAssets.size());
    }


    /*
    Classes to represent token boxes
     */

    public static class TokenInstance {
        String instanceType;
        String publicKey;
        Long amount;

        public String getInstanceType() {
            return instanceType;
        }
    }

    public static final class AssetInstance extends TokenInstance{
        String issuer;
        String assetCode;
        String data;

        AssetInstance(String publicKey, String issuer, String assetCode, Long amount, String data) {
            this.publicKey = publicKey;
            this.amount = amount;
            this.assetCode = assetCode;
            this.issuer = issuer;
            this.data = data;
            this.instanceType = "Asset";
        }

        public String getInstanceType() {
            return super.getInstanceType();
        }
    }

    public static final class ArbitInstance extends TokenInstance{

        ArbitInstance(String publicKey, String issuer, String assetCode, Long amount, String data) {
            this.publicKey = publicKey;
            this.amount = amount;
            this.instanceType = "Arbit";
        }

        public String getInstanceType() {
            return super.getInstanceType();
        }
    }
}
