package com.example.craig.pizzaheavendelivery;

/**
 * Created by dansc on 09/05/2017.
 */
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

import java.math.BigInteger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author Joseph Kellaway + Craig Banyard
 */
public class KeyStretch {

    /**
     * Stretches the user's password (with SHA256) to attempt to remove weak
     * passwords prior to hashing and encryption. 1000 iterations is an
     * arbitrary value.
     * @param password  The user's plaintext password
     * @param salt      The auto-generated hexadecimal salt
     * @return          The hexadecimal hash (stretched) password
     */
    public static String keyStretch(String password, String salt){
        try {
            Integer iterations = 1000;
            char[] c = password.toCharArray();
            byte[] s = salt.getBytes();

            //PBEKeySpec spec = new PBEKeySpec(c, s, iterations, 256);
            //SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(password.getBytes(), s, 1000);
            byte[] hash = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();

            //byte[] hash = skf.generateSecret(spec).getEncoded();
            return toHex(hash);
        } catch (Exception e){
            return null;
        }
    }

    /**
     * Turns a byte array into a hexadecimal string.
     * @param bytes Byte array to be convert to hex string.
     * @return      Hexadecimal string value of input.
     */
    private static String toHex(byte[] bytes) {
        BigInteger big = new BigInteger(1, bytes);
        String hex = big.toString(16);
        int padding = (bytes.length * 2) - hex.length();
        if (padding > 0){
            return String.format("%0" + padding + "d", 0) + hex;
        } else {
            return hex;
        }
    }
}