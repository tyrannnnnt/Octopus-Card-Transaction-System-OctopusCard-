package comp2026.OctopusCard;
import comp2026.OctopusCard.Util.Tokenizer;


import java.text.ParseException;
import java.util.Date;

public class TopUp extends OCTransaction {
    private String topUpType;
    private String agent;
    public static final String TypeHdrStr = "TopUp";

    public TopUp(String type, Date date, String transactionID, double amount, String topUpType, String agent)throws OCTransactionFormatException{
        super(type, date, transactionID, amount);
        setTopAAgent(topUpType, agent);
    }

    public TopUp(String type, String dateTimeStr, String transactionID, String amountStr, String topUpType, String agent)throws OCTransactionFormatException{
        super(type, dateTimeStr, transactionID, amountStr);
        setTopAAgent(topUpType, agent);
    }

    private void setTopAAgent( String topUpType, String agent){
        this.topUpType = topUpType;
        this.agent = agent;
        this.setStatus(Status.COMPLETED);
    }

    @Override
    public String toRecord(){
        return super.toRecord() + " " + this.topUpType + " " + this.agent;
    }

    @Override
    public boolean match(String[] criteria) throws ParseException {

        switch (criteria[0]) {
            case "Cash":
                return this.topUpType.equals("Cash");
            case "Bank":
                if (criteria.length == 1) {
                    return this.topUpType.equals("Bank");
                }
                if (criteria.length == 2) {
                    return this.topUpType.equals("Bank") && this.agent.contains(criteria[1]);
                }
                break;
            case "date":
                return matchDate(criteria[1]);
            default:
                System.out.println("Wrong type of input.");
                return false;
        }

        return false;
    }

    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String [] tokens = Tokenizer.getTokens(record);
        // chk for blank line (no tokens)
        if (tokens.length == 0) {
            return null;
        }
        String agentStr = tokens[5];

        for(int i = 6; i < tokens.length; i++){
            agentStr += " " + tokens[i];
        }

        return new TopUp(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], agentStr);
    }

    @Override
    public String toString() {
        return "[Top Up]" +
                "\nAgent: " + this.agent +
                "\nType: " + this.topUpType +
                "\n" +
                super.toString();
    }
}
