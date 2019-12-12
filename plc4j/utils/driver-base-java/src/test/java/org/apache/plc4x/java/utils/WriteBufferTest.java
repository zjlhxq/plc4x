package org.apache.plc4x.java.utils;

import org.apache.commons.io.HexDump;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class WriteBufferTest {

    @Test
    void getData() {
        // TODO: implement me
    }

    @Test
    void writeBit() {
        // TODO: implement me
    }

    @Test
    void writeUnsignedByte() {
        // TODO: implement me
    }

    @Test
    void writeUnsignedShort() {
        // TODO: implement me
    }

    @Test
    void writeUnsignedInt() {
        // TODO: implement me
    }

    @Test
    void writeUnsignedLong() {
        // TODO: implement me
    }

    @Test
    void writeUnsignedBigInteger() {
        // TODO: implement me
    }

    @Test
    void writeByte() {
        // TODO: implement me
    }

    @Test
    void writeShort() {
        // TODO: implement me
    }

    @Test
    void writeInt() {
        // TODO: implement me
    }

    @Test
    void writeLong() {
        // TODO: implement me
    }

    @Nested
    class WriteBigInteger {
        @Nested
        class BigEndian {

            @Test
            void zero() throws Exception {
                WriteBuffer SUT = new WriteBuffer(1, false);
                SUT.writeBigInteger(8, BigInteger.ZERO);
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{0b0000_0000}, data);
                assertEquals(BigInteger.ZERO, new BigInteger(data));
            }

            @Test
            void one() throws Exception {
                WriteBuffer SUT = new WriteBuffer(1, false);
                SUT.writeBigInteger(8, BigInteger.ONE);
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{0b0000_0001}, data);
                assertEquals(BigInteger.ZERO, new BigInteger(data));
            }

            @Test
            void minusOne() throws Exception {
                WriteBuffer SUT = new WriteBuffer(8, false);
                SUT.writeBigInteger(8, BigInteger.ZERO.subtract(BigInteger.ONE));
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{0b0000_0001}, data);
                assertEquals(BigInteger.ZERO, new BigInteger(data));
            }

            @Test
            void minus255() throws Exception {
                WriteBuffer SUT = new WriteBuffer(8, false);
                SUT.writeBigInteger(8, BigInteger.valueOf(-255L));
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{(byte) 0b1000_0000, 0b0000_0001}, data);
                assertEquals(BigInteger.valueOf(-255L), new BigInteger(data));
            }

        }

        @Nested
        class LittleEndian {

            @Test
            void writeBigInteger_LE() throws Exception {
                WriteBuffer SUT_LE = new WriteBuffer(8012, true);
                SUT_LE.writeBigInteger(1, BigInteger.ZERO);
            }
        }
    }

    @Nested
    class WriteUnsignedBigInteger {
        @Nested
        class BigEndian {

            @Test
            void zero() throws Exception {
                WriteBuffer SUT = new WriteBuffer(1, false);
                SUT.writeUnsignedBigInteger(8, BigInteger.ZERO);
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{0b0000_0000}, data);
                assertEquals(BigInteger.ZERO, new BigInteger(data));
            }

            @Test
            void one() throws Exception {
                WriteBuffer SUT = new WriteBuffer(1, false);
                SUT.writeUnsignedBigInteger(8, BigInteger.ONE);
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{0b0000_0001}, data);
                assertEquals(BigInteger.ZERO, new BigInteger(data));
            }

            @Test
            void minusOne() throws Exception {
                WriteBuffer SUT = new WriteBuffer(8, false);
                SUT.writeUnsignedBigInteger(8, BigInteger.ZERO.subtract(BigInteger.ONE));
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{0b0000_0001}, data);
                assertEquals(BigInteger.ZERO, new BigInteger(data));
            }

            @Test
            void minus255() throws Exception {
                WriteBuffer SUT = new WriteBuffer(8, false);
                SUT.writeUnsignedBigInteger(8, BigInteger.valueOf(-255L));
                byte[] data = SUT.getData();
                System.out.println(toHex(data));
                // TODO: check right representation
                assertArrayEquals(new byte[]{(byte) 0b1000_0000, 0b0000_0001}, data);
                assertEquals(BigInteger.valueOf(-255L), new BigInteger(data));
            }

        }

        @Nested
        class LittleEndian {

            @Test
            void writeBigInteger_LE() throws Exception {
                WriteBuffer SUT_LE = new WriteBuffer(8012, true);
                SUT_LE.writeBigInteger(1, BigInteger.ZERO);
            }
        }
    }

    @Test
    void writeFloat() {
        // TODO: implement me
    }

    @Test
    void writeDouble() {
        // TODO: implement me
    }

    @Test
    void writeBigDecimal() {
        // TODO: implement me
    }

    public static String toHex(byte[] bytes) throws Exception {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HexDump.dump(bytes, 0, byteArrayOutputStream, 0);
            return byteArrayOutputStream.toString();
        }
    }
}