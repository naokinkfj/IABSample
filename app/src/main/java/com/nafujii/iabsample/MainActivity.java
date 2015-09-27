package com.nafujii.iabsample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * http://developer.android.com/google/play/billing/billing_integrate.html
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int BILLING_RESPONSE_RESULT_OK = 0;

    private String mPremium1MonthPrice = "";
    private String mPremium1MonthTitle = "";

    private TextView mPremium1MonthPriceView;
    private TextView mPremium1MonthTitleView;
    private Button mBuyButton;
    private View.OnClickListener mBuyButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private static final String PRODUCT_ID_PREMIUM_1MONTH = "premium_1month";
    private static final String[] SKU_LIST = {
            PRODUCT_ID_PREMIUM_1MONTH,
    };

    private IInAppBillingService mService;
    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

            new Thread(new Runnable() {
                @Override
                public void run() {

                }
            });

            try {
                ArrayList<String> skuList = new ArrayList<>(Arrays.asList(SKU_LIST));
                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                int apiVersion = 3;
                String type = "subs"; // 購読の場合は"subs", 消費アイテムは"inapp"
                Bundle skuDetails = mService.getSkuDetails(apiVersion, getPackageName(), type, querySkus);

                int response = skuDetails.getInt("RESPONSE_CODE");
                if (response == BILLING_RESPONSE_RESULT_OK) {
                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                    if (responseList != null) {
                        for (String thisResponse : responseList) {
                            JSONObject object = new JSONObject(thisResponse);
                            String sku = object.getString("productId");
                            if (sku.equals(PRODUCT_ID_PREMIUM_1MONTH)) {
                                String description = object.getString("description");

                                mPremium1MonthPrice = object.getString("price");
                                mPremium1MonthTitle = object.getString("title");

                                mPremium1MonthPriceView.setText(mPremium1MonthPrice);
                                mPremium1MonthTitleView.setText(mPremium1MonthTitle);
                            }
                        }
                    }
                }
            } catch (RemoteException | JSONException e) {
                Log.e(TAG, "error", e);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBuyButton = (Button)findViewById(R.id.buy_button);
        mBuyButton.setOnClickListener(mBuyButtonListener);
        mBuyButton.setEnabled(false);

        mPremium1MonthPriceView = (TextView)findViewById(R.id.premium_price_text);
        mPremium1MonthTitleView = (TextView)findViewById(R.id.premium_title_text);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        // To protect the security of billing transactions, always make sure to explicitly set the intent's target package name to com.android.vending, using setPackage()
        serviceIntent.setPackage("com.android.vending");
        getApplicationContext().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * Remember to unbind from the In-app Billing service when you are done with your Activity.
     * If you don’t unbind, the open service connection could cause your device’s performance to degrade.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
