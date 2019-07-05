package InstrumentClasses;

import InstrumentClasses.TokenClasses.ArbitInstance;
import InstrumentClasses.TokenClasses.AssetInstance;
import InstrumentClasses.TokenClasses.TokenInstance;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

import java.io.Closeable;
import java.util.ArrayList;


public class ProgramController implements Closeable {

    public Context context;

    //Input map of boxId to box for all boxes available for use during program execution
    //Bifrost boxes from wallet should be cast into instances of local token classes
    private ArrayList<AssetInstance> assetBoxesForUse;
    private ArrayList<ArbitInstance> arbitBoxesForUse;

    private Long feesCollected;

    private  ArrayList<AssetInstance> newAssets;
    private ArrayList<ArbitInstance> newArbits;

    private ArrayList<byte[]> boxesToRemove;

//    private SourceSectionFilter filter = null;
//
//    private final Instrumenter instrumenter;

    private final TruffleInstrument.Env env;

    ProgramController(TruffleInstrument.Env env) {
        newAssets = new ArrayList<>();
        newArbits = new ArrayList<>();
        boxesToRemove = new ArrayList<>();
        assetBoxesForUse = new ArrayList<>();
        arbitBoxesForUse = new ArrayList<>();
        feesCollected = new Long(0);
        this.env = env;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void evalJS(String source) {
        context.eval("js", source);
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

//    public static synchronized void setFilter(SourceSectionFilter filter) {
//        filter = filter;
//    }

    public static ProgramController find(Engine engine) {
        return Valkyrie.getController(engine);
    }

    public void setArbitBoxesForUse(ArrayList<ArbitInstance> arbitInstances) {
        for(ArbitInstance instance: arbitInstances) {
            assert(instance.boxId != null);
        }
        this.arbitBoxesForUse = arbitInstances;
    }

    public void setAssetBoxesForUse(ArrayList<AssetInstance> assetInstances) {
        for(AssetInstance instance: assetInstances) {
            assert(instance.boxId != null);
        }
        this.assetBoxesForUse = assetInstances;
    }

    public void setTokenBoxesForUse(ArrayList<TokenInstance> tokenInstances) {
        for(TokenInstance instance: tokenInstances) {
            assert(instance.boxId != null);
            if(instance.instanceType.equals("Asset")) {
                this.assetBoxesForUse.add((AssetInstance)instance);
            }
            else if(instance.instanceType.equals("Arbit")) {
                this.arbitBoxesForUse.add((ArbitInstance)instance);
            }
        }
    }

    public ArrayList<AssetInstance> getNewAssetInstances() {
        return newAssets;
    }

    public ArrayList<ArbitInstance> getNewArbitInstances() {
        return newArbits;
    }

    public ArrayList<byte[]> getBoxesToRemove() {
        return boxesToRemove;
    }

    //TODO implement
    public Long getFeesCollected() {
        return feesCollected;
    }

    public void clear() {
        assetBoxesForUse.clear();
        arbitBoxesForUse.clear();
        boxesToRemove.clear();
        newArbits.clear();
        newAssets.clear();
        feesCollected = new Long(0);
    }

    public void close() {
    }



    /*
    Methods to be used by instrument for updating values for caught Valkyrie functions
    */
    //TODO collect fees otherwise value is being bled out
    protected void createAssets(String issuer, String to, Long amount, String assetCode, Long fee, String data) {
        newAssets.add(new AssetInstance(to, issuer, assetCode, amount - fee, data));
        //TODO fees
//        feesCollected += fee;
    }

    protected void transferAssets(String issuer, String from, String to, Long amount, String assetCode, Long fee) {
        //TODO Look into writing rollback function for greater efficiency in preventing partial state updates
        assert(checkEnoughAssetsAvailableForTransfer(from, amount, fee, assetCode, issuer)): "Not enough funds available for asset transfer";
        //Transferring assets from newly created assets first, until total transfer amount is reach
        Long amountCollected = new Long(0);
        Long change = new Long(0);
        for(AssetInstance instance: newAssets) {
            if(instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                newAssets.remove(instance);
                amountCollected += instance.amount;
                if(amountCollected <= amount) {
                    newAssets.add(new AssetInstance(to, instance.issuer, instance.assetCode, instance.amount , instance.data));
                }
                else {
                    change = new Long(amountCollected - amount);
                    newAssets.add(new AssetInstance(to, instance.issuer, instance.assetCode, instance.amount - change, instance.data));
                    newAssets.add(new AssetInstance(from, instance.issuer, instance.assetCode, change, instance.data));
                    break;
                }
            }
            //If assetBox is not what we're looking for, do nothing
        }

        //If total transfer amount not reached from newly created assets, use boxes provided as arguments to controller to fund transfer

        if(amountCollected < amount) {
            for(AssetInstance instance: assetBoxesForUse) {
                if (instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                    assetBoxesForUse.remove(instance);
                    boxesToRemove.add(instance.boxId);
                    amountCollected += instance.amount;
                    if(amountCollected <= amount) {
                        newAssets.add(new AssetInstance(to, instance.issuer, instance.assetCode, instance.amount , instance.data));
                    }
                    else {
                        change = new Long(amountCollected - amount);
                        newAssets.add(new AssetInstance(to, instance.issuer, instance.assetCode, instance.amount - change, instance.data));
                        newAssets.add(new AssetInstance(from, instance.issuer, instance.assetCode, change, instance.data));
                        break;
                    }
                }
            }
        }
        //TODO Fee
//        feesCollected += fee;
        assert(amountCollected == amount + change);

    }

    protected void transferArbits(String from, String to, Long amount, Long fee) {
        //TODO Look into writing rollback function for greater efficiency in preventing partial state updates
        assert(checkEnoughArbitsAvailableForTransfer(from, amount, fee)): "Not enough funds available for arbit transfer";
        //Transferring arbits from newly created arbits first, until total transfer amount is reach
        Long amountCollected = new Long(0);
        Long change = new Long(0);
        for(ArbitInstance instance: newArbits) {
            if(instance.publicKey.equals(from)) {
                newArbits.remove(instance);
                amountCollected += instance.amount;
                if(amountCollected <= amount) {
                    newArbits.add(new ArbitInstance(to, instance.amount));
                }
                else {
                    change = new Long(amountCollected - amount);
                    newArbits.add(new ArbitInstance(to, instance.amount - change));
                    newArbits.add(new ArbitInstance(from, change));
                    break;
                }
            }
            //If arbitBox is not what we're looking for, do nothing
        }

        //If total transfer amount not reached from newly created arbits, use boxes provided as arguments to controller to fund transfer

        if(amountCollected < amount) {
            for(ArbitInstance instance: arbitBoxesForUse) {
                if (instance.publicKey.equals(from)) {
                    arbitBoxesForUse.remove(instance);
                    boxesToRemove.add(instance.boxId);
                    amountCollected += instance.amount;
                    if(amountCollected <= amount) {
                        newArbits.add(new ArbitInstance(to, instance.amount));
                    }
                    else {
                        change = new Long(amountCollected - amount);
                        newArbits.add(new ArbitInstance(to,instance.amount - change));
                        newArbits.add(new ArbitInstance(from, change));
                        break;
                    }
                }
            }
        }
        //TODO Fee
//        feesCollected += fee;
        assert(amountCollected == amount + change);

    }


    //Helper
    private boolean checkEnoughAssetsAvailableForTransfer(String from, Long amount, Long fee, String assetCode, String issuer) {
        Long availableAmount = new Long(0);
        for(AssetInstance instance: newAssets) {
            if (instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                availableAmount += instance.amount;
                if(availableAmount >= amount) {
                    return true;
                }
            }
        }

        for(AssetInstance instance: assetBoxesForUse) {
            if (instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                availableAmount += instance.amount;
                if(availableAmount >= amount) {
                    return true;
                }
            }
        }

        return false;
    }

    //Helper
    private boolean checkEnoughArbitsAvailableForTransfer(String from, Long amount, Long fee) {
        Long availableAmount = new Long(0);
        for(ArbitInstance instance: newArbits) {
            if (instance.publicKey.equals(from)) {
                availableAmount += instance.amount;
                if(availableAmount >= amount) {
                    return true;
                }
            }
        }

        for(ArbitInstance instance: arbitBoxesForUse) {
            if (instance.publicKey.equals(from)) {
                availableAmount += instance.amount;
                if(availableAmount >= amount) {
                    return true;
                }
            }
        }

        return false;
    }
}
