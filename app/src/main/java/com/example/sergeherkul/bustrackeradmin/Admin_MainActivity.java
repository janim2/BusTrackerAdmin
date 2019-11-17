package com.example.sergeherkul.bustrackeradmin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sergeherkul.bustrackeradmin.Adapters.SolventRecyclerViewAdapter;
import com.example.sergeherkul.bustrackeradmin.Model.ItemObjects;
import com.example.sergeherkul.bustrackeradmin.Model.Notify;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Admin_MainActivity extends AppCompatActivity {

    FirebaseAuth mauth;
    Accessories mainaccessor;
    private StaggeredGridLayoutManager gaggeredGridLayoutManager;
    private String alert_key,school_id,alert_title, alert_message, alert_time, alertImage;
    private Handler thehandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mauth = FirebaseAuth.getInstance();
        mainaccessor = new Accessories(Admin_MainActivity.this);

        school_id = mainaccessor.getString("school_code");
        getSupportActionBar().setTitle("Admin | Safet");

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        gaggeredGridLayoutManager = new StaggeredGridLayoutManager(3, 1);
        recyclerView.setLayoutManager(gaggeredGridLayoutManager);

        List<ItemObjects> gaggeredList = getListItemData();

        SolventRecyclerViewAdapter rcAdapter = new SolventRecyclerViewAdapter(Admin_MainActivity.this, gaggeredList);
        recyclerView.setAdapter(rcAdapter);
    }

    private List<ItemObjects> getListItemData(){
        List<ItemObjects> listViewItems = new ArrayList<ItemObjects>();
        listViewItems.add(new ItemObjects("Alerts",R.drawable.warning));
        listViewItems.add(new ItemObjects("Buses",R.drawable.home_bus));
//        Distresses, graph
        listViewItems.add(new ItemObjects("Children",R.drawable.children1));
        return listViewItems;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.admin_profile:
                startActivity(new Intent(Admin_MainActivity.this, Admin_Profile.class));
                break;

            case R.id.admin_notifications:
                startActivity(new Intent(Admin_MainActivity.this, Notifications.class));
                break;

            case R.id.admin_the_drivers:
                startActivity(new Intent(Admin_MainActivity.this,Registered_Drivers.class));
                break;

            case R.id.parents:
                startActivity(new Intent(Admin_MainActivity.this,Registered_Parents.class));
                break;



            case R.id.admin_logout:
                final AlertDialog.Builder logout = new AlertDialog.Builder(Admin_MainActivity.this, R.style.Myalert);
                logout.setTitle("Logging Out?");
                logout.setMessage("Leaving us? Please reconsider.");
                logout.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        logout here
                        if(isNetworkAvailable()){
                            FirebaseAuth.getInstance().signOut();
                            mainaccessor.put("isverified", false);
                            mainaccessor.clearStore();
                            startActivity(new Intent(Admin_MainActivity.this,Login_Selector.class));
                        }else{
                            Toast.makeText(Admin_MainActivity.this,"No internet connection",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                logout.setPositiveButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                logout.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mauth.getCurrentUser() != null){
            if(mainaccessor.getBoolean("isverified")){
                if(mainaccessor.getString("user_type").equals("Admin")){
                    new Look_for_distresses().execute();
                }else{
                    Intent verify_school = new Intent(Admin_MainActivity.this, MapsActivity.class);
                    verify_school.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(verify_school);
                }
            }else{
                if(mainaccessor.getString("user_type").equals("Admin")){
                    Intent verify_school = new Intent(Admin_MainActivity.this, Verify_School.class);
                    verify_school.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(verify_school);
                }else{
                    Intent verify_school = new Intent(Admin_MainActivity.this, Driver_Verification.class);
                    verify_school.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(verify_school);
                }
            }
        }else{
            Intent gotoLogin = new Intent(Admin_MainActivity.this, Login_Selector.class);
            gotoLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(gotoLogin);
        }
    }

    private class Look_for_distresses extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            final Handler thehandler;

            thehandler = new Handler(Looper.getMainLooper());
            final int delay = 15000;

            thehandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(isNetworkAvailable()){
                        getUser_Alerts_IDs();
                    }else{
                        Toast.makeText(Admin_MainActivity.this,"checking", Toast.LENGTH_LONG).show();
                    }
                    thehandler.postDelayed(this,delay);
                }
            },delay);
            return null;
        }
    }


    private void getUser_Alerts_IDs() {
        try{
            DatabaseReference get_admin_notifications = FirebaseDatabase.getInstance().getReference("alerts").child(school_id);//.child(mauth.getCurrentUser().getUid());
            get_admin_notifications.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            FetchUser_Alerts(child.getKey());
                        }
                    }else{
//                    Toast.makeText(getActivity(),"Cannot get ID",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Admin_MainActivity.this,"Cancelled",Toast.LENGTH_LONG).show();
                }
            });
        }catch (NullPointerException e){

        }
    }

    private void FetchUser_Alerts(final String key) {
        DatabaseReference get_admin_Notifications = FirebaseDatabase.getInstance().getReference("alerts").child(school_id).child(key);
        get_admin_Notifications.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if(child.getKey().equals("title")){
                            alert_title = child.getValue().toString();
                        }

                        if(child.getKey().equals("message")){
                            alert_message = child.getValue().toString();
                        }

                        if(child.getKey().equals("time")){
                            alert_time = child.getValue().toString();
                        }

                        if(child.getKey().equals("image")){
                            alertImage = child.getValue().toString();
                        }

                        else{
//                            Toast.makeText(getActivity(),"Couldn't fetch posts",Toast.LENGTH_LONG).show();

                        }
                    }
                    alert_key = key;
                    Show_alert_notification(alert_key,alert_title, alert_message);
//                    Notify obj = new Notify(notification_title,notifications_message,notifications_time,notificationImage);
//                    notificationsArray.add(obj);
//                    notifications_RecyclerView.setAdapter(notifications_Adapter);
//                    notifications_Adapter.notifyDataSetChanged();
//                    no_notifications.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Admin_MainActivity.this,"Cancelled",Toast.LENGTH_LONG).show();

            }
        });

    }


    private void Show_alert_notification(String alert_key, String title, String message){

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, Alerts.class);
        intent.putExtra("alertID","yes");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //full screen notification for really important notifications
        Intent fullScreenIntent = new Intent(this, Notifications.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1000")
                .setSmallIcon(R.drawable.distress)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
//                .setFullScreenIntent(fullScreenPendingIntent,true);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1000", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            notificationManagerCompat.notify(1000, builder.build());
//            builder.setDefaults(Notification.DEFAULT_SOUND);
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
//        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManagerCompat.notify(1000, builder.build());

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
