package icechen1.com.blackbox.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icechen1.com.blackbox.AppConstants;
import icechen1.com.blackbox.R;
import icechen1.com.blackbox.SecureConstants;
import icechen1.com.blackbox.common.AppUtils;

public class PremiumActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    @Bind(R.id.premiumDescriptionText)
    public TextView mPremiumDescriptionText;

    @Bind(R.id.purchase_btn)
    public Button mPurchaseBtn;

    private BillingProcessor mBillingProcessor;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
        if(!isAvailable) {
            // show error
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_iap_service)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        } else {
            mBillingProcessor = new BillingProcessor(this, SecureConstants.PUB_KEY, this);
            mBillingProcessor.loadOwnedPurchasesFromGoogle();
        }

        if(AppUtils.isPremium(this)) {
            mPremiumDescriptionText.setText(getString(R.string.thank_you_premium));
            mPurchaseBtn.setVisibility(View.GONE);
        }
        mBillingProcessor.consumePurchase(AppConstants.APP_IAP_ID);
    }

    @OnClick(R.id.purchase_btn)
    public void purchase() {
        mBillingProcessor.purchase(this, AppConstants.APP_IAP_ID);
    }

    private void savePurchase() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mPrefs.edit().putBoolean(AppConstants.IAP_PREF, true).apply();
        mPremiumDescriptionText.setText(getString(R.string.thank_you_premium));
        mPurchaseBtn.setVisibility(View.GONE);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if(mBillingProcessor.isValidTransactionDetails(details) && productId.equals(AppConstants.APP_IAP_ID)) {
            savePurchase();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if(mBillingProcessor.isPurchased(AppConstants.APP_IAP_ID)) {
            savePurchase();
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }
}
