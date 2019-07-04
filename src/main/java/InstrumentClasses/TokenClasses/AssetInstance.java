package InstrumentClasses.TokenClasses;

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
}
