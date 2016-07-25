package com.sensorberg.sdk.demo;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergServiceMessage;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.InAppAction;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.testApp.BuildConfig;
import com.sensorberg.utils.LatestBeacons;

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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.sensorberg.utils.ListUtils.distinct;
import static com.sensorberg.utils.ListUtils.map;

@SuppressWarnings("javadoc")
public class DemoActivity extends Activity {

    private static final String EXTRA_ACTION = "com.sensorberg.demoActivity.extras.ACTION";

    public static final UUID BEACON_PROXIMITY_ID = UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5");

    private static final int MY_PERMISSION_REQUEST_LOCATION_SERVICES = 1;

    private Clock clock;

    private UUID uuid = UUID.fromString("6133172D-935F-437F-B932-A901265C24B0");

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

        BeaconEvent beaconEvent = new BeaconEvent.Builder()
                .withAction(new InAppAction(uuid, null, null, null, null, 0))
                .withPresentationTime(1337)
                .withTrigger(ScanEventType.ENTRY.getMask())
                .build();
        beaconEvent.setBeaconId(new BeaconId(BEACON_PROXIMITY_ID, 1337, 1337));
        clock = new Clock() {
            @Override
            public long now() {
                return 0;
            }

            @Override
            public long elapsedRealtime() {
                return 0;
            }
        };

        textView = new TextView(this);
        StringBuilder infoText = new StringBuilder("This is an app that exposes some SDK APIs to the user").append('\n');
        infoText.append('\n').append("sentToServerTimestamp2: ").append(list2.get(0).getSentToServerTimestamp2());

        if (Build.VERSION.SDK_INT < 18){
            infoText.append('\n').append("BLE NOT SUPPORTED, NO BEACONS WILL BE SCANNED").append('\n');
        }

        infoText.append('\n').append("API Key: ").append(DemoApplication.API_KEY);
        infoText.append('\n').append("SDK Version: ").append(com.sensorberg.sdk.BuildConfig.VERSION_NAME);
        infoText.append('\n').append("Demo Version: ").append(BuildConfig.VERSION_NAME);
        textView.setText(infoText.toString());


        Button button = new Button(this);
        button.setText("click me");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        {
                            long before = System.currentTimeMillis();
                            Collection<BeaconId> beacons = LatestBeacons.getLatestBeacons(getApplicationContext(),
                                    5, TimeUnit.MINUTES);
                            StringBuilder beaconIds = new StringBuilder("got these from the other process: ");
                            for (BeaconId beacon : beacons) {
                                beaconIds.append(beacon.getBid()).append(",");
                            }
                            beaconIds.append(" beacons");
                            beaconIds.append(" took ").append(System.currentTimeMillis() - before).append("ms");
                            Logger.log.verbose(beaconIds.toString());
                        }
                        {
                            long before = System.currentTimeMillis();
                            Collection<BeaconId> beacons = getLatestBeaconsInMyProcess(5, TimeUnit.MINUTES);
                            StringBuilder beaconIds = new StringBuilder("got these in my process: ");
                            for (BeaconId beacon : beacons) {
                                beaconIds.append(beacon.getBid()).append(",");
                            }
                            beaconIds.append(" beacons");
                            beaconIds.append(" took ").append(System.currentTimeMillis() - before).append("ms");
                            Logger.log.verbose(beaconIds.toString());
                        }
                        return null;
                    }
                };
                task.execute();
            }
        });

       setContentView(button);
        ((DemoApplication) getApplication()).setActivityContext(this);
        processIntent(getIntent());
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
                    Log.d("Scanner Message", "fine grained location permission granted");
                    ((DemoApplication) getApplication()).setLocationPermissionGranted(SensorbergServiceMessage.MSG_LOCATION_SET);
                } else {
                    ((DemoApplication) getApplication()).setLocationPermissionGranted(SensorbergServiceMessage.MSG_LOCATION_NOT_SET_WHEN_NEEDED);
                }
            }
        }
    }

    //TODO here.
    /**
     * this method is only here for a speed reference.
     */
    public static Collection<BeaconId> getLatestBeaconsInMyProcess(long duration, TimeUnit unit){
        long now = System.currentTimeMillis() - unit.toMillis(duration);
        return  distinct(map(
                BeaconActionHistoryPublisher.latestEnterEvents(now),
                BeaconId.FROM_BEACON_SCAN));
    }
}
