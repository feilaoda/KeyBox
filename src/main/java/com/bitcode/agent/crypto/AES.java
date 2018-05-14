/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.bitcode.agent.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class AES {

    private AES() {
    }

    private static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data)
            throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        // create output buffer
        int size = cipher.getOutputSize(data.length);
        byte[] buf = new byte[size];

        // process data
        int length1 = cipher.processBytes(data, 0, data.length, buf, 0);
        int length2 = cipher.doFinal(buf, length1);
        int length = length1 + length2;

        // copy buffer to result, without padding
        byte[] result = new byte[length];
        System.arraycopy(buf, 0, result, 0, result.length);

        return result;
    }

    /**
     * Encrypt data with AES/CBC/PKCS5Padding.
     * 
     * @param raw
     * @param key
     * @param iv
     * @return
     * @throws RuntimeException
     */
    public static byte[] encrypt(byte[] raw, byte[] key, byte[] iv) throws RuntimeException {

        try {
            PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            CipherParameters params = new ParametersWithIV(new KeyParameter(key), iv);
            aes.init(true, params);

            return cipherData(aes, raw);
        } catch (DataLengthException | IllegalArgumentException | IllegalStateException
                | InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Decrypt data with AES/CBC/PKCS5Padding
     * 
     * @param encrypted
     * @param key
     * @param iv
     * @return
     * @throws RuntimeException
     */
    public static byte[] decrypt(byte[] encrypted, byte[] key, byte[] iv) throws RuntimeException {
        try {
            PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            CipherParameters params = new ParametersWithIV(new KeyParameter(key), iv);
            aes.init(false, params);

            return cipherData(aes, encrypted);
        } catch (DataLengthException | IllegalArgumentException | IllegalStateException
                | InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

}