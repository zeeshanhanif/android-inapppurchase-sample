package demo.com.inapppurchasedemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import demo.com.inapppurchasedemo.util.IabHelper;
import demo.com.inapppurchasedemo.util.IabResult;
import demo.com.inapppurchasedemo.util.Inventory;
import demo.com.inapppurchasedemo.util.Purchase;


public class MainActivity extends ActionBarActivity {

    private static final String TAG =
            "demo.com.inapppurchasedemo.MainActivity";
    private Button clickBtn;
    private Button buyBtn;


    private IabHelper mIabHelper;
    private IabHelper.OnIabPurchaseFinishedListener mIabPurchaseFinishedListener;
    private IabHelper.QueryInventoryFinishedListener mQueryInventoryFinishedListener;
    private IabHelper.OnConsumeFinishedListener mOnConsumeFinishedListener;
    static final String ITEM_SKU = "android.test.purchased";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clickBtn = (Button)findViewById(R.id.button);
        buyBtn = (Button)findViewById(R.id.button2);
        clickBtn.setEnabled(false);

        settingPlayBilling();
        registerEvents();

    }

    private void settingPlayBilling(){
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlDatX1VRSHLgwCu96vzSexqgBjNvvsxXfkydGAyPerO1m+GJK7+DB63G3PNe8ugcF5gYp2eJg4UEfnAf+drVG7yUHCQInUU6u496jfeYVaobvTC1MmkNjXfqtJknRzipKQY86E1pdXOtIGik9HVFXj7KD+MXCd5leScHl7JfRcpfKW2T7Mc4QakvgBgqG6tuUBF5dGtpwGZZ9JUM4b1KtkBCYH7Dj3HLjOXExusGlThEiDaFzWm9IUVK+1DWaP11vY0gcZJYX9d4n5C1wSfbDx5hLHtNCuUUCwoKa2hRx55QA/nn8ub8eIH+tCLW6qy9rGNcczeEkktjqy8C0qdMdQIDAQAB";
        mIabHelper = new IabHelper(this,base64EncodedPublicKey);

        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " +
                            result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                    //consumeItem();
                }
            }
        });

        mIabPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if(result.isFailure()){
                    return ;
                }
                else if(purchase.getSku().equals(ITEM_SKU)) {
                    consumeItem();
                    buyBtn.setEnabled(false);

                }
            }
        };

        mQueryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                if(result.isFailure()){
                    // Handle failure
                }
                else {
                    mIabHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),mOnConsumeFinishedListener);
                }

            }
        };

        mOnConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                if(result.isSuccess()){
                    clickBtn.setEnabled(true);
                }
                else{
                   // Handel Error
                }
            }
        };

    }

    private void consumeItem(){
        mIabHelper.queryInventoryAsync(mQueryInventoryFinishedListener);
    }

    private void buttonClicked(){
        clickBtn.setEnabled(false);
        buyBtn.setEnabled(true);
    }

    private void registerEvents(){
        clickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click Event", Toast.LENGTH_SHORT).show();
            }
        });

        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Buy", Toast.LENGTH_SHORT).show();
                mIabHelper.launchPurchaseFlow(MainActivity.this,ITEM_SKU,10001,mIabPurchaseFinishedListener,"mypurchasetoken");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(!mIabHelper.handleActivityResult(requestCode,resultCode,data)){
            super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mIabHelper != null){
            mIabHelper.dispose();
            mIabHelper = null;
        }
    }
}
