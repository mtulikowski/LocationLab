package course.labs.locationlab;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceViewActivity extends ListActivity implements LocationListener {
    private static final long FIVE_MINS = 5 * 60 * 1000;

    private static String TAG = "Lab-Location";

    private Location mLastLocationReading;
    private PlaceViewAdapter mAdapter;

    // default minimum time between new readings
    private long mMinTime = 5000;

    // default minimum distance between old and new readings.
    private float mMinDistance = 1000.0f;

    private LocationManager mLocationManager;

    // A fake location provider used for testing
    private MockLocationProvider mMockLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new PlaceViewAdapter(getApplicationContext());

        getListView().setFooterDividersEnabled(true);

        TextView footerView = (TextView) getLayoutInflater().inflate(R.layout.footer_view, null);

        getListView().addFooterView(footerView);

        assert footerView != null;
        footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                log("Entered footerView.OnClickListener.onClick()");

                mLastLocationReading = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if(mLastLocationReading == null) {
                    log("Location data is not available");
                } else {
                    if(mAdapter.intersects(mLastLocationReading)) {
                        //location viewed
                        log("You already have this location badge");
                        Toast.makeText(getApplicationContext(), "You already have this location badge", Toast.LENGTH_SHORT).show();
                    } else {
                        //location new
                        log("Starting Place Download");
                        new PlaceDownloaderTask(PlaceViewActivity.this).execute(mLastLocationReading);
//                        PlaceRecord placeRecord = placeDownloaderTask.doInBackground(mLastLocationReading);
//                        placeDownloaderTask.onPostExecute(placeRecord);
                    }
                }
            }
        });

        setListAdapter(mAdapter);

        // footerView must respond to user clicks.
        // Must handle 3 cases:
        // 1) The current location is new - download new Place Badge. Issue the
        // following log call:
        // log("Starting Place Download");

        // 2) The current location has been seen before - issue Toast message.
        // Issue the following log call:
        // log("You already have this location badge");


        // 3) There is no current location - response is up to you. The best
        // solution is to disable the footerView until you have a location.
        // Issue the following log call:
        // log("Location data is not available");


    }

    @Override
    protected void onResume() {
        super.onResume();

        mMockLocationProvider = new MockLocationProvider(
                LocationManager.NETWORK_PROVIDER, this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if( mLastLocationReading != null && age(mLastLocationReading) > FIVE_MINS ) {
            mLastLocationReading = null;
        }

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,mMinTime,mMinDistance,this);
    }

    @Override
    protected void onPause() {

        mMockLocationProvider.shutdown();
        mLocationManager.removeUpdates(this);

        super.onPause();
    }

    // Callback method used by PlaceDownloaderTask
    public void addNewPlace(PlaceRecord place) {

        log("Entered addNewPlace()");
        mAdapter.add(place);

    }

    @Override
    public void onLocationChanged(Location currentLocation) {

        if(mLastLocationReading == null) {
            mLastLocationReading = currentLocation;
        }

        if( currentLocation.getTime() > mLastLocationReading.getTime() ) {
            mLastLocationReading = currentLocation;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // not implemented
    }

    @Override
    public void onProviderEnabled(String provider) {
        // not implemented
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // not implemented
    }

    private long age(Location location) {
        return System.currentTimeMillis() - location.getTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.print_badges:
                ArrayList<PlaceRecord> currData = mAdapter.getList();
                for (PlaceRecord aCurrData : currData) {
                    log(aCurrData.toString());
                }
                return true;
            case R.id.delete_badges:
                mAdapter.removeAllViews();
                return true;
            case R.id.place_one:
                mMockLocationProvider.pushLocation(37.422, -122.084);
                return true;
            case R.id.place_invalid:
                mMockLocationProvider.pushLocation(0, 0);
                return true;
            case R.id.place_two:
                mMockLocationProvider.pushLocation(38.996667, -76.9275);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static void log(String msg) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, msg);
    }

}