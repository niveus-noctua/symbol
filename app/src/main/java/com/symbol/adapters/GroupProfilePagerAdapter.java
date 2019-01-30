package com.symbol.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.symbol.symbol.ChatListFragment;
import com.symbol.symbol.GroupProfileFragment;

public class GroupProfilePagerAdapter extends FragmentPagerAdapter {

    public GroupProfilePagerAdapter(FragmentManager mg) {
        super(mg);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ChatListFragment chatListFragment = new ChatListFragment();
                return chatListFragment;
            case 1:
                GroupProfileFragment groupProfileFragment = new GroupProfileFragment();
                return groupProfileFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
