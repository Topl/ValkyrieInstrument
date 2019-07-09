package InstrumentClasses.TokenClasses;

import InstrumentClasses.ProgramController;

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

    public static void validateWithBoxId(PolyInstance instance) {
        if(instance.boxId == null) {
            throw new IllegalArgumentException("Provided poly box does not have boxId");
        }
        else if(instance.amount < 0) {
            throw new IllegalArgumentException("Provided poly box has negative amount");
        }
        else if(ProgramController.base58Decode(instance.publicKey).length != ProgramController.keyLength) {
            throw new IllegalArgumentException("Provided poly box has an invalid public key");
        }
    }
}
