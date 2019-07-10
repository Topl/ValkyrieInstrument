package InstrumentClasses.TokenClasses;
import InstrumentClasses.Base58;
import InstrumentClasses.ProgramController;

public class AssetInstance extends TokenInstance{
    public String issuer;
    public String assetCode;
    public String data;

    public AssetInstance(String publicKey, String issuer, String assetCode, Long amount, String data) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.assetCode = assetCode;
        this.issuer = issuer;
        this.data = data;
        this.instanceType = "Asset";
    }

    public AssetInstance(String publicKey, String issuer, String assetCode, Long amount, String data, byte[] boxId) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.assetCode = assetCode;
        this.issuer = issuer;
        this.data = data;
        this.instanceType = "Asset";
        this.boxId = boxId;
    }


    public String getInstanceType() {
        return super.getInstanceType();
    }

    public static void validateWithBoxId(AssetInstance instance) {
        if(instance.boxId == null) {
            throw new IllegalArgumentException("Provided asset box does not have boxId");
        }
        else if(instance.amount < 0) {
            throw new IllegalArgumentException("Provided asset box has negative amount");
        }
        else if(Base58.decode(instance.issuer).length != ProgramController.keyLength || Base58.decode(instance.publicKey).length != ProgramController.keyLength) {
            throw new IllegalArgumentException("Provided asset box has an invalid public key");
        }
    }
}
