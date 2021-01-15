package comp2026.OctopusCard;
import comp2026.OctopusCard.Util.Tokenizer;


import java.text.ParseException;
import java.util.Date;

public class Retail extends OCTransaction {
    private String retailer;
    private String description;
    public static final String TypeHdrStr = "Retail";

    public Retail(String type, Date date, String transactionID, double amount, String retailer, String description)throws OCTransactionFormatException{
        super(type, date, transactionID, amount);
        setRetailerADescription(retailer, description);
    }

    public Retail(String type, String dateTimeStr, String transactionID, String amountStr, String retailer, String description)throws OCTransactionFormatException{
        super(type, dateTimeStr, transactionID, amountStr);
        setRetailerADescription(retailer, description);
    }

    private void setRetailerADescription(String retailer, String description){
        this.retailer = retailer;
        this.description = description;
        this.setStatus(Status.COMPLETED);
    }

    @Override
    public String toRecord() {
        return super.toRecord() + " " + this.retailer + ", " + this.description;
    }

    @Override
    public boolean match(String[] criteria) throws ParseException {
        switch (criteria[0]) {
            case "retailer":
                return this.retailer.contains(criteria[1]);
            case "description":
                return this.description.contains(criteria[1]);
            case "date":
                return matchDate(criteria[1]);
            default:
                System.out.println("Wrong input format.");
                return false;
        }
    }

    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String [] tokens = Tokenizer.getTokens(record);

        if (tokens.length < 6){
            throw new OCTransactionFormatException("RetailTransactionFormatException: parseRetailTransaction: too few tokens.");
        }

//        // chk for blank line (no tokens)
//        if (tokens.length == 0) {
//            return null;
//        }

        String retailerStr = tokens[4];
        int commaPos = 5;
        for (int i = 5; i < tokens.length; i++) {
            if (!tokens[i].contains(","))
                retailerStr += " " + tokens[i];
            else {
                retailerStr += " " + tokens[i].replace(",", "");
                commaPos = i;
                break;
            }
        }
        String descriptionStr = tokens[commaPos + 1];
        for (int i = commaPos + 2; i < tokens.length; i++) {
            descriptionStr += " " + tokens[i];
        }
        return new Retail(tokens[0], tokens[1], tokens[2], tokens[3], retailerStr, descriptionStr);
    }

    @Override
    public String toString() {
        return "[Retail]" +
                "\nRetailer: "+ this.retailer +
                "\nDescription: " + this.description +
                "\n" +
                super.toString();
    }
}
