package InstrumentClasses.TokenClasses;

public class ArbitInstance extends TokenInstance{

    ArbitInstance(String publicKey, String issuer, String assetCode, Long amount, String data) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Arbit";
    }

    ArbitInstance(String publicKey, String issuer, String assetCode, Long amount, String data, byte[] boxId) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Arbit";
        this.boxId = boxId;
    }

    public String getInstanceType() {
        return super.getInstanceType();
    }
}
