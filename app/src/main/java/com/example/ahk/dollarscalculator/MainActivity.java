package com.example.ahk.dollarscalculator;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String EXTRA_CARD_TYPE = "cardType";
    static final String EXTRA_AYYAM_ARRAY = "ayyamArray";
    static final String EXTRA_DOLLARS_ARRAY = "dollarsArray";
    static final int REQUEST_ACTIVITY_CARDS = 200;


    GregorianCalendar calendar, nextCalendar;
    ImageButton buttonDecrease, buttonIncrease;
    SimpleDateFormat dateFormatter;

    TouchCards touch;
    AlfaCards alfa;

    Cards lastCard;

    ArrayList<DollarsData> dollarsDataList;

    CheckBox cummulative;
    TextView tvDate, tvTotalMoney;
    TextView tv_touchSentDollars, tv_touchReceivedDollars, tv_touchCardsCount;
    TextView tv_alfaSentDollars, tv_alfaReceivedDollars, tv_alfaCardsCount;

    int dayIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        cummulative = (CheckBox) findViewById(R.id.cb_cumulative);

        tv_touchSentDollars = (TextView) findViewById(R.id.tv_TouchSent);
        tv_touchReceivedDollars = (TextView) findViewById(R.id.tv_TouchReceived);
        tv_touchCardsCount = (TextView) findViewById(R.id.tv_TouchCards);

        tv_alfaSentDollars = (TextView) findViewById(R.id.tv_AlfaSent);
        tv_alfaReceivedDollars = (TextView) findViewById(R.id.tv_AlfaReceived);
        tv_alfaCardsCount = (TextView) findViewById(R.id.tv_AlfaCards);

        tvDate = (TextView) findViewById(R.id.row_textDescription);
        tvTotalMoney = (TextView) findViewById(R.id.tv_TotalMoney);

        buttonDecrease = (ImageButton) findViewById(R.id.button_DecreaseDate);
        buttonIncrease = (ImageButton) findViewById(R.id.button_IncreaseDate);
        buttonIncrease.setEnabled(false);

        dateFormatter = (SimpleDateFormat) DateFormat.getDateInstance();
        dateFormatter.applyPattern("dd-MM-yyyy");

        initiateData();
    }

    void initiateData() {
        touch = new TouchCards(this);
        alfa = new AlfaCards(this);

        dayIndex = 0;
        dollarsDataList = new ArrayList<>();

        calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        nextCalendar = (GregorianCalendar) calendar.clone();
        nextCalendar.add(Calendar.DAY_OF_YEAR, 1);

        tvDate.setText(dateFormatter.format(calendar.getTime()));

        getLoaderManager().restartLoader(Cards.CARD_TYPE_TOUCH, null, this);
        getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFA, null, this);
        getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFAGIFT, null, this);
    }

    public void updateDate(View v) {
        int changeDay = -1;
        if (v.getId() == R.id.button_IncreaseDate) {
            dayIndex--;
            changeDay = 1;
            if ((calendar.get(Calendar.DAY_OF_YEAR) + 1) == (Calendar.getInstance().get(Calendar.DAY_OF_YEAR))) {
                buttonIncrease.setEnabled(false);
            }
        } else {
            dayIndex++;
            buttonIncrease.setEnabled(true);
            buttonDecrease.setEnabled(false);
        }

        calendar.add(Calendar.DAY_OF_YEAR, changeDay);
        nextCalendar.add(Calendar.DAY_OF_YEAR, changeDay);
        tvDate.setText(dateFormatter.format(calendar.getTime()));

        if (dayIndex + 1 > dollarsDataList.size()) {
            getLoaderManager().restartLoader(Cards.CARD_TYPE_TOUCH, null, this);
            getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFA, null, this);
            getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFAGIFT, null, this);
        } else {
            showData(dayIndex,cummulative.isChecked());
        }
    }

    static final String[] Message_SUMMARY_PROJECTION = new String[]{
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sender;
        if (id == Cards.CARD_TYPE_ALFA) {
            sender = "'alfa'";
        } else if (id == Cards.CARD_TYPE_TOUCH) {
            sender = "1199";
        } else {
            sender = "'Alfa'";
        }

        String select = "(" + Telephony.Sms.Inbox.ADDRESS + "=" + sender + " AND " +
                Telephony.Sms.Inbox.DATE + ">" + calendar.getTimeInMillis() + " AND " +
                Telephony.Sms.Inbox.DATE + "<" + nextCalendar.getTimeInMillis() + ")";
        return new CursorLoader(this, Telephony.Sms.Inbox.CONTENT_URI,
                Message_SUMMARY_PROJECTION, select, null,
                Telephony.Sms.Inbox.DATE + " ASC");
    }

    int loaderRounds = 3;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loaderRounds == 3) {
            touch.Clear();
            alfa.Clear();
        }

        if (loader.getId() == Cards.CARD_TYPE_TOUCH) {
            touch.ProcessData(data);
        } else if (loader.getId() == Cards.CARD_TYPE_ALFA) {
            alfa.ProcessData(data);
        } else if (loader.getId() == Cards.CARD_TYPE_ALFAGIFT) {
            alfa.GiftProcess(data);
        }

        if (--loaderRounds == 0) {
            dollarsDataList.add(new DollarsData(touch, alfa));
            showData(dayIndex, cummulative.isChecked());
            loaderRounds = 3;
        }

    }


    @Override
    public void onLoaderReset(Loader loader) {

    }

    void showData(int index, boolean isCumulative) {
        DollarsData data;
        if (isCumulative) {
            data = new DollarsData();
            for (int i = 0; i <= index; i++) {
                data.addData(dollarsDataList.get(i));
            }
        } else {
            data = dollarsDataList.get(index);
        }

        tv_touchReceivedDollars.setText(String.valueOf(data.touchReceivedDollars));
        tv_touchSentDollars.setText(String.format(Locale.getDefault(), "%.2f", data.touchSentDollars));
        tv_touchCardsCount.setText(String.valueOf(data.touchCardsCount));

        tv_alfaReceivedDollars.setText(String.valueOf(data.alfaReceivedDollars));
        tv_alfaSentDollars.setText(String.format(Locale.getDefault(), "%.2f", data.alfaSentDollars));
        tv_alfaCardsCount.setText(String.valueOf(data.alfaCardsCount));

        tvTotalMoney.setText(String.valueOf(data.touchTotalMoney + data.alfaTotalMoney));

        buttonDecrease.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuitem_alfa || item.getItemId() == R.id.menuitem_touch) {
            lastCard = touch;
            if (item.getItemId() == R.id.menuitem_alfa) {
                lastCard = alfa;
            }

            Intent i = new Intent(this, PricesActivity.class);

            i.putExtra(EXTRA_CARD_TYPE, lastCard.Name);
            i.putExtra(EXTRA_AYYAM_ARRAY, lastCard.ayyamPrice);
            i.putExtra(EXTRA_DOLLARS_ARRAY, lastCard.dollarsPrice);
            startActivityForResult(i, REQUEST_ACTIVITY_CARDS);
            return true;

        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            initiateData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
