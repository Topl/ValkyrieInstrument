package TokenBox;

import Utils.Base58;
import Instrument.ProgramController;

public class Arbit extends Token {

    public Arbit(String publicKey, Long amount) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Arbit";
    }

    public Arbit(String publicKey, Long amount, byte[] boxId) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Arbit";
        this.boxId = boxId;
    }

    public String getInstanceType() {
        return super.getInstanceType();
    }

    public static void validateWithBoxId(Arbit instance) {
        if(instance.boxId == null) {
            throw new IllegalArgumentException("Provided arbit box does not have boxId");
        }
        else if(instance.amount < 0) {
            throw new IllegalArgumentException("Provided arbit box has negative amount");
        }
        else if(Base58.decode(instance.publicKey).length != ProgramController.keyLength) {
            throw new IllegalArgumentException("Provided arbit box has an invalid public key");
        }
    }
}
