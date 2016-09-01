package com.sensorberg.sdk.demo;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import com.sensorberg.SensorbergSdk;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.testApp.BuildConfig;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

@SuppressWarnings("javadoc")
public class DemoActivity extends Activity {

    private static final String EXTRA_ACTION = "com.sensorberg.demoActivity.extras.ACTION";

    private static final int MY_PERMISSION_REQUEST_LOCATION_SERVICES = 1;

    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, " +
                        "this app will not be able to discover beacons when in the background.");
                builder.setPositiveButton(android.R.string.ok, null);

                if (Build.VERSION.SDK_INT >= 17) {
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(DemoActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSION_REQUEST_LOCATION_SERVICES);
                        }

                    });
                }

                builder.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_LOCATION_SERVICES);
            }
        }

        textView = new TextView(this);
        StringBuilder infoText = new StringBuilder("This is an app that exposes some SDK APIs to the user").append('\n');

        if (Build.VERSION.SDK_INT < 18){
            infoText.append('\n').append("BLE NOT SUPPORTED, NO BEACONS WILL BE SCANNED").append('\n');
        }

        infoText.append('\n').append("API Key: ").append(DemoApplication.API_KEY);
        infoText.append('\n').append("SDK Version: ").append(com.sensorberg.sdk.BuildConfig.VERSION_NAME);
        infoText.append('\n').append("Demo Version: ").append(BuildConfig.VERSION_NAME);
        textView.setText(infoText.toString());
        setContentView(textView);
        ((DemoApplication) getApplication()).setActivityContext(this);
        processIntent(getIntent());

        AsyncTask<String, Integer, Pair<String, Long>> task = new AsyncTask<String, Integer, Pair<String, Long>>() {
            @Override
            protected Pair<String, Long> doInBackground(String... params) {
                long timeBefore = System.currentTimeMillis();
                String advertiserIdentifier = "not-found";

                try {
                    advertiserIdentifier = "google:" + AdvertisingIdClient.getAdvertisingIdInfo(DemoActivity.this).getId();
                } catch (Exception e) {
                    //not logging anything because it's already logged in the Application
                }

                long timeItTook = System.currentTimeMillis() - timeBefore;
                Logger.log.verbose("foreground fetching the advertising identifier took " + timeItTook + " millis");
                return Pair.create(advertiserIdentifier, timeItTook);
            }

            @Override
            protected void onPostExecute(Pair<String, Long> o) {
                textView.append("\nGoogle Advertising ID: " + o.first);
                textView.append("\nGoogle ID took: " + o.second + " milliseconds");
            }
        };

        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((DemoApplication) getApplication()).setActivityContext(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((DemoApplication) getApplication()).setActivityContext(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent != null) {
            Action action = intent.getParcelableExtra(EXTRA_ACTION);
            if (action != null) {
                DemoApplication application = (DemoApplication) getApplication();
                application.showAlert(action, null);
            }
        }
    }

    public static Intent getIntent(Context context, Action action) {
        Intent intent = new Intent(context, DemoActivity.class);
        intent.putExtra(EXTRA_ACTION, action);
        return intent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION_SERVICES: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SensorbergSdk.sendLocationFlagToReceiver(SensorbergServiceMessage.MSG_LOCATION_SET);
                } else {
                    SensorbergSdk.sendLocationFlagToReceiver(SensorbergServiceMessage.MSG_LOCATION_NOT_SET_WHEN_NEEDED);
                }
            }
        }
    }
}
