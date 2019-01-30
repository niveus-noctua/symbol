package com.symbol.symbol;


import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.symbol.adapters.GroupProfilePagerAdapter;

public class GroupProfileActivity extends AppCompatActivity {

    private Toolbar groupProfileToolbar;
    private ViewPager groupViewPager;
    private GroupProfilePagerAdapter adapter;
    private TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        groupProfileToolbar = findViewById(R.id.groupProfileToolbar);
        groupViewPager = findViewById(R.id.groupViewPager);
        tabs = findViewById(R.id.groupTabs);
        setSupportActionBar(groupProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Group title");
        adapter = new GroupProfilePagerAdapter(getSupportFragmentManager());
        groupViewPager.setAdapter(adapter);
        tabs.setupWithViewPager(groupViewPager);

        tabs.getTabAt(0).setIcon(R.drawable.ic_chats).setText("Chats");
        tabs.getTabAt(1).setIcon(R.drawable.ic_account).setText("Group profile");

        if (getIntent().getExtras().get("selected_index") != null) {
            groupViewPager.setCurrentItem(getIntent().getExtras().getInt("selected_index"));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, SymbolActivity.class);
        intent.putExtra("selected_index", 1);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
    }
}

