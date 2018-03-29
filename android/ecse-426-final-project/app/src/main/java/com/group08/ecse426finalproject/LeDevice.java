package com.group08.ecse426finalproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class LeDevice {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<BLEDevice> ITEMS = new ArrayList<BLEDevice>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, BLEDevice> ITEM_MAP = new HashMap<String, BLEDevice>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDevice(i));
        }
    }

    private static void addItem(BLEDevice item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static BLEDevice createDevice(int position) {
        return new BLEDevice(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }


    public static class BLEDevice {
        public final String id;
        public final String content;
        public final String details;

        public BLEDevice(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
