package hu.ait.android.aloke.aloah.crypto;

import android.media.MediaCodec;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;

import javax.crypto.spec.SecretKeySpec;

import hu.ait.android.aloke.aloah.MainActivity;

/**
 * Created by Aloke on 4/16/15.
 */
public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    public static final int AES_KEY_SIZE = 128;    // in bits
    public static final int GCM_NONCE_LENGTH = 12; // in bytes
    public static final int GCM_TAG_LENGTH = 16;   // in bytes

    public static boolean encrypt(String key, File inputFile, File outputFile)
            throws MediaCodec.CryptoException {
        return doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    public static boolean decrypt(String key, File inputFile, File outputFile)
            throws MediaCodec.CryptoException {
        return doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
    }

    private static boolean doCrypto(int cipherMode, String key, File inputFile,
                                 File outputFile) throws MediaCodec.CryptoException {

        boolean success = true;
        try {


//            SecureRandom secureRandom = new SecureRandom();
//            byte[] iv = new byte[16];
//            secureRandom.nextBytes(iv);


            Key secretKey = new SecretKeySpec(generateKey(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // for GCM, we create IV (which is a nonce)
            final byte[] nonce = new byte[GCM_NONCE_LENGTH];

            SecureRandom random = SecureRandom.getInstance("NativePRNG");
            random.nextBytes(nonce);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(cipherMode, secretKey, spec);

            byte[] tag = new byte[GCM_TAG_LENGTH];
            cipher.updateAAD(tag);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];

            Log.d("tag_", ""+inputBytes.length);

            inputStream.read(inputBytes);
            if (cipherMode == Cipher.ENCRYPT_MODE) {
                System.out.println("input bytes");
                for (byte b: inputBytes) {
                    System.out.print(b);
                }
                System.out.println("\n\n");
            }

            byte[] outputBytes = cipher.doFinal(inputBytes);

            if (cipherMode == Cipher.DECRYPT_MODE) {
                System.out.println("output bytes");
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
                | IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException ex) {
            ex.printStackTrace();
            success = false;
        }
        return success;
    }

    public static byte[] generateKey(String value) {
        //take the sha hash of a string so that we can verify the byte value is a multiple of 16
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(value.getBytes());
        return md.digest();

        //return value.getBytes();
    }
}
