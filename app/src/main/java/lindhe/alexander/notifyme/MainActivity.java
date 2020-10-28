package lindhe.alexander.notifyme;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.PackageInfoCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import lindhe.alexander.notifyme.DataFunctions;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class MainActivity extends AppCompatActivity {

    String CHANNEL_ID;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CHANNEL_ID = getResources().getString(R.string.CHANNEL_ID);

        // Create toolbar with icon
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup ads
        setupAds();

        // Create notification channel for priority on Android 8.0 or higher
        createNotificationChannel();

        // Create on click listener for notification button
        final MediaPlayer mp_easter_mf = MediaPlayer.create(this, R.raw.motherfucker);
        Button notificationBtn = findViewById(R.id.notificationBtn);
        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText titleEditText = findViewById(R.id.titleEditText);
                EditText descriptionEditText = findViewById(R.id.descriptionEditText);

                String mTitle = titleEditText.getText().toString();
                String mDescription = descriptionEditText.getText().toString();

                // Validate title and description
                if (mTitle.length() < 1 && mDescription.length() < 1){
                    return;
                }
                if (mDescription.toLowerCase().equals("motherfucker")){
                    mp_easter_mf.start();
                }

                // Create notification ID
                int notificationID = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

                // Create the notification
                createNotification(mTitle, mDescription, notificationID);

                // Clear text boxes
                titleEditText.setText("");
                descriptionEditText.setText("");
            }
        });

        // If activity was created by an intent
        Bundle extras = getIntent().getExtras();
        try {
            // Try. If app is opened from a notification get information
            Integer notificationID = extras.getInt("notificationID");
            String title = extras.getString("title");
            String description = extras.getString("description");

            EditText titleEditText = findViewById(R.id.titleEditText);
            EditText descriptionEditText = findViewById(R.id.descriptionEditText);

            titleEditText.setText(title);
            descriptionEditText.setText(description);
        }
        catch(NullPointerException e){
            // Gets here if the app is opened normally
        }

        // If first time opening the app, run the tutorial
        int versionCode = 0;
        try{
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            long longVersionCode= PackageInfoCompat.getLongVersionCode(pInfo);
            versionCode = (int) longVersionCode;
        }
        catch (PackageManager.NameNotFoundException e){
            // Don't care about exceptions, LOL
        }
        if (!DataFunctions.isInSharedPreferences(this, getString(R.string.spTutorialHasBeenRunInVersion))) {
            runTutorial(true);
            DataFunctions.saveData(this, getString(R.string.spTutorialHasBeenRunInVersion), versionCode);
        }
    }

    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Add actions for menu clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuOptionBugReport:
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("allindhe@gmail.com") +
                                 "?subject=" + Uri.encode("Bug report, NotifyMe!") +
                                 "&body=" + Uri.encode("Please state your issue below:\n");
                Uri uri = Uri.parse(uriText);

                send.setData(uri);
                startActivity(Intent.createChooser(send, "Send mail..."));
                break;

            case R.id.menuOptionSupportDeveloper:
                // If an ad is loaded, show it
                if (mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                } else{
                    Toast.makeText(this, "Ad couldn't load. Please try again.", Toast.LENGTH_LONG).show();
                }

                // If no ad is loading, load one
                if (!mInterstitialAd.isLoading()){
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
                break;

            case R.id.menuOptionAbout:
                Toast.makeText(this, "Hobby project created by Alexander Lindhe\nVersion " + BuildConfig.VERSION_NAME, Toast.LENGTH_LONG).show();
                break;

            case R.id.menuOptionTutorial:
                runTutorial();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification(String title, String description, int notificationID) {
        // Create tag
        //String notificationTag = notificationID.toString();

        // Create intent for when notification is clicked
        Intent clickIntent = new Intent(this, MainActivity.class);
        clickIntent.setAction(String.valueOf(notificationID)); // Need to make intent unique. Otherwise Android reuses last one
        Bundle extras = new Bundle();
        extras.putInt("notificationID", notificationID);
        extras.putString("title", title);
        extras.putString("description", description);
        clickIntent.putExtras(extras);

        // Build notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_nm)
                .setContentTitle(title)
                .setContentText(description)
                .setColor(getResources().getColor(R.color.colorIcon))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, clickIntent, 0)); // Sets intent when notification is clicked

        // Create notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationID, mBuilder.build());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        try {
            // Try. If app is opened from a notification get information
            Integer notificationID = extras.getInt("notificationID");
            String title = extras.getString("title");
            String description = extras.getString("description");

            EditText titleEditText = findViewById(R.id.titleEditText);
            EditText descriptionEditText = findViewById(R.id.descriptionEditText);

            titleEditText.setText(title);
            descriptionEditText.setText(description);
        }
        catch(NullPointerException e){
            // Gets here if the app is reopened normally
        }
    }

    private void setupAds(){
        MobileAds.initialize(this, getResources().getString(R.string.adID));

        // Setup ad unit ID and load first ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitialAdID));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        // Setup Ad listener
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // When ad is loaded
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //Toast.makeText(ViewAdScreen.this, "Couldn't load ad.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdOpened() {
                DataFunctions.incrementCounter(MainActivity.this, getResources().getString(R.string.spAdCounter1));
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                int adCount = DataFunctions.getInt(MainActivity.this, getResources().getString(R.string.spAdCounter1));
                String toastMsg = getResources().getString(R.string.gratitudeMessageAd) + "\nTotal ads watched: " + String.valueOf(adCount);
                Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private GuideView mGuideView;
    private GuideView.Builder builder;
    private View view1;
    private View view2;
    private View view3;
    private View view4;

    private void runTutorial(Boolean firstTimeRunningApp) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        runTutorial();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.PopUpDialogBox));
        builder.setMessage("Welcome to NotifyMe!\nDo you want to run the tutorial?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private void runTutorial() {
        view1 = findViewById(R.id.linearLayout1);
        view2 = findViewById(R.id.notificationBtn);
        view3 = findViewById(R.id.linearLayout2);
        view4 = findViewById(R.id.linearLayout3);

        final EditText titleEditText = findViewById(R.id.titleEditText);
        final EditText descriptionEditText = findViewById(R.id.descriptionEditText);

        final String tutorialTitleString = "Tutorial";
        final String tutorialDescriptionString = "Notification description..";
        final int tutorialNotificationID = 12345;

        builder = new GuideView.Builder(this)
                .setTitle("Enter info")
                .setContentText("Enter a title and/or a description for your notification")
                .setGravity(Gravity.center)
                .setDismissType(DismissType.outside)
                .setTargetView(view1)
                .setGuideListener(new GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        // Create coach marks
                        switch (view.getId()) {
                            case R.id.linearLayout1:
                                // Fill text boxes during tutorial
                                titleEditText.setText(tutorialTitleString);
                                descriptionEditText.setText(tutorialDescriptionString);

                                builder.setTargetView(view2)
                                        .setTitle("Press the button")
                                        .setContentText("Create your custom notification");
                                break;
                            case R.id.notificationBtn:
                                // Empty text boxes during tutorial, create notification
                                titleEditText.setText("");
                                descriptionEditText.setText("");
                                createNotification(tutorialTitleString, tutorialDescriptionString, tutorialNotificationID);

                                builder.setTargetView(view3)
                                        .setTitle("Done!")
                                        .setContentText("Your custom notification is now in place,\nit's that simple!")
                                        .setGravity(Gravity.auto);
                                break;
                            case R.id.linearLayout2:
                                // Remove tutorial notification
                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                                notificationManager.cancel(tutorialNotificationID);

                                builder.setTargetView(view4)
                                        .setTitle("All set!")
                                        .setContentText("For more info, please refer to the menu.")
                                        .setGravity(Gravity.auto);
                                break;
                            case R.id.linearLayout3:
                                return;
                        }

                        mGuideView = builder.build();
                        mGuideView.show();

                        // Special coach mark happenings
                        switch (view.getId()) {
                            case R.id.linearLayout1:

                                break;
                            case R.id.notificationBtn:
                                builder.setTargetView(view3)
                                        .setTitle("Done!")
                                        .setContentText("Your custom notification is now in place,\nit's that simple!")
                                        .setGravity(Gravity.auto);
                                break;
                            case R.id.linearLayout2:
                                builder.setTargetView(view4)
                                        .setTitle("All set!")
                                        .setContentText("For more info, please refer to the menu.")
                                        .setGravity(Gravity.auto);
                                break;
                            case R.id.linearLayout3:
                                return;
                        }
                    }
                });

        mGuideView = builder.build();
        mGuideView.show();
    }

}
