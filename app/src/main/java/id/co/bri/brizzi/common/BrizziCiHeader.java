package id.co.bri.brizzi.common;

/**
 * Created by indra on 23/01/16.
 */
public enum BrizziCiHeader {
    RC(2),BRI(6),CardNumber(16),IssueDate(6),ExpireDate(6),BranchIssue(4),CardType(4),ModelCard(4);

    private BrizziCiHeader(int code){
        this.code = code;
    }
    private int code;

    public int getCode() {
        return code;
    }
}
