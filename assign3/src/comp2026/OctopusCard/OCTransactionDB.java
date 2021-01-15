package comp2026.OctopusCard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class OCTransactionDB {
    private ArrayList<OCTransaction> transactionList;

    //============================================================
    // constructor
    public OCTransactionDB() {
        this.transactionList = new ArrayList<OCTransaction>();
    }


    //============================================================
    // loadDB
    public void loadDB(String fName) throws OCTransactionDBException {
        int cnt = 0;
        int lineNo = 0;

        try {
            System.out.println("Loading transaction db from " + fName + "...");
            Scanner in = new Scanner(new File(fName));

            // fixme: the number of records read, and the line number of corrupted records are not reported corrected!
            while (in.hasNextLine()) {
                String line = in.nextLine();
                lineNo++;
                try {
                    if (addTransaction(line) != null) {
                        cnt++;
                    }
                } catch (OCTransactionDBException | OCTransaction.OCTransactionFormatException e) {
                    System.out.println("OCTransactionDB.loadDB: error loading record from line " + lineNo + " of " + fName + " -- " + e.getMessage());
                }

            }
            in.close();
        } catch (FileNotFoundException e) {
            throw new OCTransactionDBException("loadDB failed: File not found (" + fName + ")!");
        }
        System.out.println(cnt + " Octopus card transactions loaded.");
    }


    //============================================================
    // saveDB
    public void saveDB(String fName) {
        int cnt = 0;
        try {
            PrintWriter out = new PrintWriter(fName);
            for (OCTransaction ocTransaction : transactionList) {
                out.println(ocTransaction.toRecord());
                cnt++;
            }
            out.close();
            System.out.println(cnt + " Octopus card transactions saved to " + fName + ".");
        }catch (FileNotFoundException e){
            System.out.println(e);
        }
    }


    //============================================================
    // list
    public void list(String type) {
        int cnt = 0;
        type = type.toLowerCase();
        //System.out.println(transactionList.size());
        if(type.length() == 0){
            for(int i = 0; i < transactionList.size(); i++){
                switch (transactionList.get(i).getType()){
                    case "MTR":
                        System.out.println(toMtr(transactionList.get(i)));
                        break;
                    case "Retail":
                        System.out.println(toRetail(transactionList.get(i)));
                        break;
                    case "BusFare":
                        System.out.println(toBusFare(transactionList.get(i)));
                        break;
                    case "TopUp":
                        System.out.println(toTopUp(transactionList.get(i)));
                        break;
                    default: break;
                }

                cnt++;
            }
        }else{
            for(int i = 0; i < transactionList.size(); i++){
                if(type.equals(transactionList.get(i).getType().toLowerCase())){
                    System.out.println(transactionList.get(i));
                    cnt++;
                }
            }
        }

        // fixme: go through transactionList, and print transactions [done
        // with matching type (or print all if type is an empty string)
        // Note: (1) should ignore letter case for the type; and
        //       (2) should count the number of records correctly.
        System.out.println(cnt + " record(s) found.");
    }

    public void list() {
        list("");
    }

    public MTR toMtr(OCTransaction input){
        return (MTR)input;
    }

    public Retail toRetail(OCTransaction input){
        return (Retail)input;
    }

    public BusFare toBusFare(OCTransaction input){
        return (BusFare)input;
    }

    public TopUp toTopUp(OCTransaction input){
        return (TopUp)input;
    }

    //============================================================
    // addTransaction
    public void addTransaction(OCTransaction newTransaction) throws OCTransactionDBException {
        //System.out.println(newTransaction);
        if(isDuplicated(newTransaction)){
            throw new OCTransactionDBException("Transaction is duplicated.");
        }else {
            transactionList.add(newTransaction);
        }

        for (int i = 0; i < transactionList.size(); i++) {
            for (int j = i; j < transactionList.size(); j++) {
                if (transactionList.get(i).getDate().after(transactionList.get(j).getDate())) {
                    OCTransaction temp = transactionList.get(j);
                    transactionList.set(j, transactionList.get(i));
                    transactionList.set(i, temp);
                }
            }
        }

    }

    public boolean isDuplicated(OCTransaction newTransaction){
        for(int i = 0; i < this.transactionList.size(); i++){
            if(this.transactionList.get(i).getType().equals(newTransaction.getType()) &&
            this.transactionList.get(i).getDateStr().equals(newTransaction.getDateStr()) &&
            this.transactionList.get(i).getTransactionID().equals(newTransaction.getTransactionID())){
                return true;
            }
        }
        return false;
    }

    public OCTransaction addTransaction(String record) throws OCTransactionDBException, OCTransaction.OCTransactionFormatException {
        OCTransaction transaction = OCTransaction.parseTransaction(record);

        // skip blank lines
        if (transaction == null) {
            return null;
        }

        addTransaction(transaction);

        // fixme: Do this last when we try to match MTR checkout transactions
        // with their corresponding checkin transaction.

        // is this a mtr checkout?
        if (transaction.getType().equalsIgnoreCase("MTR") && ((MTR) transaction).getMtrType() == MTR.MTRType.CheckOut) {
            mtrCheckOut((MTR) transaction);
        }

        return transaction;
    }


    //============================================================
    // mtrCheckOut
    private void mtrCheckOut(MTR chkOutTransaction) throws OCTransactionDBException {
        // fixme: from the newest to the oldest transactions, search for
        // an outstanding MTR checkin transaction.
        int index = -1;
        for(int i = 0; i < this.transactionList.size(); i++){
            if(this.transactionList.get(i).getType().equalsIgnoreCase("MTR")&&
                    this.transactionList.get(i).getStatus()== OCTransaction.Status.MTR_OUTSTANDING){
                index = i;
                break;
            }
        }

        // if the checkin transaction is found, match them so that
        // they keep a reference pointing to each other.
        if(index != -1){
            chkOutTransaction.setStatus(OCTransaction.Status.MTR_COMPLETED);
            //System.out.println("Test: \n" + this.transactionList.get(index));
            this.transactionList.get(index).setStatus(OCTransaction.Status.MTR_COMPLETED);
            chkOutTransaction.setPointTo((MTR)this.transactionList.get(index));
            ((MTR)this.transactionList.get(index)).setPointTo(chkOutTransaction);
        }else{
            transactionList.remove(chkOutTransaction);
            throw new OCTransactionDBException("add: failed to add record -- mtrCheckOut: No outstanding MTR CheckIn transaction found!");
        }


        // if no outstanding checkin transaction can be found, throw
        // a OCTransactionDBException.
    }

    //============================================================
    // search
    public OCTransaction [] search(String type, String[] criteria) throws OCTransaction.OCTransactionSearchException, ParseException, OCTransaction.OCTransactionFormatException {
        OCTransaction [] searchResult = new OCTransaction[0];
        if(OCTransaction.typeIsValid(type)){
            // search through the transactions now
            for (OCTransaction transaction : transactionList) {
                if (transaction.getType().equalsIgnoreCase(type) && OCTransaction.typeOfOCT(transaction).match(criteria)) {
                    searchResult = addOCArray(searchResult, transaction);
                }
            }
        }else {
            throw new OCTransaction.OCTransactionSearchException("The type is not valid.");
        }
        return searchResult;
    }

    public OCTransaction[] addOCArray(OCTransaction[] oriArray, OCTransaction added){
        OCTransaction[] returnArray = new OCTransaction[oriArray.length + 1];
        for(int i = 0; i < oriArray.length; i++){
            returnArray[i] = oriArray[i];
        }

        returnArray[returnArray.length-1] = added;
        return returnArray;
    }


    //============================================================
    // searchIdx
    private int searchIdx(String type, Date date, String transactionID) {
        for (int i = 0; i < transactionList.size(); i++) {
            if(this.transactionList.get(i).getType().equals(type) &&
            this.transactionList.get(i).getDate() == date &&
            this.transactionList.get(i).getTransactionID().equals(transactionID)){
                return i;
            }
        }
        return -1;
    }


    //============================================================
    // OCTransactionDBException
    public static class OCTransactionDBException extends Exception {
        public OCTransactionDBException(String ocTransactionDBExMsg) {
            super(ocTransactionDBExMsg);
        }
    }
}
