package TokenBox;

import Utils.Base58;
import Instrument.ProgramController;

public class Poly extends Token {

    public Poly(String publicKey, Long amount) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Poly";
    }

    public Poly(String publicKey, Long amount, byte[] boxId) {
        this.publicKey = publicKey;
        this.amount = amount;
        this.instanceType = "Poly";
        this.boxId = boxId;
    }

    public String getInstanceType() {
        return super.getInstanceType();
    }

    public static void validateWithBoxId(Poly instance) {
        if(instance.boxId == null) {
            throw new IllegalArgumentException("Provided poly box does not have boxId");
        }
        else if(instance.amount < 0) {
            throw new IllegalArgumentException("Provided poly box has negative amount");
        }
        else if(Base58.decode(instance.publicKey).length != ProgramController.keyLength) {
            throw new IllegalArgumentException("Provided poly box has an invalid public key");
        }
    }
}
