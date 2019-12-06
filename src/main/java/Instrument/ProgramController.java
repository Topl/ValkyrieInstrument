package Instrument;

import TokenBox.Arbit;
import TokenBox.Asset;
import TokenBox.Poly;
import TokenBox.Token;
import Utils.Base58;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import java.io.Closeable;
import java.util.ArrayList;


public class ProgramController implements Closeable {

    public boolean didExecuteCorrectly;

    public Context context;

    //Bifrost boxes from wallet should be cast into instances of local token classes
    private ArrayList<Asset> assetBoxesForUse;
    private ArrayList<Arbit> arbitBoxesForUse;
    private ArrayList<Poly> polyBoxesForUse;

    private Long feesCollected;

    private  ArrayList<Asset> newAssets;
    private ArrayList<Arbit> newArbits;

    private ArrayList<byte[]> boxesToRemove;

    private final TruffleInstrument.Env env;

    ProgramController(TruffleInstrument.Env env) {
        assetBoxesForUse = new ArrayList<>();
        arbitBoxesForUse = new ArrayList<>();
        polyBoxesForUse = new ArrayList<>();
        newAssets = new ArrayList<>();
        newArbits = new ArrayList<>();
        boxesToRemove = new ArrayList<>();
        feesCollected = new Long(0);
        didExecuteCorrectly = true;
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
    Methods to be used on scala side for initializing controller and getting results of execution
     */

    public static ProgramController find(Engine engine) {
        return Valkyrie.getController(engine);
    }

    public void setArbitBoxesForUse(ArrayList<Arbit> arbits) {
        for(Arbit instance: arbits) {
            Arbit.validateWithBoxId(instance);
        }
        this.arbitBoxesForUse = (ArrayList<Arbit>) arbits.clone();
    }

    public void setAssetBoxesForUse(ArrayList<Asset> assets) {
        for(Asset instance: assets) {
            Asset.validateWithBoxId(instance);
        }
        this.assetBoxesForUse = (ArrayList<Asset>) assets.clone();
    }

    public void setPolyBoxesForUse(ArrayList<Poly> polys) {
        for(Poly instance: polys) {
            Poly.validateWithBoxId(instance);
        }
        this.polyBoxesForUse = (ArrayList<Poly>) polys.clone();
    }

    public void setTokenBoxesForUse(ArrayList<Token> tokens) {
        for(Token instance: tokens) {
            if(instance.instanceType.equals("Asset")) {
                Asset.validateWithBoxId((Asset)instance);
                this.assetBoxesForUse.add((Asset)instance);
            }
            else if(instance.instanceType.equals("Arbit")) {
                Arbit.validateWithBoxId((Arbit)instance);
                this.arbitBoxesForUse.add((Arbit)instance);
            }
            else if(instance.instanceType.equals("Poly")) {
                Poly.validateWithBoxId((Poly)instance);
                this.polyBoxesForUse.add((Poly)instance);
            }
        }
    }

    public ArrayList<Asset> getNewAssetInstances() {
        return newAssets;
    }

    public ArrayList<Arbit> getNewArbitInstances() {
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
        polyBoxesForUse.clear();
        boxesToRemove.clear();
        newArbits.clear();
        newAssets.clear();
        feesCollected = new Long(0);
    }

    public void close() {
    }



    /*
    Methods to be used by instrument to update values for encountered Valkyrie functions
    */
    protected void createAssets(String issuer, String to, Long amount, String assetCode, Long fee, String data) {
        //TODO check enough fees
        if(amount < 0) {
            throw new IllegalArgumentException("Negative amount for asset creation");
        }
        if(fee < 0) {
            throw new IllegalArgumentException("Negative fee for asset creation");
        }
        if(Base58.decode(issuer).length != keyLength || Base58.decode(to).length != keyLength) {
            throw new IllegalArgumentException("Invalid public key provided in asset creation");
        }
        newAssets.add(new Asset(to, issuer, assetCode, amount - fee, data));
        //TODO fee collection
//        feesCollected += fee;
    }

    protected void transferAssets(String issuer, String from, String to, Long amount, String assetCode, Long fee) {
        //TODO check enough fees
        //TODO Look into writing rollback function for greater efficiency in preventing partial state updates
        if(amount < 0) {
            throw new IllegalArgumentException("Negative amount for asset transfer");
        }
        if(fee < 0) {
            throw new IllegalArgumentException("Negative fee for asset transfer");
        }
        if(Base58.decode(issuer).length != keyLength || Base58.decode(from).length != keyLength || Base58.decode(to).length != keyLength) {
            throw new IllegalArgumentException("Invalid public key provided in asset transfer");
        }
        if(!checkEnoughAssetsAvailableForTransfer(from, amount, fee, assetCode, issuer)){
            throw new IllegalStateException("Not enough funds available for asset transfer");
        }
        //Transferring assets from newly created assets first, until total transfer amount is reach
        Long amountCollected = new Long(0);
        Long change = new Long(0);
        for(Asset instance: newAssets) {
            if(instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                newAssets.remove(instance);
                amountCollected += instance.amount;
                if(amountCollected <= amount) {
                    newAssets.add(new Asset(to, instance.issuer, instance.assetCode, instance.amount , instance.data));
                }
                else {
                    change = new Long(amountCollected - amount);
                    newAssets.add(new Asset(to, instance.issuer, instance.assetCode, instance.amount - change, instance.data));
                    newAssets.add(new Asset(from, instance.issuer, instance.assetCode, change, instance.data));
                    break;
                }
            }
            //If assetBox is not what we're looking for, do nothing
        }
        //If total transfer amount not reached from newly created assets, use boxes provided as arguments to controller to fund transfer
        if(amountCollected < amount && assetBoxesForUse != null) {
            for(Asset instance: assetBoxesForUse) {
                if (instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                    assetBoxesForUse.remove(instance);
                    boxesToRemove.add(instance.boxId);
                    amountCollected += instance.amount;
                    if(amountCollected <= amount) {
                        newAssets.add(new Asset(to, instance.issuer, instance.assetCode, instance.amount , instance.data));
                    }
                    else {
                        change = new Long(amountCollected - amount);
                        newAssets.add(new Asset(to, instance.issuer, instance.assetCode, instance.amount - change, instance.data));
                        newAssets.add(new Asset(from, instance.issuer, instance.assetCode, change, instance.data));
                        break;
                    }
                }
            }
        }
        //TODO fee collection
//        feesCollected += fee;
        if(amountCollected != amount + change) {
            didExecuteCorrectly = false;
            throw new ArithmeticException("Boxes do not sum to correct values");
        }

    }

    protected void transferArbits(String from, String to, Long amount, Long fee) {
        //TODO check enough fees
        //TODO Look into writing rollback function for greater efficiency in preventing partial state updates
        if(amount < 0) {
            throw new IllegalArgumentException("Negative amount for arbit transfer");
        }
        if(fee < 0) {
            throw new IllegalArgumentException("Negative fee for arbit transfer");
        }
        if(Base58.decode(to).length != keyLength || Base58.decode(from).length != keyLength) {
            throw new IllegalArgumentException("Invalid public key provided in arbit transfer");
        }
        if(!checkEnoughArbitsAvailableForTransfer(from, amount, fee)){
            throw new IllegalStateException("Not enough funds available for arbit transfer");
        }
        //Transferring arbits from newly created arbits first, until total transfer amount is reach
        Long amountCollected = new Long(0);
        Long change = new Long(0);
        for(Arbit instance: newArbits) {
            if(instance.publicKey.equals(from)) {
                newArbits.remove(instance);
                amountCollected += instance.amount;
                if(amountCollected <= amount) {
                    newArbits.add(new Arbit(to, instance.amount));
                }
                else {
                    change = new Long(amountCollected - amount);
                    newArbits.add(new Arbit(to, instance.amount - change));
                    newArbits.add(new Arbit(from, change));
                    break;
                }
            }
            //If arbitBox is not what we're looking for, do nothing
        }
        //If total transfer amount not reached from newly created arbits, use boxes provided as arguments to controller to fund transfer
        if(amountCollected < amount && arbitBoxesForUse != null) {
            for(Arbit instance: arbitBoxesForUse) {
                if (instance.publicKey.equals(from)) {
                    arbitBoxesForUse.remove(instance);
                    boxesToRemove.add(instance.boxId);
                    amountCollected += instance.amount;
                    if(amountCollected <= amount) {
                        newArbits.add(new Arbit(to, instance.amount));
                    }
                    else {
                        change = new Long(amountCollected - amount);
                        newArbits.add(new Arbit(to,instance.amount - change));
                        newArbits.add(new Arbit(from, change));
                        break;
                    }
                }
            }
        }
        //TODO fee collection
//        feesCollected += fee;
        if(amountCollected != amount + change) {
            didExecuteCorrectly = false;
            throw new ArithmeticException("Boxes do not sum to correct values");
        }
    }


    //Helper
    private boolean checkEnoughAssetsAvailableForTransfer(String from, Long amount, Long fee, String assetCode, String issuer) {
        Long availableAmount = new Long(0);
        for(Asset instance: newAssets) {
            if (instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                availableAmount += instance.amount;
                if(availableAmount >= amount) {
                    return true;
                }
            }
        }
        if(assetBoxesForUse != null) {
            for (Asset instance : assetBoxesForUse) {
                if (instance.issuer.equals(issuer) && instance.assetCode.equals(assetCode) && instance.publicKey.equals(from)) {
                    availableAmount += instance.amount;
                    if (availableAmount >= amount) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Helper
    private boolean checkEnoughArbitsAvailableForTransfer(String from, Long amount, Long fee) {
        Long availableAmount = new Long(0);
        for(Arbit instance: newArbits) {
            if (instance.publicKey.equals(from)) {
                availableAmount += instance.amount;
                if(availableAmount >= amount) {
                    return true;
                }
            }
        }
        if(arbitBoxesForUse != null) {
            for (Arbit instance : arbitBoxesForUse) {
                if (instance.publicKey.equals(from)) {
                    availableAmount += instance.amount;
                    if (availableAmount >= amount) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * Base 58 decoding utility method - taken from Base58codec: https://github.com/chrylis/base58-codec
     */

    public static final int keyLength = 32;

//    public static final BigInteger BASE = BigInteger.valueOf(58);
//
//    public static final char ALPHABET[] = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
//
//    public static byte[] base58Decode(final String source) {
//        BigInteger value = BigInteger.ZERO;
//
//        Iterator<Character> it = stringIterator(source);
//        while (it.hasNext()) {
//            value = value.add(BigInteger.valueOf(Arrays.asList(ALPHABET).indexOf(it.next())));
//            if (it.hasNext())
//                value = value.multiply(BASE);
//        }
//        return value.toByteArray();
//    }
//
//    public static Iterator<Character> stringIterator(final String string) {
//        // Ensure the error is found as soon as possible.
//        if (string == null)
//            throw new NullPointerException();
//
//        return new Iterator<Character>() {
//            private int index = 0;
//
//            public boolean hasNext() {
//                return index < string.length();
//            }
//
//            public Character next() {
//                /*
//                 * Throw NoSuchElementException as defined by the Iterator contract, not IndexOutOfBoundsException.
//                 */
//                if (!hasNext())
//                    throw new NoSuchElementException();
//                return string.charAt(index++);
//            }
//
//            public void remove() {
//                throw new UnsupportedOperationException();
//            }
//        };
//    }
}
