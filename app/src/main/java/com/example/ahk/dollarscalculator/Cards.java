package com.example.ahk.dollarscalculator;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

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


    MainActivity ownerActivity;

    String Name;
    int[] dollarsPrice;
    int[] ayyamPrice;
    Double sentDollars = 0.0;
    int receivedDollars = 0;
    int cardsCount = 0;
    Integer TotalMoney = 0;

    String defaultDollarsPrices, defaultAyyamPrices;

    HashMap<String, Integer> Ayyam = new HashMap<>();

    Cards(Context c) {
        ownerActivity = (MainActivity) c;
    }

    void Clear() {
        sentDollars = 0.0;
        receivedDollars = 0;
        cardsCount = 0;
        TotalMoney = 0;
        Ayyam.clear();
    }

    void processAyam() {
        for (HashMap.Entry<String, Integer> entry : Ayyam.entrySet()) {
            int dollars = entry.getValue();
            while (dollars > 20) {
                cardsCount++;
                dollars -= 20;
                TotalMoney += ayyamPrice[0];
            }

            cardsCount++;
            TotalMoney += ayyamPrice[20 - dollars];
        }
    }

    void preparePrices() {
        if (ownerActivity.getSharedPreferences(Name + SHARED_PREF, Activity.MODE_PRIVATE).contains(SHARED_DOLLARS)) {
            restorePrices();
            return;
        }
        ayyamPrice = stringToIntArray(defaultAyyamPrices);
        dollarsPrice = stringToIntArray(defaultDollarsPrices);

        for (int i = 1; i < 10; i++) {
            ayyamPrice[i + 10] = ayyamPrice[i] + dollarsPrice[10];
        }
    }

    void restorePrices() {
        ayyamPrice = stringToIntArray(ownerActivity.getSharedPreferences(Name + SHARED_PREF, Activity.MODE_PRIVATE).getString(SHARED_AYYAM, "[ ]"));
        dollarsPrice = stringToIntArray(ownerActivity.getSharedPreferences(Name + SHARED_PREF, Activity.MODE_PRIVATE).getString(SHARED_DOLLARS, "[ ]"));
    }

    abstract void ProcessData(Cursor data);

    private int[] stringToIntArray(String s) {
        String[] items = s.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");

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

    TouchCards(Context c) {
        super(c);
        Name = "touch";
        defaultDollarsPrices = "[0, 2500, 4000, 5000, 6500, 8000, 9000, 10000, 11000, 12000, 13000]";
        defaultAyyamPrices = "[16000, 18000, 19500, 21000, 22000, 23000, 24500, 26000, 27000, 28000, 29000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43000]";
        //defaultAyyamPrices = "[43000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29000, 28000, 27000, 26000, 24500, 23000, 22000, 21000, 19500, 18000, 16000]";
        preparePrices();
    }


    @Override
    public void ProcessData(Cursor data) {
        if (data.getCount() > 0) {
            lost = 0;
            while (data.moveToNext()) {
                String row = data.getString(1);
                Calendar sentDate = new GregorianCalendar();
                sentDate.setTimeInMillis(data.getLong(2));
                boolean afterTen = sentDate.get(Calendar.HOUR_OF_DAY) >= 22;
                if (row.contains("from your")) {
                    String dollarsSentString = row.split(" ")[2].substring(1);
                    Integer dollarsSent = Integer.parseInt(dollarsSentString);
                    sentDollars += dollarsSent + 0.45;
                    TotalMoney += dollarsPrice[dollarsSent];
                    if (afterTen)
                        sentDollars -= 0.01;
                } else if (row.contains("to your")) {
                    String[] words = row.split(" ");
                    String dollarsReceivedString = words[2].substring(1);
                    int dollarsReceived = Integer.parseInt(dollarsReceivedString);
                    int d = 0;
                    String number = words[12];
                    if (Ayyam.containsKey(number)) {
                        d = Ayyam.get(number);
                    }
                    Ayyam.put(number, dollarsReceived + d);
                    receivedDollars += dollarsReceived;
                } else {
                    sentDollars += 0.05;
                    if (afterTen) sentDollars -= 0.01;
                    lost++;
                }
            }

            processAyam();
        }
    }
}

class AlfaCards extends Cards {

    AlfaCards(Context c) {
        super(c);
        Name = "alfa";
        defaultDollarsPrices = "[0, 2500, 4500, 6000, 7500, 9000, 10000, 11500, 12500, 13500, 14500]";
        defaultAyyamPrices = "[14000, 16000, 17500, 19000, 20500, 22000, 23500, 25000, 26000, 27000, 28000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43000]";
        //defaultAyyamPrices = "[43000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28000, 27000, 26000, 25000, 23500, 22000, 20500, 19000, 17500, 16000, 14000]";
        preparePrices();
    }

    @Override
    public void ProcessData(Cursor data) {
        if (data.getCount() > 0) {
            while (data.moveToNext()) {
                String row = data.getString(1);
                if (row.contains("from your")) {
                    String dollarsSentString = row.split(" ")[1].substring(11);
                    Integer dollarsSent = Integer.parseInt(dollarsSentString);
                    sentDollars += dollarsSent + 0.4;
                    TotalMoney += dollarsPrice[dollarsSent];
                } else if (row.contains("to your")) {
                    String[] words = row.split(" ");
                    String dollarsReceivedString = words[1].substring(11);
                    int dollarsReceived = Integer.parseInt(dollarsReceivedString);
                    int d = 0;
                    String number = words[14].substring(0, 11);
                    if (Ayyam.containsKey(number)) {
                        d = Ayyam.get(number);
                    }
                    Ayyam.put(number, dollarsReceived + d);
                    receivedDollars += dollarsReceived;
                }
            }
            processAyam();

        }
    }

    void GiftProcess(Cursor data) {
        if (data.getCount() > 0) {
            while (data.moveToNext()) {
                String row = data.getString(1);
                if (row.contains("you have offered")) {
                    String[] words = row.split(" ");
                    if (words[7].length() < 9) continue;
                    String dollarsSentString = words[16].substring(0, 2);
                    int dollarsSent = Integer.parseInt(dollarsSentString);
                    sentDollars += dollarsSent;
                    while (dollarsSent > 10) {
                        TotalMoney += dollarsPrice[10];
                        dollarsSent -= 10;
                    }
                    TotalMoney += dollarsPrice[dollarsSent];
                }
            }
        }
    }
}

class DollarsData
{
    double touchSentDollars;
    int touchReceivedDollars;
    int touchCardsCount;
    int touchTotalMoney;

    double alfaSentDollars;
    int alfaReceivedDollars;
    int alfaCardsCount;
    int alfaTotalMoney;

    DollarsData()
    {
        touchSentDollars = 0;
        touchReceivedDollars = 0;
        touchCardsCount = 0;
        touchTotalMoney = 0;

        alfaSentDollars = 0;
        alfaReceivedDollars = 0;
        alfaCardsCount = 0;
        alfaTotalMoney = 0;
    }
    DollarsData(TouchCards tc, AlfaCards ac)
    {
        touchSentDollars = tc.sentDollars;
        touchReceivedDollars = tc.receivedDollars;
        touchCardsCount = tc.cardsCount;
        touchTotalMoney = tc.TotalMoney;

        alfaSentDollars = ac.sentDollars;
        alfaReceivedDollars = ac.receivedDollars;
        alfaCardsCount = ac.cardsCount;
        alfaTotalMoney = ac.TotalMoney;
    }

    void addData(DollarsData d)
    {
        touchSentDollars += d.touchSentDollars;
        touchReceivedDollars += d.touchReceivedDollars;
        touchCardsCount += d.touchCardsCount;
        touchTotalMoney += d.touchTotalMoney;

        alfaSentDollars += d.alfaSentDollars;
        alfaReceivedDollars += d.alfaReceivedDollars;
        alfaCardsCount += d.alfaCardsCount;
        alfaTotalMoney += d.alfaTotalMoney;
    }

}