package com.example.ahk.dollarscalculator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by AHK on 07-Dec-17.
 */

public class PricesActivity extends Activity implements AdapterView.OnItemClickListener {


    private static final int ADAPTER_TYPE_AYYAM = 10;
    private static final int ADAPTER_TYPE_DOLLARS = 20;

    PricesAdapter adapterAyyam;
    PricesAdapter adapterDollars;

    ListView listAyyam;
    ListView listDollars;

    int[] ayyamPrices;
    int[] dollarsPrices;

    int offer;

    String cardType;

    Button button_add, button_substract;
    MenuItem menuItemSave;

    TextView tv_offer;

    class SelectedItem {
        int adapterType;
        int position;
        TextView viewPice;
    }

    SelectedItem lastSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activiy_prices);

        button_add = (Button) findViewById(R.id.button_add);
        button_substract = (Button) findViewById(R.id.button_substract);
        tv_offer = findViewById(R.id.tv_offer);

        cardType = getIntent().getStringExtra(MainActivity.EXTRA_CARD_TYPE);
        ayyamPrices = getIntent().getIntArrayExtra(MainActivity.EXTRA_AYYAM_ARRAY);
        dollarsPrices = getIntent().getIntArrayExtra(MainActivity.EXTRA_DOLLARS_ARRAY);
        offer = getIntent().getIntExtra(MainActivity.EXTRA_OFFER, -1);

        if (offer == -1)
            tv_offer.setText("");
        else if (offer == 0)
            tv_offer.setText("no offer");
        else
            tv_offer.setText("offer");

            TextView textViewHeader = (TextView) findViewById(R.id.tv_MainHeader);
        if (cardType.equals("alfa")) {
            textViewHeader.setText("ALFA");
            textViewHeader.setTextColor(0xFFFF0000);
        } else {
            textViewHeader.setText("TOUCH");
            textViewHeader.setTextColor(0xFF0000FF);
        }

        adapterAyyam = new PricesAdapter(this, ADAPTER_TYPE_AYYAM, ayyamPrices);
        adapterDollars = new PricesAdapter(this, ADAPTER_TYPE_DOLLARS, dollarsPrices);

        listAyyam = (ListView) findViewById(R.id.list_ayyam);
        listAyyam.setAdapter(adapterAyyam);
        listAyyam.setTag(ADAPTER_TYPE_AYYAM);
        listAyyam.setOnItemClickListener(this);

        listDollars = (ListView) findViewById(R.id.list_dollars);
        listDollars.setAdapter(adapterDollars);
        listDollars.setTag(ADAPTER_TYPE_DOLLARS);
        listDollars.setOnItemClickListener(this);

        lastSelected = new SelectedItem();
        lastSelected.adapterType = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuItemSave = menu.add("Save");
        menuItemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItemSave.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor edit = getSharedPreferences(cardType + Cards.SHARED_PREF, MODE_PRIVATE).edit();
        String d = Arrays.toString(dollarsPrices);

        edit.putString(Cards.SHARED_DOLLARS, d);
        for (int i = 1; i < 10; i++) {
            ayyamPrices[i + 10] = ayyamPrices[i] + dollarsPrices[10];
        }
        edit.putString(Cards.SHARED_AYYAM, Arrays.toString(ayyamPrices));
        edit.commit();
        setResult(RESULT_OK);
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int selectedAdapter = (int) parent.getTag();
        int pos = position;
        int scrollAyyam = 1, scrollDollars = 1;

        if (listAyyam.canScrollVertically(-1)) scrollAyyam = -1;
        if (listDollars.canScrollVertically(-1)) scrollDollars = -1;

        button_substract.setEnabled(true);
        button_add.setEnabled(true);

        if (lastSelected.adapterType == 0) {
            ((ListView) parent).setSelector(android.R.color.holo_blue_light);
        } else if (lastSelected.adapterType != selectedAdapter) {
            if (lastSelected.adapterType == ADAPTER_TYPE_AYYAM) {
                listAyyam.setSelector(android.R.color.transparent);
                listAyyam.smoothScrollBy(scrollAyyam, 1);
                listDollars.setSelector(android.R.color.holo_blue_light);
                listDollars.smoothScrollBy(scrollDollars, 1);
            } else {
                listDollars.setSelector(android.R.color.transparent);
                listDollars.smoothScrollBy(scrollDollars, 1);
                listAyyam.setSelector(android.R.color.holo_blue_light);
                listAyyam.smoothScrollBy(scrollAyyam, 1);
            }
        }

        lastSelected.adapterType = selectedAdapter;
        if (selectedAdapter == ADAPTER_TYPE_DOLLARS)
            pos++;

        lastSelected.position = pos;
        lastSelected.viewPice = (TextView) view.findViewById(R.id.row_textPrice);
    }

    public void changerPrice(View v) {
        int change = 500;
        int[] prices = dollarsPrices;

        if (v.getId() == R.id.button_substract)
            change = -500;

        if (lastSelected.adapterType == ADAPTER_TYPE_AYYAM)
            prices = ayyamPrices;

        if (change == 500) {
            prices[lastSelected.position] += change;
        } else if (prices[lastSelected.position] > 500)
            prices[lastSelected.position] += change;

        lastSelected.viewPice.setText(String.valueOf(prices[lastSelected.position]));

        menuItemSave.setVisible(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    class PricesAdapter extends BaseAdapter {

        LayoutInflater inflater;
        int[] prices;
        int adapterType;

        class ViewHolder {
            TextView textDescription;
            TextView textPrice;
        }


        public PricesAdapter(Context c, int pricesType, int[] p) {
            inflater = LayoutInflater.from(c);
            adapterType = pricesType;
            prices = p;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_price, null);

                holder = new ViewHolder();
                holder.textDescription = (TextView) convertView.findViewById(R.id.row_textDescription);
                holder.textPrice = (TextView) convertView.findViewById(R.id.row_textPrice);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int pos = position;
            if (adapterType == ADAPTER_TYPE_DOLLARS) {
                pos++;
                holder.textDescription.setText(String.valueOf(pos) + " $");
            } else if (adapterType == ADAPTER_TYPE_AYYAM) {
                holder.textDescription.setText("(10+" + String.valueOf(10 - pos) + ")");
            }
            holder.textPrice.setText(String.valueOf(prices[pos]));
            return convertView;
        }

        @Override
        public int getCount() {
            if (adapterType == ADAPTER_TYPE_AYYAM)
                return 11;
            else
                return 10;
        }

        @Override
        public Object getItem(int position) {
            return prices[position];
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


    }
}
