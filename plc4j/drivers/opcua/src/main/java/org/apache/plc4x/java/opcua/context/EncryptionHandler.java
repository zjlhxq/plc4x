/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.plc4x.java.opcua.context;

import org.apache.plc4x.java.opcua.protocol.OpcuaProtocolLogic;
import org.apache.plc4x.java.opcua.readwrite.MessagePDU;
import org.apache.plc4x.java.opcua.readwrite.OpcuaAPU;
import org.apache.plc4x.java.opcua.readwrite.OpcuaOpenResponse;
import org.apache.plc4x.java.opcua.readwrite.io.OpcuaAPUIO;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class EncryptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcuaProtocolLogic.class);

    private X509Certificate serverCertificate;
    private X509Certificate clientCertificate;
    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;

    public EncryptionHandler(CertificateKeyPair ckp, byte[] senderCertificate) {
        this.clientPrivateKey = ckp.getKeyPair().getPrivate();
        this.clientPublicKey = ckp.getKeyPair().getPublic();
        this.clientCertificate = ckp.getCertificate();
        this.serverCertificate = getCertificateX509(senderCertificate);
    }

    public ReadBuffer decodeOpcuaOpenResponse(OpcuaOpenResponse message) throws ParseException {
        int encryptedLength = message.getLengthInBytes();
        LOGGER.info("Encrypted Length {}", encryptedLength);
        int encryptedMessageLength = message.getMessage().length + 8;
        LOGGER.info("Encrypted Message Length {}", encryptedMessageLength);
        int headerLength = encryptedLength - encryptedMessageLength;
        LOGGER.info("Header Length {}", headerLength);
        int numberOfBlocks = encryptedMessageLength / 256;
        LOGGER.info("Number of Blocks Length {}", numberOfBlocks);
        WriteBuffer buf = new WriteBuffer(headerLength + numberOfBlocks * 256,true);
        OpcuaAPUIO.staticSerialize(buf, new OpcuaAPU(message));
        byte[] data = buf.getBytes(headerLength, encryptedLength);
        buf.setPos(headerLength);
        decryptBlock(buf, data);
        int tempPos = buf.getPos();
        LOGGER.info("Position at end of Decryption:- {}", tempPos);
        LOGGER.info("Position at end of Decryption within Messgae:- {}", tempPos - headerLength);
        buf.setPos(4);
        buf.writeInt(32, tempPos - 208 - 1);
        //Check Signature, etc..
        buf.setPos(0);
        byte[] unencrypted = buf.getBytes(0, tempPos - 208);
        byte[] signature = buf.getBytes(tempPos - 208, tempPos);
        LOGGER.info("Signature Length:- {}" + signature.length);
        LOGGER.info("Signature;- {}", signature);
        if (!checkSignature(unencrypted, signature)) {
            LOGGER.info("Signature verification failed: - {}", unencrypted);
        }
        ReadBuffer readBuffer = new ReadBuffer(unencrypted, true);
        OpcuaOpenResponse opcuaOpenResponse = (OpcuaOpenResponse) OpcuaAPUIO.staticParse(readBuffer, true).getMessage();
        return new ReadBuffer(opcuaOpenResponse.getMessage(), true);
    }

    public void decryptBlock(WriteBuffer buf, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, this.clientPrivateKey);
            for (int i = 0; i < data.length; i += 256) {
                LOGGER.info("Decrypt Iterate:- {}, Data Length:- {}", i, data.length);
                byte[] decrypted = cipher.doFinal(data, i, 256);
                for (int j = 0; j < 190; j++) {
                    buf.writeByte(8, decrypted[j]);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to decrypt Data");
            e.printStackTrace();
        }
    }

    public boolean checkSignature(byte[] data, byte[] senderSignature) {
        try {
            LOGGER.info("Server public key size {}", this.serverCertificate.getPublicKey().getEncoded().length);
            LOGGER.info("Server public key format {}", this.serverCertificate.getPublicKey().getFormat());
            LOGGER.info("Server public key {}", this.serverCertificate.getPublicKey().toString());
            LOGGER.info("Server public key Algo{}", this.serverCertificate.getPublicKey().getAlgorithm());

            //Signature signature = Signature.getInstance("HMAC-SHA-256", "BC");
            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initVerify(this.serverCertificate.getPublicKey());
            signature.update(data);
            return signature.verify(senderSignature);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Unable to sign Data");
            return false;
        }
    }

    public byte[] encryptPassword(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, this.serverCertificate.getPublicKey());
            return cipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.error("Unable to encrypt Data");
            e.printStackTrace();
            return null;
        }
    }

    public void encryptBlock(WriteBuffer buf, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, this.serverCertificate.getPublicKey());
            for (int i = 0; i < data.length; i += 190) {
                LOGGER.info("Iterate:- {}, Data Length:- {}", i, data.length);
                byte[] encrypted = cipher.doFinal(data, i, 190);
                for (int j = 0; j < 256; j++) {
                    buf.writeByte(8, encrypted[j]);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to encrypt Data");
            e.printStackTrace();
        }
    }



    public X509Certificate getCertificateX509(byte[] senderCertificate) {
        try {
            CertificateFactory factory =  CertificateFactory.getInstance("X.509");
            LOGGER.info("Public Key Length {}", senderCertificate.length);
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(senderCertificate));
        } catch (Exception e) {
            LOGGER.error("Unable to get certificate from String {}", senderCertificate);
            return null;
        }
    }

    public byte[] sign(byte[] data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initSign(this.clientPrivateKey);
            signature.update(data);
            byte[] ss = signature.sign();
            LOGGER.info("----------------Signature Length{}", ss.length);
            return ss;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Unable to sign Data");
            return null;
        }
    }
}
