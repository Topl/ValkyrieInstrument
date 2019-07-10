package InstrumentClasses.TokenClasses;

import InstrumentClasses.ProgramController;

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

    public static void validateWithBoxId(ArbitInstance instance) {
        if(instance.boxId == null) {
            throw new IllegalArgumentException("Provided arbit box does not have boxId");
        }
        else if(instance.amount < 0) {
            throw new IllegalArgumentException("Provided arbit box has negative amount");
        }
//        else if(ProgramController.base58Decode(instance.publicKey).length != ProgramController.keyLength) {
//            throw new IllegalArgumentException("Provided arbit box has an invalid public key");
//        }
        else if(ProgramController.base58Decode(instance.publicKey).length != ProgramController.keyLength) {
            throw new IllegalArgumentException("Provided arbit box has an invalid public key");
        }
    }
}
