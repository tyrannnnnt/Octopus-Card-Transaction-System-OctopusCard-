package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.DateTimeUtil;
import comp2026.OctopusCard.Util.Tokenizer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OCTransaction {
    public enum Status{
        COMPLETED,
        MTR_OUTSTANDING,
        MTR_COMPLETED
    }

    private final Date date;
    private final String transactionID;
    private final double amount;
    private final String type;
    private Status status;              // fixme: add the enumeration type, "Status"!!


    //============================================================
    // Constructors


    public OCTransaction(String type, Date date, String transactionID, double amount) throws OCTransactionFormatException {
        this.type = type;
        this.date = date;
        this.transactionID = transactionID;
        this.amount = amount;

        if(type.equalsIgnoreCase("MTR")) {
            status = Status.MTR_OUTSTANDING;
        } else {
            status = Status.COMPLETED;
        }

        // chk type
        if (!typeIsValid(this.type)) {
            throw new OCTransactionFormatException("Invalid transaction type: " + this.type);
        }
    }

    public OCTransaction(String type, String dateTimeStr, String transactionID, String amountStr) throws OCTransactionFormatException {
        this(type, parseDateTimeStr(dateTimeStr), transactionID, parseAmountStr(amountStr));
    }

    //============================================================
    // parseTransaction: type dateTime transactionID amount...
    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String [] tokens = Tokenizer.getTokens(record);

        // chk for blank line (no tokens)
        if (tokens.length == 0) {
            return null;
        }

        // chk transaction type
        String transactionType = tokens[0];
        if (transactionType.equalsIgnoreCase(MTR.TypeHdrStr)) {
            return MTR.parseTransaction(record);
        } else if (transactionType.equalsIgnoreCase(BusFare.TypeHdrStr)) {
            return BusFare.parseTransaction(record);
        } else if (transactionType.equalsIgnoreCase(Retail.TypeHdrStr)) {
            return Retail.parseTransaction(record);
        } else if (transactionType.equalsIgnoreCase(TopUp.TypeHdrStr)) {
            return TopUp.parseTransaction(record);
        } else {
            throw new OCTransactionFormatException("parseTransaction: Invalid transaction type: " + tokens[0]);
        }
    }

    public String toRecord(){
        return this.type + " " + this.getDateStr() + " " + this.transactionID + " " + this.amount;
    }

    public boolean match(String[] criteria) throws ParseException, OCTransactionSearchException {
        return false;
    }

    //============================================================
    // Helper Methods
    public String getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public String getDateStr() {
        return new SimpleDateFormat("MMM. d, yyyy (E)", Locale.ENGLISH).format(date);
    }

    public String getSearchDateStr(){return new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH).format(date); }

    public String getTimeStr() {
        return new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(date);
    }

    public String getTransactionID() {
        return transactionID;
    }

    public double getAmount() {
        return amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRecordHdr() {
        return type + " " + DateTimeUtil.dateTime2Str(date) + " " + transactionID + " " + amount;
    }

    public static int Str2Int(String inputStr){
        int num = 0;
        for(int i = 0; i < inputStr.length(); i++){
            num = num *10 + (inputStr.charAt(i)-'0');
        }
        return num;
    }

    //============================================================
    // Helper Method -- parseDateTimeStr
    private static Date parseDateTimeStr(String dateStr) throws OCTransactionFormatException {
        try {
            return DateTimeUtil.str2DateTime(dateStr);
        } catch (NumberFormatException | ParseException e) {
            throw new OCTransactionFormatException("parseDateTimeStr: Corrupted dateTime: " + dateStr);
        }
    }


    //============================================================
    // Helper Method -- parseAmountStr
    private static double parseAmountStr(String amountStr) throws OCTransactionFormatException {
        try {
            return Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new OCTransactionFormatException("parseAmountStr: Corrupted amount: " + amountStr);
        }
    }

    public static OCTransaction typeOfOCT(OCTransaction inputOCT) throws OCTransactionFormatException {
        if(inputOCT.getType().equalsIgnoreCase("MTR")){
            return (MTR)inputOCT;
        }else if(inputOCT.getType().equalsIgnoreCase("Retail")){
            return (Retail)inputOCT;
        }else if(inputOCT.getType().equalsIgnoreCase("TopUp")){
            return (TopUp)inputOCT;
        }else if(inputOCT.getType().equalsIgnoreCase("BusFare")){
            return (BusFare)inputOCT;
        }else {
            throw new OCTransactionFormatException("Wrong type format.");
        }
    }

    //============================================================
    // Helper Method -- matchDate
    protected boolean matchDate(String matchDateStr) throws ParseException {
        String dateFormat = "yyyy-MM-dd";

        // validate dateStr
        if (matchDateStr.length() != 10) {
            throw new ParseException("Invalid date format", 0);
        }
        new SimpleDateFormat(dateFormat).parse(matchDateStr);
        return new SimpleDateFormat(dateFormat, Locale.ENGLISH).format(date).equals(matchDateStr);
    }

    public static boolean typeIsValid(String type){
        if(type.equalsIgnoreCase("MTR") || type.equalsIgnoreCase("BusFare")
        || type.equalsIgnoreCase("Retail") || type.equalsIgnoreCase("TopUP")){
            return true;
        }else {
            return false;
        }
    }


    //============================================================
    // toString
    @Override
    public String toString() {
        String str = "";
        str += "    TransactionID: " + transactionID + "\n";
        str += "    Date/Time: " + getDateStr() + " at " + getTimeStr() + "\n";
        str += "    Amount: " + amount + "\n";
        str += "    Status: " + status + "\n";
        return str;
    }


    //============================================================
    // OCTransactionSearchException
    public static class OCTransactionSearchException extends Exception {
        public OCTransactionSearchException(String ocTransactionSearchExMsg) {
            super(ocTransactionSearchExMsg);
        }
    }


    //============================================================
    // OCTransactionFormatException
    public static class OCTransactionFormatException extends Exception {
        public OCTransactionFormatException(String ocTransactionFormatExMsg) {
            super(ocTransactionFormatExMsg);
        }
    }


}
