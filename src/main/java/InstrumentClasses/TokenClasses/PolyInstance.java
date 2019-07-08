package InstrumentClasses.TokenClasses;

public class PolyInstance extends TokenInstance{

    public PolyInstance(String publicKey, Long amount) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Poly";
    }

    public PolyInstance(String publicKey, Long amount, byte[] boxId) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Poly";
        this.boxId = boxId;
    }

    public String getInstanceType() {
        return super.getInstanceType();
    }
}
