package com.monte.chatbroadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_ITERATIONS = 20;
    PingBroadcastReceiver mPingReceiver;
    PongBroadcastReceiver mPongReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPingReceiver.onReceive(MainActivity.this, null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPingReceiver == null) {
            mPingReceiver = new PingBroadcastReceiver();
        }

        IntentFilter pingIntentFilter =
                new IntentFilter("vandy.mooc.action.VIEW_PING");
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mPingReceiver, pingIntentFilter);

        if (mPongReceiver == null) {
            mPongReceiver = new PongBroadcastReceiver();
        }

        IntentFilter pongIntentFilter =
                new IntentFilter("vandy.mooc.action.VIEW_PONG");
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mPongReceiver, pongIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mPingReceiver != null) {
            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(mPingReceiver);
        }
        if (mPongReceiver != null) {
            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(mPongReceiver);
        }
    }

    private static class PingBroadcastReceiver extends BroadcastReceiver {
        /**
         * Logging tag.
         */
        private static final String TAG = "PingBroadcastReceiver";

        @Override
        public void onReceive(
                Context context, Intent intent) {
            int count = intent != null ? intent.getExtras().getInt("count") : 0;
            Log.d(TAG, "PING " + count);
            Intent resultIntent =
                    new Intent(context, PongBroadcastReceiver.class);
            resultIntent.setAction("vandy.mooc.action.VIEW_PONG");
            resultIntent.putExtra("count", count + 1);
            if (count < MAX_ITERATIONS) {
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(resultIntent);
            }
        }
    }

    private static class PongBroadcastReceiver extends BroadcastReceiver {
        /**
         * Logging tag.
         */
        private static final String TAG = "PongBroadcastReceiver";

        @Override
        public void onReceive(
                final Context context, Intent intent) {
            final int count = intent != null ? intent.getExtras().getInt("count") : 0;
            Log.d(TAG, "PONG " + count);


            if (true) {
                final PendingResult pendingResult = goAsync();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent resultIntent =
                                new Intent(context, PingBroadcastReceiver.class);
                        resultIntent.setAction("vandy.mooc.action.VIEW_PING");
                        resultIntent.putExtra("count", count + 1);
                        LocalBroadcastManager.getInstance(context)
                                .sendBroadcast(resultIntent);
                        if (pendingResult == null) {
                            Log.d(TAG, "pendingResult is NULL!");
                        } else {
                            pendingResult.finish();
                        }
                    }
                }).start();
            } else {
                Intent resultIntent =
                        new Intent(context, PingBroadcastReceiver.class);
                resultIntent.setAction("vandy.mooc.action.VIEW_PING");
                resultIntent.putExtra("count", count + 1);
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(resultIntent);
            }
        }
    }
}
