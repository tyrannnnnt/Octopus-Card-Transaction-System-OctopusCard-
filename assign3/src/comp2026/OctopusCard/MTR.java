package comp2026.OctopusCard;
import comp2026.OctopusCard.Util.Tokenizer;

import java.text.ParseException;
import java.util.Date;

public class MTR extends OCTransaction {
    public enum MTRType{
        CheckIn,
        CheckOut
    }

    public  MTRType mtrType;
    private String station;
    private MTR pointTo;
    public static final String TypeHdrStr = "MTR";

    public MTR(String type, Date date, String transactionID, double amount, String mtrType, String station)throws OCTransactionFormatException{
        super(type, date, transactionID, amount);
        setOthers(mtrType, station);
    }

    public MTR(String type, String dateTimeStr, String transactionID, String amountStr, String mtrType, String station)throws OCTransactionFormatException{
        super(type, dateTimeStr, transactionID, amountStr);
        setOthers(mtrType, station);
    }

    private void setOthers(String mtrType, String station) throws OCTransactionFormatException {
        this.setStatus(Status.MTR_OUTSTANDING);
        if(mtrType.equalsIgnoreCase("CheckIn")) {
            this.mtrType = MTRType.CheckIn;
        }else if(mtrType.equalsIgnoreCase("CheckOut")){
            this.mtrType = MTRType.CheckOut;
        }else {
            throw new OCTransactionFormatException("MTRTransactionFormatException: Invalid checkIn/checkOut type");
        }
        this.station = station;
    }

    public void setPointTo(MTR pointTo){
        this.pointTo = pointTo;
        setStatus(Status.MTR_COMPLETED);
    }

    public String getStation(){
        return station;
    }

    public MTR getPointTo(){
        return this.pointTo;
    }

    public MTRType getMtrType(){
        return this.mtrType;
    }

    @Override
    public String toRecord() {
        return super.toRecord() + " " + this.mtrType + " " + this.station;
    }

    @Override
    public boolean match(String[] criteria) throws ParseException, OCTransactionSearchException {

        switch (criteria[0].toLowerCase()) {
            case "station":
                return this.station.equals(criteria[1]);
            case "mtrtype":
                if (criteria[1].equals("CheckIn")) {
                    return this.mtrType == MTRType.CheckIn;
                } else if (criteria[1].equals("CheckOut")) {
                    return this.mtrType == MTRType.CheckOut;
                } else {
                    System.out.println("Wrong input format.");
                    return false;
                }
            case "status":
                if (criteria[1].equals("Completed")) {
                    return this.getStatus().equals(Status.MTR_COMPLETED);
                } else if (criteria[1].equals("Outstanding")) {
                    return this.getStatus().equals(Status.MTR_OUTSTANDING);
                } else {
                    System.out.println("Wrong input format.");
                    return false;
                }
            case "date":
                return matchDate(criteria[1]);
            default:
                throw new OCTransaction.OCTransactionSearchException("search: Error: Invalid MTR search type: " + criteria[0]);
        }


    }

    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String [] tokens = Tokenizer.getTokens(record);
        // chk for blank line (no tokens)
        if (tokens.length == 0) {
            return null;
        }
        String stationStr = tokens[5];

        for(int i = 6; i < tokens.length; i++){
            stationStr += " " + tokens[i];
        }
        return new MTR(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], stationStr);
    }

    @Override
    public String toString() {
        if(this.pointTo!=null) {
            return "[MTR Transaction]" +
                    "\nMTR Type: " + this.mtrType +
                    "\nMatching TransactionID: " + this.pointTo.getTransactionID() +
                    "\n   Date/Time:" + this.pointTo.getDateStr() + " at " + this.pointTo.getTimeStr() +
                    "\n   Station: " + this.pointTo.getStation() +
                    "\n   Station: " +this.station +
                    "\n" +
                    super.toString();
        }else {
            return "[MTR Transaction]" +
                    "\nMTR Type: " + this.mtrType +
                    "\n Station: " +this.station +
                    "\n" +
                    super.toString();
        }
    }
}
