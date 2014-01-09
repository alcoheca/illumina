package de.medienDresden.illumina.pilight;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Setting extends LinkedHashMap<String, Location> {

    private static final String TAG = Setting.class.getSimpleName();

    private Setting(JSONObject locationsJson) throws JSONException {
        final Iterator locationsJsonIterator = locationsJson.keys();
        final Map<String, Location> unsortedLocations = new HashMap<>();

        while (locationsJsonIterator.hasNext()) {
            final String currentLocation = (String) locationsJsonIterator.next();
            final Location location = parseLocation(locationsJson.getJSONObject(currentLocation));

            unsortedLocations.put(currentLocation, location);
        }

        addSorted(unsortedLocations);
    }

    private Location parseLocation(JSONObject jsonLocation) throws JSONException {
        final Location location = new Location();
        final Iterator locationJsonIterator = jsonLocation.keys();
        final Map<String, Device> devices = new HashMap<>();

        while (locationJsonIterator.hasNext()) {
            final String currentLocationAttribute = (String) locationJsonIterator.next();

            switch (currentLocationAttribute) {
                case "name":
                    location.setName(jsonLocation.optString(currentLocationAttribute));
                    break;

                case "order":
                    location.setOrder(jsonLocation.optInt(currentLocationAttribute));
                    break;

                default:
                    final JSONObject device = jsonLocation.optJSONObject(currentLocationAttribute);

                    if (device != null) {
                        devices.put(currentLocationAttribute, parseDevice(device));
                    } else {
                        Log.d(TAG, "unhandled device parameter " + currentLocationAttribute
                                + ":" + jsonLocation.optString(currentLocationAttribute));
                    }

                    break;
            }
        }

        location.addSorted(devices);
        return location;
    }

    private Device parseDevice(JSONObject jsonDevice) throws JSONException {
        final Device device = new Device();
        final Iterator deviceJsonIterator = jsonDevice.keys();

        while (deviceJsonIterator.hasNext()) {
            final String currentDeviceAttribute = (String) deviceJsonIterator.next();

            switch (currentDeviceAttribute) {
                case "name":
                    device.setName(jsonDevice.optString(currentDeviceAttribute));
                    break;

                case "order":
                    device.setOrder(jsonDevice.optInt(currentDeviceAttribute));
                    break;

                case "state":
                    device.setValue(jsonDevice.optString(currentDeviceAttribute));
                    break;

                case "dimlevel":
                    device.setType(Device.Type.Dimmer); // assuming dimmer device
                    device.setDimLevel(jsonDevice.optInt(currentDeviceAttribute));
                    break;

                case "values":
                    final ArrayList<String> list = new ArrayList<>();
                    final JSONArray jsonArray = jsonDevice.optJSONArray(currentDeviceAttribute);

                    if (jsonArray != null) {
                        final int len = jsonArray.length();
                        for (int i = 0; i < len; i++) {
                            list.add(jsonArray.get(i).toString());
                        }
                    }

                    device.setValues(list);
                    break;

                default:
                    Log.d(TAG, "unhandled device parameter " + currentDeviceAttribute
                            + ":" + jsonDevice.optString(currentDeviceAttribute));
                    break;
            }
        }

        return device;
    }

    private void updateDevices(String locationId, JSONArray deviceIds, JSONObject jsonValues) {
        final int deviceCount = deviceIds.length();

        for (int i = 0; i < deviceCount; i++) {
            try {
                final String deviceId = deviceIds.getString(i);
                Log.i(TAG, "- updating device " + deviceId);
                updateDevice(get(locationId).get(deviceId), jsonValues);
            } catch (JSONException exception) {
                Log.w(TAG, "- updating values failed", exception);
            }
        }
    }

    private void updateDevice(Device device, JSONObject jsonValues) throws JSONException {
        final Iterator jsonValuesIterator = jsonValues.keys();

        while (jsonValuesIterator.hasNext()) {
            final String valueKey = (String) jsonValuesIterator.next();

            switch (valueKey) {
                case "state":
                    final String state = jsonValues.getString(valueKey);
                    Log.i(TAG, "- set state to " + state);
                    device.setValue(state);
                    break;

                case "dimlevel":
                    final int dimLevel = jsonValues.getInt(valueKey);
                    Log.i(TAG, "- set dim level to " + dimLevel);
                    device.setDimLevel(dimLevel);
                    break;

                default:
                    Log.i(TAG, "device value ignored: " + valueKey);
                    break;
            }
        }
    }

    private void addSorted(Map<String, Location> locations) {
        final List<Entry<String, Location>> entries = new LinkedList<>(locations.entrySet());

        Collections.sort(entries, new Comparator<Entry<String, Location>>() {
            @Override
            public int compare(Entry<String, Location> e1,
                               Entry<String, Location> e2) {

                final int o1 = e1.getValue().getOrder();
                final int o2 = e2.getValue().getOrder();

                if (o1 > o2) {
                    return 1;
                } else if (o1 < o2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for(Map.Entry<String, Location> entry: entries){
            put(entry.getKey(), entry.getValue());
        }
    }

    public static Setting create(JSONObject json) throws JSONException {
        return new Setting(json);
    }

    public void update(JSONObject json) {
        Log.i(TAG, "update setting");

        final JSONObject jsonDevices = json.optJSONObject("devices");
        final JSONObject jsonValues = json.optJSONObject("values");
        final Iterator locationIterator = jsonDevices.keys();

        while (locationIterator.hasNext()) {
            final String locationId = (String) locationIterator.next();
            final JSONArray deviceIds = jsonDevices.optJSONArray(locationId);

            Log.i(TAG, "- updating location " + locationId);
            updateDevices(locationId, deviceIds, jsonValues);
        }
    }

    public Location getByIndex(int index) {
        final Iterator<Entry<String, Location>> iterator = entrySet().iterator();
        Location location = null;
        int position = 0;

        while (iterator.hasNext()) {
            final Entry<String, Location> entry = iterator.next();

            if (index == position++) {
                location = entry.getValue();
                break;
            }
        }

        return location;
    }

}
