package com.symbol.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.symbol.symbol.ChatsFragment;
import com.symbol.symbol.FriendsFragment;
import com.symbol.symbol.NewsFragment;
import com.symbol.symbol.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                NewsFragment newsFragment = new NewsFragment();
                return newsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default:
                return null;
        }
    }

    public View getTabView(int position, Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        TextView icon = v.findViewById(R.id.customTabIcon);
        ImageButton badge = v.findViewById(R.id.customTabBadge);
        TextView title = v.findViewById(R.id.customTabTitle);
        if (position == 0) {
            icon.setBackground(context.getDrawable(R.drawable.ic_news));
            badge.setVisibility(View.INVISIBLE);
            title.setText("NEWS");
            title.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        if (position == 1) {
            icon.setBackground(context.getDrawable(R.drawable.ic_groups));
            badge.setVisibility(View.INVISIBLE);
            title.setText("GROUPS");
        }
        if (position == 2) {
            icon.setBackground(context.getDrawable(R.drawable.ic_friends));
            badge.setVisibility(View.INVISIBLE);
            title.setText("INVITES");
        }
        return v;
    }

    public View getWithBadge(int position, Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        TextView icon = v.findViewById(R.id.customTabIcon);
        TextView title = v.findViewById(R.id.customTabTitle);
        ImageButton badge = v.findViewById(R.id.customTabBadge);
        TextView badgeView = v.findViewById(R.id.badgeView);

        icon.setBackground(context.getDrawable(R.drawable.ic_friends));
        badge.setVisibility(View.VISIBLE);
        title.setText("FRIENDS");
        /*if (position == 2) {
            int temp = Integer.valueOf(badgeView.getText().toString());
            badgeView.setText(String.valueOf(++temp));
        }*/
        return v;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
