package com.example.ahk.dollarscalculator;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by AHK on 23-Nov-17.
 */

abstract class Cards {

    static final int CARD_TYPE_TOUCH = 1;
    static final int CARD_TYPE_ALFA = 2;
    static final int CARD_TYPE_ALFAGIFT = 3;

    static final String SHARED_PREF = "_shared";
    static final String SHARED_AYYAM = "ayyam_shared";
    static final String SHARED_DOLLARS = "dollars_shared";
//    public static final String SHARED_ALFA_AYYAM = "ayyam_shared2";
//    public static final String SHARED_ALFA_DOLLARS = "dollars_shared2";

    static final String DEFAULT_TOUCH_AYAM_PRICES = "[300000, 340000, 380000, 420000, 460000, 500000, 230000, 0, 0, 0, 0, 0, 0, 600000, 2000000]";
    static final String DEFAULT_TOUCH_DOLLARS_PRICES = "[0, 50000, 90000, 130000, 170000, 210000, 250000]";

    static final String DEFAULT_ALFA_AYAM_PRICES = "[300000, 340000, 380000, 420000, 460000, 500000, 230000, 0, 0, 0, 0, 0, 0, 600000, 2000000]";
    static final String DEFAULT_ALFA_DOLLARS_PRICES = "[0, 50000, 90000, 130000, 170000, 210000, 250000]";


    MainActivity ownerActivity;

    String Name;
    int[] dollarsPrice;
    int[] ayyamPrice;
    double sentDollars = 0.0;
    double receivedDollars = 0;
    int cardsBigCount = 0;
    int cardsSmallCount = 0;
    Integer TotalMoney = 0;
    int year = 0;

    public boolean DataProcessed = false;

    String defaultDollarsPrices, defaultAyyamPrices;

    HashMap<String, Double> Ayyam = new HashMap<>();

    Cards(Context c) {
        ownerActivity = (MainActivity) c;
    }

    void Clear() {
        sentDollars = 0.0;
        receivedDollars = 0.0;
        cardsBigCount = 0;
        TotalMoney = 0;
        Ayyam.clear();
        cardsSmallCount = 0;
        year = 0;
        DataProcessed = false;
    }

    void processAyam() {
        for (HashMap.Entry<String, Double> entry : Ayyam.entrySet()) {
            double dollars = entry.getValue();

//            if (dollars > 140) {
//                year++;
//                TotalMoney += ayyamPrice[20];
//                double extraDollar = 220 - dollars;
//                while (extraDollar > 10) {
//                    TotalMoney += dollarsPrice[10];
//                    extraDollar -= 10;
//                }
//                if (extraDollar > 0) {
//                    int pos = (int) extraDollar * 2;
//                    TotalMoney += dollarsPrice[pos];
//                }
//                continue;
//            }
            if (dollars > 54) {
                year++;
                TotalMoney += ayyamPrice[14];
                double extraDollar = 72.0 - dollars;
                while (extraDollar >= 3) {
                    TotalMoney += dollarsPrice[6];
                    extraDollar -= 3;
                }
                //int pos = (int) (extraDollar * 2);
                //TotalMoney += dollarsPrice[pos];
                if (extraDollar > 3)
                    TotalMoney += (int) ((3 - extraDollar) * PricesActivity.half_dollar_increment * 2);

            } else if (dollars > 18 && dollars <= 21) {
                cardsBigCount += 3;
                TotalMoney += ayyamPrice[13] + 60000;
                if (Name.equals("touch")) TotalMoney += 20000;
                double extraDollars = dollars - 18;
                if (extraDollars == 3) continue;
                //TotalMoney += dollarsPrice[(int) ((3 - extraDollars) * 2)] - 5000;
                TotalMoney += (int) ((3 - extraDollars) * PricesActivity.half_dollar_increment * 2);

            } else if (dollars > 15 && dollars <= 18) {
                cardsBigCount += 2;
                cardsSmallCount++;
                TotalMoney += ayyamPrice[13];
                double extraDollars = dollars - 15;
                if (extraDollars == 3) continue;
                //TotalMoney += dollarsPrice[(int) ((3 - extraDollars) * 2)] - 5000;
                TotalMoney += (int) ((3 - extraDollars) * PricesActivity.half_dollar_increment * 2);

            } else {

                while (dollars > 6) {
                    cardsBigCount++;
                    dollars -= 6;
                    TotalMoney += ayyamPrice[0];
                }

                if (dollars <= 3)
                    cardsSmallCount++;
                else
                    cardsBigCount++;

                int pos = (int) (dollars * 2);
                TotalMoney += ayyamPrice[12 - pos];
            }
        }
    }

    void preparePrices() {
        if (ownerActivity.getSharedPreferences(Name + SHARED_PREF, Activity.MODE_PRIVATE).contains(SHARED_DOLLARS)) {
            restorePrices();
            return;
        }
        ayyamPrice = stringToIntArray(defaultAyyamPrices);
        dollarsPrice = stringToIntArray(defaultDollarsPrices);

        for (int i = 1; i < 6; i++) {
            ayyamPrice[i + 6] = ayyamPrice[6] + (i * PricesActivity.half_dollar_increment);
        }
    }

    void restorePrices() {
        ayyamPrice = stringToIntArray(ownerActivity.getSharedPreferences(Name + SHARED_PREF, Activity.MODE_PRIVATE).getString(SHARED_AYYAM, "[ ]"));
        dollarsPrice = stringToIntArray(ownerActivity.getSharedPreferences(Name + SHARED_PREF, Activity.MODE_PRIVATE).getString(SHARED_DOLLARS, "[ ]"));

    }

    abstract void ProcessData(Cursor data);

    private int[] stringToIntArray(String s) {
        String[] items = s.replaceAll("\\[", "").replaceAll("]", "").replaceAll("\\s", "").split(",");

        int[] results = new int[items.length];

        for (int i = 0; i < items.length; i++) {
            try {
                results[i] = Integer.parseInt(items[i]);
            } catch (NumberFormatException nfe) {
                results[i] = 0;
            }
        }
        return results;

    }
}

class TouchCards extends Cards {

    public int lost = 0;
    public int discountOffer = 0;

    TouchCards(Context c) {
        super(c);
        Name = "touch";
        defaultDollarsPrices = DEFAULT_TOUCH_DOLLARS_PRICES;
        defaultAyyamPrices = DEFAULT_TOUCH_AYAM_PRICES;
        //defaultAyyamPrices = "[43000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29000, 28000, 27000, 26000, 24500, 23000, 22000, 21000, 19500, 18000, 16000]";
        preparePrices();
    }


    @Override
    public void ProcessData(Cursor data) {
        if (data.getCount() > 0) {
            HashMap<String, Double> DollarsSent = new HashMap<>();
            lost = 0;
            while (data.moveToNext()) {
                String row = data.getString(1);
                Calendar sentDate = new GregorianCalendar();
                sentDate.setTimeInMillis(data.getLong(2));
                String[] words = row.split(" ");
                boolean afterTen = sentDate.get(Calendar.HOUR_OF_DAY) >= 22;
                if (row.contains("from your")) {
                    String dollarsSentString = words[2].substring(1);
                    String numberSentString = words[12];
                    double dollarsSent = Double.parseDouble(dollarsSentString);

                    double d = 0;
                    if (DollarsSent.containsKey(numberSentString)) {
                        d = DollarsSent.get(numberSentString);
                    }
                    DollarsSent.put(numberSentString, dollarsSent + d);

                    sentDollars += dollarsSent + 0.16;
                    TotalMoney += dollarsPrice[(int) (dollarsSent * 2)];
                    if (afterTen)
                        sentDollars -= 0;
                } else if (row.contains("to your")) {
                    String dollarsReceivedString = words[2].substring(1);
                    double dollarsReceived = Double.parseDouble(dollarsReceivedString);
                    double d = 0;
                    String number = words[12];
                    if (Ayyam.containsKey(number)) {
                        d = Ayyam.get(number);
                    }
                    Ayyam.put(number, dollarsReceived + d);
                    receivedDollars += dollarsReceived;
                } else {
                    sentDollars += 0.02;
                    if (afterTen) sentDollars -= 0;
                    lost++;
                }
            }

            int discountedDollars = 0;
            for (HashMap.Entry<String, Double> entry : DollarsSent.entrySet()) {
                double sent = entry.getValue();
                if (sent > 3) {
                    discountedDollars += sent / 3;
                    if (sent % 3 == 0) discountedDollars--;
                }


            }
            TotalMoney -= discountOffer * discountedDollars * 5000;

            processAyam();
            //TotalMoney -= extraMonths*1000;
        }
        DataProcessed = true;
    }
}

class AlfaCards extends Cards {

    AlfaCards(Context c) {
        super(c);
        Name = "alfa";

        defaultDollarsPrices = DEFAULT_ALFA_DOLLARS_PRICES;
        defaultAyyamPrices = DEFAULT_ALFA_AYAM_PRICES;
        // old prices
        //defaultDollarsPrices = "[0, 2500, 4500, 6000, 7500, 9000, 10000, 11500, 12500, 13500, 14500]";
        //defaultAyyamPrices = "[14000, 16000, 17500, 19000, 20500, 22000, 23500, 25000, 26000, 27000, 28000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 120000]";
        //defaultAyyamPrices = "[43000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28000, 27000, 26000, 25000, 23500, 22000, 20500, 19000, 17500, 16000, 14000]";
        preparePrices();
    }

    @Override
    public void ProcessData(Cursor data) {
        if (data.getCount() > 0) {
            ArrayList<String> numbers = new ArrayList<>();
            Ayyam.clear();
            while (data.moveToNext()) {
                String row = data.getString(1);
                String[] words = row.split(" ");
                if (row.contains("you have offered")) {
                    String number = words[7];
                    if (number.length() <= 8) {
                        number = "961" + words[7];
                    }
                    if (numbers.contains(number)) continue;
                    numbers.add(number);
                    String dollarsSentString = words[16];
                    if (row.contains("broadband"))
                        dollarsSentString = words[19];
                    if (row.contains("Weekly") || row.contains("MB Data Booster"))
                        dollarsSentString = words[17];
                    if (dollarsSentString.contains("USD"))
                        dollarsSentString = dollarsSentString.replaceAll("USD", "");
                    else if (dollarsSentString.contains("$"))
                        dollarsSentString = dollarsSentString.replaceAll("\\$", "");
                    double dollarsSent = Double.parseDouble(dollarsSentString);
                    sentDollars += dollarsSent;
                    while (dollarsSent > 3) {
                        TotalMoney += dollarsPrice[6] - 10000;
                        dollarsSent -= 3.0;
                    }
                    TotalMoney += dollarsPrice[(int) (dollarsSent * 2)] - 10000;
                } else if (row.contains("from your")) {
                    String dollarsSentString;
                    if (row.contains("dollar")) {
                        dollarsSentString = words[1].substring(11);
                    } else {
                        dollarsSentString = words[2].replaceAll("USD", "");
                        if (dollarsSentString.isEmpty()) dollarsSentString = words[3];
                    }
                    double DollarsSent = Double.parseDouble(dollarsSentString);
                    sentDollars += DollarsSent + 0.14;
                    TotalMoney += dollarsPrice[(int) (DollarsSent * 2)];

                } else if (row.contains("to your")) {
                    String dollarsReceivedString = words[2].replaceAll("USD", "");
                    String number = words[14];
                    if (dollarsReceivedString.isEmpty()) {
                        dollarsReceivedString = words[3];
                        number = words[15];
                    }
                    double dollarsReceived = Double.parseDouble(dollarsReceivedString);
                    double d = 0;
                    if (Ayyam.containsKey(number)) {
                        d = Ayyam.get(number);
                    }
                    Ayyam.put(number, dollarsReceived + d);
                    receivedDollars += dollarsReceived;
                }
                /*
                int discountedDollars = 0;
                for (HashMap.Entry<String, Double> entry : DollarsSent.entrySet()) {
                    double sent = entry.getValue();
                    if (sent > 3) {
                        discountedDollars += sent / 3;
                        if (sent % 3 == 0) discountedDollars--;
                    }


                }
                TotalMoney -= discountOffer * discountedDollars * 5000;*/
            }
            processAyam();
        }
        DataProcessed = true;
    }
}

class DollarsData {
    double touchSentDollars;
    double touchReceivedDollars;
    int touchCardsBigCount;
    int touchTotalMoney;
    int touchYear;

    double alfaSentDollars;
    double alfaReceivedDollars;
    int alfaCardsBigCount;
    int alfaTotalMoney;
    int alfaYear;

    int cardsSmallCount;


    DollarsData() {
        touchSentDollars = 0;
        touchReceivedDollars = 0;
        touchCardsBigCount = 0;
        touchTotalMoney = 0;
        touchYear = 0;

        alfaSentDollars = 0;
        alfaReceivedDollars = 0;
        alfaCardsBigCount = 0;
        alfaTotalMoney = 0;
        alfaYear = 0;

        cardsSmallCount = 0;
    }

    DollarsData(TouchCards tc, AlfaCards ac) {
        touchSentDollars = tc.sentDollars;
        touchReceivedDollars = tc.receivedDollars;
        touchCardsBigCount = tc.cardsBigCount;
        touchTotalMoney = tc.TotalMoney;
        touchYear = tc.year;

        alfaSentDollars = ac.sentDollars;
        alfaReceivedDollars = ac.receivedDollars;
        alfaCardsBigCount = ac.cardsBigCount;
        alfaTotalMoney = ac.TotalMoney;
        alfaYear = ac.year;

        cardsSmallCount = tc.cardsSmallCount + ac.cardsSmallCount;
    }

    void addData(DollarsData d) {
        touchSentDollars += d.touchSentDollars;
        touchReceivedDollars += d.touchReceivedDollars;
        touchCardsBigCount += d.touchCardsBigCount;
        touchTotalMoney += d.touchTotalMoney;
        touchYear += d.touchYear;

        alfaSentDollars += d.alfaSentDollars;
        alfaReceivedDollars += d.alfaReceivedDollars;
        alfaCardsBigCount += d.alfaCardsBigCount;
        alfaTotalMoney += d.alfaTotalMoney;
        alfaYear += d.alfaYear;

        cardsSmallCount += d.cardsSmallCount;
    }

}