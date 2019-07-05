package InstrumentClasses.TokenClasses;

public class ArbitInstance extends TokenInstance{

    public ArbitInstance(String publicKey, Long amount) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Arbit";
    }

    public ArbitInstance(String publicKey, Long amount, byte[] boxId) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Arbit";
        this.boxId = boxId;
    }

    public String getInstanceType() {
        return super.getInstanceType();
    }
}
