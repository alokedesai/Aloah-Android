package hu.ait.android.aloke.aloah.crypto;

import android.media.MediaCodec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Aloke on 4/16/15.
 */
public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public static void encrypt(String key, File inputFile, File outputFile)
            throws MediaCodec.CryptoException {
        doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    public static void decrypt(String key, File inputFile, File outputFile)
            throws MediaCodec.CryptoException {
        doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
    }

    private static void doCrypto(int cipherMode, String key, File inputFile,
                                 File outputFile) throws MediaCodec.CryptoException {
        try {
            Key secretKey = new SecretKeySpec(generateKey(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];

            inputStream.read(inputBytes);
            if (cipherMode == Cipher.ENCRYPT_MODE) {
                System.out.println("input byytes");
                for (byte b: inputBytes) {
                    System.out.print(b);
                }
                System.out.println("\n\n");
            }

            byte[] outputBytes = cipher.doFinal(inputBytes);

            if (cipherMode == Cipher.DECRYPT_MODE) {
                System.out.println("output byytes");
                for (byte b: outputBytes) {
                    System.out.print(b);
                }
                System.out.println("\n\n");
            }

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);



            inputStream.close();
            outputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public static byte[] generateKey(String value) {
        //take the sha hash of a string so that we can verifty the byte value is a mutiple of 16
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(value.getBytes());
        return md.digest();
    }
}
