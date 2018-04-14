package com.group08.ecse426finalproject.utils;

public class ByteManipulator {
    private static final float PITCH_ROLL_RESOLUTION = 65_536f; // 16 bits of resolution
    private static final int MAX_PITCH_ROLL_VALUE = 360;

    /**
     *  Used to transfer byte[] to String if want to display the values in the app.
      * @param data in byte,
     * @return String value of the byte
     */
    public static String hexToString(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length);

        for(byte byteChar : data) {
            sb.append(String.format("%02X ", byteChar));
        }

        return sb.toString();
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

    private int twoBytesToUnsignedInt(byte b1, byte b2) {
        return shortToUnsignedInt((short)((b2 << 8) | (b1 & 0xFF)));
    }

    private int shortToUnsignedInt(short s) {
        return s & 0xFFFF;
    }
}
