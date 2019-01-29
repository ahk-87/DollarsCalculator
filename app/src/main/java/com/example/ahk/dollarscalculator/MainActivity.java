package com.example.ahk.dollarscalculator;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.GoogleApiAvailability;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String EXTRA_CARD_TYPE = "cardType";
    static final String EXTRA_AYYAM_ARRAY = "ayyamArray";
    static final String EXTRA_DOLLARS_ARRAY = "dollarsArray";
    static final int REQUEST_ACTIVITY_CARDS = 200;
    private static final String TAG = "DollarsActivity";


    GregorianCalendar calendar, nextCalendar;
    GregorianCalendar fromDate, toDate;
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

    CalendarView datePicker;
    FrameLayout layout_DatePicker;
    TextView tv_dateTitle, tv_fromDate, tv_toDate;

    int dayIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        cummulative = findViewById(R.id.cb_cumulative);

        tv_touchSentDollars = findViewById(R.id.tv_TouchSent);
        tv_touchReceivedDollars = findViewById(R.id.tv_TouchReceived);
        tv_touchCardsCount = findViewById(R.id.tv_TouchCards);

        tv_alfaSentDollars = findViewById(R.id.tv_AlfaSent);
        tv_alfaReceivedDollars = findViewById(R.id.tv_AlfaReceived);
        tv_alfaCardsCount = findViewById(R.id.tv_AlfaCards);

        tvDate = findViewById(R.id.row_textDescription);
        tvTotalMoney = findViewById(R.id.tv_TotalMoney);
        tv_dateTitle = findViewById(R.id.tv_DatePickerTitle);
        tv_fromDate = findViewById(R.id.tv_fromDate);
        tv_toDate = findViewById(R.id.tv_toDate);

        datePicker = findViewById(R.id.datePicker);
        layout_DatePicker = findViewById(R.id.layout_DatePicker);

        buttonDecrease = findViewById(R.id.button_DecreaseDate);
        buttonDecrease.setOnLongClickListener(new View.OnLongClickListener() {
                                                  @Override
                                                  public boolean onLongClick(View v) {
                                                      showDatePicker(v);
                                                      return true;
                                                  }
                                              }

        );
        buttonIncrease = (ImageButton) findViewById(R.id.button_IncreaseDate);
        buttonIncrease.setOnLongClickListener(new View.OnLongClickListener() {
                                                  @Override
                                                  public boolean onLongClick(View v) {
                                                      showDatePicker(v);
                                                      return true;
                                                  }
                                              }

        );
        //buttonIncrease.setEnabled(false);

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
        calendar.set(Calendar.HOUR_OF_DAY, 1);
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

    int changeDay = 0;

    public void updateDate(View v) {

        if (fromDate != null) {
            showDatePicker(v);
        } else {
            dateChanged = true;
            changeDay = -1;
            if (v.getId() == R.id.button_IncreaseDate) {
                if (dayIndex == 0) {
                    Toast.makeText(this, "Maximum Day reached", Toast.LENGTH_SHORT).show();
                    return;
                }
                dayIndex--;
                changeDay = 1;
            } else {
                dayIndex++;
                buttonDecrease.setEnabled(false);
            }

            calendar.add(Calendar.DAY_OF_YEAR, changeDay);
            nextCalendar.add(Calendar.DAY_OF_YEAR, changeDay);
            tvDate.setText(dateFormatter.format(calendar.getTime()));

            SimpleDateFormat f1 = (SimpleDateFormat) DateFormat.getDateTimeInstance();
            f1.applyPattern("yyyy-MM-dd HH:mm:ss SSS");
            Log.d(TAG, "selectedDate: " + f1.format(calendar.getTime()));
            Log.d(TAG, "selectedDate: " + f1.format(nextCalendar.getTime()));

            if (dayIndex + 1 > dollarsDataList.size()) {
                getLoaderManager().restartLoader(Cards.CARD_TYPE_TOUCH, null, this);
                getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFA, null, this);
                getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFAGIFT, null, this);
            } else {
                showData(dayIndex, cummulative.isChecked(), null);
            }
        }
    }

    void showDatePicker(View button) {
        if (button == buttonIncrease) {
            tv_dateTitle.setText("To Date");

            datePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
            if (fromDate != null)
                datePicker.setMinDate(fromDate.getTimeInMillis());
            else
                datePicker.setMinDate(Calendar.getInstance().getTimeInMillis() - DateUtils.YEAR_IN_MILLIS);
        } else {
            tv_dateTitle.setText("From Date");
            datePicker.setMinDate(Calendar.getInstance().getTimeInMillis() - DateUtils.YEAR_IN_MILLIS);
            if (toDate != null)
                datePicker.setMaxDate(toDate.getTimeInMillis() - 1000);
            else
                datePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
        }

        datePicker.setTag(button);
        layout_DatePicker.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (layout_DatePicker.getVisibility() == View.VISIBLE)
            layout_DatePicker.setVisibility(View.GONE);
        else
            super.onBackPressed();
    }

    public void layoutButtonClick(View v) {
        if (v.getId() == R.id.button_Ok) {

            // Invoke the OK button method
            SimpleDateFormat f1 = (SimpleDateFormat) DateFormat.getDateTimeInstance();
            f1.applyPattern("yyyy-MM-dd HH:mm:ss SSS");
            GregorianCalendar selectedDate = new GregorianCalendar();
            selectedDate.setTime(new Date(datePicker.getDate()));
            selectedDate.set(Calendar.HOUR_OF_DAY, 23);
            selectedDate.set(Calendar.MINUTE, 59);
            selectedDate.set(Calendar.SECOND, 59);
            selectedDate.set(Calendar.MILLISECOND, 999);
            Log.d(TAG, "selectedDate: " + f1.format(selectedDate.getTime()));

            if (datePicker.getTag().equals(buttonDecrease)) {
                // Handle "from" date action
                fromDate = (GregorianCalendar) selectedDate.clone();
                fromDate.add(Calendar.SECOND, 1);
                if (toDate == null) {
                    toDate = (GregorianCalendar) fromDate.clone();
                }
                fromDate.add(Calendar.DAY_OF_YEAR, -1);
                // If this day of daylight saving, edit the fromDate
                if (fromDate.get(Calendar.HOUR_OF_DAY) == 23)
                    fromDate.add(Calendar.HOUR_OF_DAY, 1);
            } else {
                // Handle "to" date action
                toDate = (GregorianCalendar) selectedDate.clone();
                if (fromDate == null) {
                    // If fromDate is null, then make it the first day of the same month of toDate
                    fromDate = (GregorianCalendar) toDate.clone();
                    fromDate.set(Calendar.DAY_OF_MONTH, 1);
                    fromDate.add(Calendar.SECOND, 1);
                    fromDate.add(Calendar.DAY_OF_YEAR, -1);
                }
                toDate.add(Calendar.SECOND, 1);
            }

            Log.d(TAG, "fromDate: " + f1.format(fromDate.getTime()));
            Log.d(TAG, "toDate: " + f1.format(toDate.getTime()));
            GregorianCalendar toDateText = ((GregorianCalendar) toDate.clone());
            toDateText.add(Calendar.SECOND, -1);
            tv_fromDate.setText(dateFormatter.format(fromDate.getTime()));
            tv_toDate.setText(dateFormatter.format(toDateText.getTime()));

            tvDate.setTextColor(Color.RED);
            tvDate.setTag(false);

            getLoaderManager().restartLoader(Cards.CARD_TYPE_TOUCH, null, this);
            getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFA, null, this);
            getLoaderManager().restartLoader(Cards.CARD_TYPE_ALFAGIFT, null, this);

        }

        layout_DatePicker.setVisibility(View.GONE);
    }

    public void dateTitleClick(View v) {
        if (tvDate.getTag() != null && !(boolean) tvDate.getTag()) {
            tvDate.setTextColor(Color.BLACK);
            tvDate.setTag(true);
            fromDate = null;
            toDate = null;
            tv_fromDate.setText("");
            tv_toDate.setText("");
            showData(dayIndex, cummulative.isChecked(), null);
        }
    }

    static final String[] Message_SUMMARY_PROJECTION = new String[]{
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
    };

    public void checkChanged(View v) {
        if (tvDate.getTag() == null || (boolean) tvDate.getTag())
            showData(dayIndex, cummulative.isChecked(), null);
    }


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

        GregorianCalendar fromCalendar, toCalendar;
        fromCalendar = fromDate == null ? calendar : fromDate;
        toCalendar = toDate == null ? nextCalendar : toDate;
        String select = "(" + Telephony.Sms.Inbox.ADDRESS + "=" + sender + " AND " +
                Telephony.Sms.Inbox.DATE + ">" + fromCalendar.getTimeInMillis() + " AND " +
                Telephony.Sms.Inbox.DATE + "<" + toCalendar.getTimeInMillis() + ")";
        return new CursorLoader(this, Telephony.Sms.Inbox.CONTENT_URI,
                Message_SUMMARY_PROJECTION, select, null,
                Telephony.Sms.Inbox.DATE + " ASC");
    }

    int loaderRounds = 3;
    boolean dateChanged = false;

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
            if (fromDate != null) {
                showData(0, false, new DollarsData(touch, alfa));

            } else if (dateChanged || dollarsDataList.isEmpty()) {
                dollarsDataList.add(new DollarsData(touch, alfa));
                showData(dayIndex, cummulative.isChecked(), null);
                //calendar.add(Calendar.DAY_OF_YEAR, -changeDay * dayIndex);
                //nextCalendar.add(Calendar.DAY_OF_YEAR, -changeDay * dayIndex);
            } else {
                if (!buttonIncrease.isEnabled()) {
                    dollarsDataList.remove(0);
                    dollarsDataList.add(0, new DollarsData(touch, alfa));
                }
                showData(dayIndex, cummulative.isChecked(), null);
            }
            loaderRounds = 3;
        }

    }


    @Override
    public void onLoaderReset(Loader loader) {

    }

    void showData(int index, boolean isCumulative, DollarsData data) {
        if (data != null) {
        } else if (isCumulative) {
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
        dateChanged = false;
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
