/*
 * Copyright (c) 2014 Peter Heisig.
 *
 * This work is licensed under a Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 */

package de.medienDresden.illumina.widget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Locale;

import de.medienDresden.illumina.fragment.DeviceListFragment;
import de.medienDresden.illumina.pilight.Location;

public class LocationPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<Location> mLocations;

    public LocationPagerAdapter(FragmentManager fragmentManager, ArrayList<Location> locations) {
        super(fragmentManager);

        mLocations = locations;
    }

    @Override
    public Fragment getItem(int position) {
        return DeviceListFragment.newInstance(mLocations.get(position).getId());
    }

    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final Locale locale = Locale.getDefault();
        return mLocations.get(position).getName().toUpperCase(locale);
    }

}
