package comp2026.OctopusCard;
import comp2026.OctopusCard.Util.Tokenizer;

import java.text.ParseException;
import java.util.Date;

public class BusFare extends OCTransaction {
    private int route;
    private String station;
    private String terminal;
    public static final String TypeHdrStr = "BusFare";

    public BusFare(String type, Date date, String transactionID, double amount, int route, String station, String terminal)throws OCTransactionFormatException{
        super(type, date, transactionID, amount);
        setOthers(route, station, terminal);
    }

    public BusFare(String type, String dateTimeStr, String transactionID, String amountStr, int route, String station, String terminal)throws OCTransactionFormatException{
        super(type, dateTimeStr, transactionID, amountStr);
        setOthers(route, station, terminal);
    }

    private void setOthers(int route, String station, String terminal){
        this.route = route;
        this.station = station;
        this.terminal = terminal;
        this.setStatus(Status.COMPLETED);
    }

    @Override
    public String toRecord() {
        return super.toRecord() + " " + this.route + " " +this.station + " to " + this.terminal;
    }

    @Override
    public boolean match(String[] criteria) throws ParseException, OCTransactionSearchException {

        switch (criteria[0]) {
            case "route":
                return (this.route + "").equals(criteria[1]);
            case "station":
                return this.station.contains(criteria[1]);
            case "terminal":
                return this.terminal.contains(criteria[1]);
            case "date":
                return matchDate(criteria[1]);
            default:
                throw new OCTransaction.OCTransactionSearchException("search: Error: Invalid Bus Fare search type: " + criteria[0]);
        }
    }

    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String [] tokens = Tokenizer.getTokens(record);
        // chk for blank line (no tokens)
        if (tokens.length == 0) {
            return null;
        }
        String stationStr = tokens[5];
        int pos = 0;

        for (int i = 6; i < tokens.length; i++){
            if(!tokens[i].equals("to")){
                stationStr += " " + tokens[i];
            }else {
                pos = i+1;
                break;
            }
        }
        String terminalStr = tokens[pos];
        for(int i = pos+1; i < tokens.length; i++){
            terminalStr += " " + tokens[i];
        }

        return new BusFare(tokens[0], tokens[1], tokens[2], tokens[3], OCTransaction.Str2Int(tokens[4]), stationStr, terminalStr);
    }

    @Override
    public String toString() {
        return "[Bus Fare]" +
                "\nRoute: " + this.route +
                "\nTerminal: " + this.terminal +
                "\nStation: " + this.station +
                "\n" + super.toString();
    }
}
