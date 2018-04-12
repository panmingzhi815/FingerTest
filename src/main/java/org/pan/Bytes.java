package org.pan;

public class Bytes {

    public static String bytes2string(byte[] bytes) {
        StringBuilder format = new StringBuilder();
        for (byte aByte : bytes) {
            format.append(byteToHexString(aByte)).append(" ");
        }
        return format.toString();
    }

    public static byte[] string2bytes(String s) {
        String[] split = s.split(" ");
        byte[] bytes = new byte[split.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(split[i],16);
        }
        return bytes;
    }

    private static String byteToHexString(byte bytes) {
        char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'
        };
        char hexChars1 = 0;
        char hexChars2 = 0;
        int v;

        v = bytes & 0xFF;
        hexChars1 = hexArray[v / 16];
        hexChars2 = hexArray[v % 16];

        return hexChars1 + "" + hexChars2;
    }
}