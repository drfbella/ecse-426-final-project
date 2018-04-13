package com.group08.ecse426finalproject.utils;

/**
 * Created by seanstappas1 on 2018-04-13.
 */

public class ByteUtils {
    private static final float PITCH_ROLL_RESOLUTION = 65_536f; // 16 bits of resolution
    private static final int MAX_PITCH_ROLL_VALUE = 360;


    public int twoBytesToUnsignedInt(byte b1, byte b2) {
        return shortToUnsignedInt((short)((b2 << 8) | (b1 & 0xFF)));
    }

    public int shortToUnsignedInt(short s) {
        return s & 0xFFFF;
    }

    public float twoBytesToPitchRollData(byte b1, byte b2) {
        return (twoBytesToUnsignedInt(b1, b2) / PITCH_ROLL_RESOLUTION) * MAX_PITCH_ROLL_VALUE;
    }

    public int[] toUnsignedArray(byte[] data) {
        int[] unsignedData = new int[data.length / 2];
        for (int i = 0; i < data.length; i += 2) {
            unsignedData[i / 2] = twoBytesToUnsignedInt(data[i], data[i + 1]);
        }
        return unsignedData;
    }
}
