package com.symbol.symbol;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.symbol.adapters.SectionsPagerAdapter;
import com.symbol.firebase.FirebasePath;
import com.symbol.services.FirebaseMessagingService;

public class SymbolActivity extends AppCompatActivity {

    private Toolbar mainPageToolbar;
    private ViewPager pager;
    private SectionsPagerAdapter adapter;
    private TabLayout tabs;

    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DocumentReference path =
            FirebaseFirestore.getInstance().document("PublicProfileData/" + uid);
    private DocumentReference path2 = FirebaseFirestore
            .getInstance()
            .document("Users/" + uid + "/RegistrationData/" + uid);
    private DocumentReference invites = FirebaseFirestore
            .getInstance()
            .collection("Users").document(uid);
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbol);
        FirebasePath.setNewPath("ProfileData", path2);
        FirebasePath.setNewPath("PublicProfileData", path);
        FirebasePath.setUid(uid);

        serviceIntent = new Intent(this, FirebaseMessagingService.class);

        mainPageToolbar = findViewById(R.id.mainPageToolbar);
        pager = findViewById(R.id.tabPager);
        tabs = findViewById(R.id.tabsMenu);
        setSupportActionBar(mainPageToolbar);
        getSupportActionBar().setTitle(R.string.main_action_bar_title);
        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
        tabs.getTabAt(0).setCustomView(adapter.getTabView(0, this));
        tabs.getTabAt(1).setCustomView(adapter.getTabView(1, this)).select();
        tabs.getTabAt(2).setCustomView(adapter.getTabView(2, this));
        invites.collection("Invites").document("invitesCount").addSnapshotListener(this,
                new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()) {
                    String count = snapshot.get("count").toString();
                    if (!count.equals("0")) {
                        tabs.getTabAt(2)
                                .getCustomView()
                                .findViewById(R.id.customTabBadge)
                                .setVisibility(View.VISIBLE);
                        TextView view = tabs.getTabAt(2)
                                .getCustomView()
                                .findViewById(R.id.badgeView);
                        view.setText(count);
                    } else {
                        tabs.getTabAt(2)
                                .getCustomView()
                                .findViewById(R.id.customTabBadge)
                                .setVisibility(View.INVISIBLE);
                    }
                }

                //tabs.getTabAt(2).setCustomView(adapter.getWithBadge(2, SymbolActivity.this));
                //Toast.makeText(SymbolActivity.this, "У тебя новое приглашение", Toast.LENGTH_SHORT).show();
            }
        });
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = tab.getCustomView().findViewById(R.id.customTabTitle);
                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
                if (tab.getPosition() == 2) {
                    FirebaseFirestore.getInstance().collection("Users").document(uid)
                            .collection("Invites").document("invitesCount")
                            .update("count", "0");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView textView = tab.getCustomView().findViewById(R.id.customTabTitle);
                textView.setTextColor(getResources().getColor(R.color.cardview_dark_background));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                TextView textView = tab.getCustomView().findViewById(R.id.customTabTitle);
                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        //tabs.getTabAt(1).setIcon(R.drawable.ic_groups).setText("Groups");
        //tabs.getTabAt(2).setIcon(R.drawable.ic_friends).setText("Friends");

        pager.setCurrentItem(getIntent().getIntExtra("selected_index", 0));
    }

    @Override
    protected void onStart() {
        stopService(serviceIntent);
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.mainLogOutButton) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SymbolActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
            finish();

        }
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(SymbolActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
        }
        if (item.getItemId() == R.id.action_all_users) {
            Intent intent = new Intent(SymbolActivity.this, AllUsersActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(serviceIntent);
    }
}
