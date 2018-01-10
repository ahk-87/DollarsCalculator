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
import android.widget.ImageButton;
import android.widget.TextView;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String EXTRA_CARD_TYPE = "cardType";
    static final String EXTRA_AYYAM_ARRAY = "ayyamArray";
    static final String EXTRA_DOLLARS_ARRAY = "dollarsArray";
    static final int REQUEST_ACTIVITY_CARDS = 200;


    GregorianCalendar calendar, nextCalendar;
    TextView tvDate, tvTotalMoney;
    ImageButton buttonDecrease, buttonIncrease;
    SimpleDateFormat dateFormatter;

    TouchCards touch;
    AlfaCards alfa;

    Cards lastCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        touch = new TouchCards(this);
        alfa = new AlfaCards(this);

        tvDate = (TextView) findViewById(R.id.row_textDescription);
        tvTotalMoney = (TextView) findViewById(R.id.tv_TotalMoney);
        buttonDecrease = (ImageButton) findViewById(R.id.button_DecreaseDate);
        buttonIncrease = (ImageButton) findViewById(R.id.button_IncreaseDate);
        buttonIncrease.setEnabled(false);

        calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        nextCalendar = (GregorianCalendar) calendar.clone();
        nextCalendar.add(Calendar.DAY_OF_YEAR, 1);

        dateFormatter = (SimpleDateFormat) DateFormat.getDateInstance();
        dateFormatter.applyPattern("dd-MM-yyyy");

        tvDate.setText(dateFormatter.format(calendar.getTime()));

        getLoaderManager().initLoader(alfa.ID, null, this);
        getLoaderManager().initLoader(touch.ID, null, this);
        getLoaderManager().initLoader(Cards.CARD_TYPE_ALFAGIFT, null, this);
    }

    public void updateDate(View v) {
        int changeDay = -1;
        if (v.getId() == R.id.button_IncreaseDate) {
            changeDay = 1;
            if ((calendar.get(Calendar.DAY_OF_YEAR) + 1) == (Calendar.getInstance().get(Calendar.DAY_OF_YEAR))) {
                buttonIncrease.setEnabled(false);
            }
        } else {
            buttonIncrease.setEnabled(true);
        }

        calendar.add(Calendar.DAY_OF_YEAR, changeDay);
        nextCalendar.add(Calendar.DAY_OF_YEAR, changeDay);
        tvDate.setText(dateFormatter.format(calendar.getTime()));

        alfa.Clear();
        touch.Clear();

        getLoaderManager().restartLoader(alfa.ID, null, this);
        getLoaderManager().restartLoader(touch.ID, null, this);
        getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFAGIFT, null, this);
    }

    static final String[] Message_SUMMARY_PROJECTION = new String[]{
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
    };


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sender;
        if (id == alfa.ID) {
            sender = "'alfa'";
        } else if (id == touch.ID) {
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == touch.ID) {
            touch.ProcessData(data);
        } else if (loader.getId() == alfa.ID) {
            alfa.ProcessData(data);
        } else if (loader.getId() == Cards.CARD_TYPE_ALFAGIFT) {
            alfa.GiftProcess(data);
        }

        tvTotalMoney.setText(String.valueOf(touch.TotalMoney + alfa.TotalMoney));

    }


    @Override
    public void onLoaderReset(Loader loader) {

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
        if (resultCode == RESULT_OK)
        {
            lastCard.restorePrices();
            lastCard.Clear();
            getLoaderManager().restartLoader(lastCard.ID, null, this);
            if (lastCard.getClass() == AlfaCards.class)
            {
                getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFAGIFT, null, this);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
