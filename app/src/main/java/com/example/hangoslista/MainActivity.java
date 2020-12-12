package com.example.hangoslista;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView titleText, welcomeText;
    private View viewRoot;
    private Button sortItemButton;

    //Locale
    Locale HUNGARY = new Locale("hu", "HU");

    //Activities
    private static final int USER_ACT_CODE = 101;
    private static final int ITEM_ACT_CODE = 102;

    //SharedPref
    private SharedPreferences sharedPref;
    private String actualUserName;

    //Sorting
    ListItemHelper.SortState sortState;

    //Sensors
    private SensorManager mSensorManager;
    private Sensor proxySensor;
    private Sensor AccelSensor;
    private SensorEventListener proxyEventListener;
    private static final int SENSOR_SENSITIVITY = 2;
    private static final float SHAKE_THRESHOLD = 3.25f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;

    //ROOM
    private ListDataBase soundListDataBase = null;
    private RecyclerView listRecycleView;
    private ViewAdapter actualAdapter;
    private class DataBaseObserver implements Observer<List<ListItem>> {
        @Override
        public void onChanged(List<ListItem> listItems) {
            actualAdapter = new ViewAdapter(listItems);
            listRecycleView.setAdapter(actualAdapter);
        }
    };

    //TTS
    public TextToSpeech helpingTTS;
    public boolean TTSisAvailable = false, TTSisActivated = false;
    public float TTSpitch = 0.85f;
    public float TTSspeed = 0.75f;

    //Broadcast
    BroadcastReceiver mCustomReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if(intentAction != null && intentAction == ViewAdapter.ACTION_CUSTOM_BROADCAST){
                ListItem item = new ListItem();

                item.setItemId(intent.getIntExtra(ViewAdapter.BROADCAST_EXTRA_ID,1));
                item.setItemName(intent.getStringExtra(ViewAdapter.BROADCAST_EXTRA_NAME));
                item.setItemPrice(intent.getIntExtra(ViewAdapter.BROADCAST_EXTRA_PRICE,-1));

                TTSitem(item);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewRoot = findViewById(R.id.root);

        titleText = findViewById(R.id.titleText);
        welcomeText = findViewById(R.id.welcomeText);
        sortItemButton = findViewById(R.id.sortItemButton);
        sortState = ListItemHelper.SortState.BY_ID;
        actualUserName = getString(R.string.defaultUser);
        updateWelcomeStr(actualUserName);

        //Locale
        final Locale currentLocale = getResources().getConfiguration().locale;

        //Sensor
        mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        proxySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        AccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //AccelData
        mLastShakeTime = System.currentTimeMillis();
        //Proxy
        proxyEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if (TTSisActivated && event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                        //near
                        helpingTTS.stop();
                        TTSisActivated = false;
                        Toast.makeText(getApplicationContext(), getString(R.string.tts_quiet), Toast.LENGTH_SHORT).show();
                    }
                    //else work normally
                }
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    long curTime = System.currentTimeMillis();
                    if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];
                        double acceleration = Math.sqrt(Math.pow(x, 2) +
                                Math.pow(y, 2) +
                                Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                        if (acceleration > SHAKE_THRESHOLD) {
                            mLastShakeTime = curTime;
                            //Change Sorting Type
                            sortState = sortState.next();
                            sortItem(sortItemButton);
                            TTSReadSortState(sortState);
                        }
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {    }
        };
        mSensorManager.registerListener(proxyEventListener, proxySensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(proxyEventListener, AccelSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //ROOM
        soundListDataBase = Room.databaseBuilder(this, ListDataBase.class, "soundList_db")
                .fallbackToDestructiveMigration().build();
        listRecycleView = (RecyclerView) findViewById(R.id.listRecycleView);
        listRecycleView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        final @NonNull LifecycleOwner thisOwner = this;

        //Get the viewed data, and observe
        getLiveData(sortState, actualUserName).observe(this, new DataBaseObserver());

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //Code on moving
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //Code on swiping
                int pos = viewHolder.getAdapterPosition();
                final ListItem myItem = ((ViewAdapter) listRecycleView.getAdapter()).getItemAtPosition(pos);
                //Toast.makeText(MainActivity.this, "Deleting " + myItem.getItemName(), Toast.LENGTH_LONG).show();

                //Delete the item
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Delete from DataBase
                        soundListDataBase.soundListDAO().deleteFromList(myItem);
                    }
                }).start();

                //Refresh the list
                //getLiveData(sortState, actualUserName).removeObservers(thisOwner);
                //getLiveData(sortState, actualUserName).observe(thisOwner, new DataBaseObserver());
            }
        });
        //itemTouchHelper class connection to listRecycleView
        itemTouchHelper.attachToRecyclerView(listRecycleView);

        //TTS
        helpingTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int res = 0;
                    if(currentLocale.equals(HUNGARY)){
                        res = helpingTTS.setLanguage(HUNGARY);
                    }else{
                        res = helpingTTS.setLanguage(Locale.ENGLISH);
                    }
                    if(res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported.");
                        Toast.makeText(getApplicationContext(), "TTS: Language not supported.", Toast.LENGTH_SHORT).show();
                    }else{
                        TTSisAvailable = true;
                    }
                }else{
                    Log.e("TTS", "Init failed.");
                    Toast.makeText(getApplicationContext(), " TTS: Init failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(mCustomReceiver, new IntentFilter(ViewAdapter.ACTION_CUSTOM_BROADCAST));

        //Choose user
        //Intent intent = new Intent(this, ChooseUserActivity.class);
        //startActivityForResult(intent, USER_ACT_CODE);
    }

    //Sensor
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(proxyEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(proxyEventListener, proxySensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(proxyEventListener, AccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Activity Caller
    public void changeUser(View view) {
        Intent intent = new Intent(this, ChooseUserActivity.class);
        startActivityForResult(intent, USER_ACT_CODE);
    }

    public void openAddItem(View view) {
        //Open the new Item adder Activity
        Intent intent = new Intent(this, ItemActivity_.class);
        startActivityForResult(intent, ITEM_ACT_CODE);
    }

    //Sorting Caller
    public void sortItem(View view) {
        //Remove previous observers
        getLiveData(sortState, actualUserName).removeObservers(this);

        //Next state
        switch (sortState){
            case BY_ID:
                Toast.makeText(getApplicationContext(), getString(R.string.tts_time), Toast.LENGTH_SHORT).show();
                break;
            case BY_NAME:
                Toast.makeText(getApplicationContext(), getString(R.string.tts_name), Toast.LENGTH_SHORT).show();
                break;
            case BY_PRICE:
                Toast.makeText(getApplicationContext(), getString(R.string.tts_price), Toast.LENGTH_SHORT).show();
                break;
        }

        //Get the viewed data, and observe
        getLiveData(sortState, actualUserName).observe(this, new DataBaseObserver());

    }

    //Activity return handler
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            //Remove previous observers
            getLiveData(sortState, actualUserName).removeObservers(this);

            if(requestCode == USER_ACT_CODE){
                //Update the username
                final String name = data.getStringExtra(ChooseUserActivity.INTENTRESULT);

                //Save the username and update the messages
                sortState = ListItemHelper.SortState.BY_ID;
                actualUserName = name;
                updateWelcomeStr(name);
            }if(requestCode == ITEM_ACT_CODE){
                String itemName = data.getStringExtra(ItemActivity_.NAME_RESULT);
                String itemPrice = data.getStringExtra(ItemActivity_.PRICE_RESULT);
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        ListItem item = new ListItem();
                        item.setItemName(params[0]);
                        item.setItemPrice(Integer.parseInt(params[1]));
                        item.setUser(params[2]);
                        soundListDataBase.soundListDAO().insertItem(item);
                        return null;
                    }
                }.execute(itemName, itemPrice, actualUserName);
            }

            //Get the viewed data, and observe
            getLiveData(sortState, actualUserName).observe(this, new DataBaseObserver());
        }
    }

    //ROOM
    public void clearShoppingList(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                soundListDataBase.soundListDAO().clearUserDB(actualUserName);
            }
        }).start();
    }

    //TTS
    public void TTSwelcome(String name){
        String text = welcomeText.getText().toString();
        String strID = "WLC_" + name;

        //Set the TTS variables
        helpingTTS.setPitch(TTSpitch);
        helpingTTS.setSpeechRate(TTSspeed);

        //Add extra information
        text += getString(R.string.ExtraInfo);

        TTSisActivated = true;
        helpingTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, strID);
    }

    void TTSitem(ListItem item){
        String text = getString(R.string.item_name_text) + item.getItemName() + getString(R.string.item_price_text) + item.getItemPrice() + getString(R.string.item_price_unit)  + ".";
        String strID = "ITEM_" + item.getItemId();

        //Set the TTS variables
        helpingTTS.setPitch(TTSpitch);
        helpingTTS.setSpeechRate(TTSspeed);

        TTSisActivated = true;
        helpingTTS.speak(text, TextToSpeech.QUEUE_ADD, null, strID);
    }

    void TTSReadSortState(ListItemHelper.SortState state){
        String stateStr = "";
        long curTime = System.currentTimeMillis();
        switch (state){
            case BY_ID:
                stateStr = getString(R.string.tts_time) + ".";
                break;
            case BY_NAME:
                stateStr = getString(R.string.tts_name) + ".";
                break;
            case BY_PRICE:
                stateStr = getString(R.string.tts_price) + ".";
                break;
        }
        String text = getString(R.string.state_pre) + stateStr;
        String strID = "STATE_" + curTime;

        //Set the TTS variables
        helpingTTS.setPitch(TTSpitch);
        helpingTTS.setSpeechRate(TTSspeed);

        TTSisActivated = true;
        helpingTTS.speak(text, TextToSpeech.QUEUE_ADD, null, strID);
    }

    //Extra methods
    private void updateWelcomeStr(String name){
        String welcome = getString(R.string.welcome);
        String defaultUser = getString(R.string.defaultUser);
        welcomeText.setText(welcome + " " + name + "!");
        if(!name.equals(defaultUser) && TTSisAvailable){
            TTSwelcome(name);
        }
    }

    private LiveData<List<ListItem>> getLiveData(ListItemHelper.SortState state, String username){
        if(state == ListItemHelper.SortState.BY_ID){
            return soundListDataBase.soundListDAO().getUserItems(username);
        }
        if(state == ListItemHelper.SortState.BY_NAME){
            return soundListDataBase.soundListDAO().getUserItemsByName(username);
        }
        return soundListDataBase.soundListDAO().getUserItemsByPrice(username);
    }

    //Shutdown
    @Override
    protected void onDestroy() {
        //Broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCustomReceiver);

        //TTS
        if(helpingTTS != null){
            helpingTTS.stop();
            helpingTTS.shutdown();
        }

        super.onDestroy();
    }
}