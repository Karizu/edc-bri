package id.co.bri.brizzi.common;

/**
 * Created by indra on 23/01/16.
 */
public enum BrizziCiStatus {
    RC(2),ActivationCode(6),Status(4),Interoperability(54);

    private BrizziCiStatus(int code){
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }
}
