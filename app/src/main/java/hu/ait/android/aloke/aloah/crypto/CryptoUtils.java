package hu.ait.android.aloke.aloah.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaCodec;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;

import javax.crypto.spec.SecretKeySpec;

import hu.ait.android.aloke.aloah.MainActivity;
import hu.ait.android.aloke.aloah.R;

/**
 * Created by Aloke on 4/16/15.
 */
public class CryptoUtils {
    private static final String PRIVATE_KEY = "PRIVATE_KEY";
    private static final String PUBLIC_KEY = "PUBLIC_KEY";
    public static final String ENCRYPTED_KEY = "ENCRYPTED_KEY";

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static Context context;

    public static boolean encrypt(File inputFile, File outputFile)
            throws MediaCodec.CryptoException {
        return doCrypto(Cipher.ENCRYPT_MODE, inputFile, outputFile);
    }

    public static boolean decrypt(File inputFile, File outputFile)
            throws MediaCodec.CryptoException {
        return doCrypto(Cipher.DECRYPT_MODE, inputFile, outputFile);
    }

    private static boolean doCrypto(int cipherMode, File inputFile,
                                    File outputFile) throws MediaCodec.CryptoException {

        boolean success = true;
        try {
            Key secretKey = new SecretKeySpec(getSymmetricKey(), ALGORITHM);
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

    private static byte[] getSymmetricKey() throws IOException {

        String private_key = getKey(PRIVATE_KEY);
        System.out.println("private key num bytes: " + private_key.getBytes().length);

//        byte[] keyBytes = getEncryptedKey();

        // get the first line from keyfile and
        String firstLine = getKey(ENCRYPTED_KEY);
        System.out.println("the first line in getSymmetricKey is: \n" + firstLine );
        byte[] keyBytes = Base64.decode(firstLine, Base64.DEFAULT);

        // decrypt keyBytes into the actual symmetric key
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");

            cipher.init(Cipher.DECRYPT_MODE, getKeyFromString(private_key));
            return cipher.doFinal(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("inside no such algorithm");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getSymmetricKey(byte[] symmetricKey, String privateKey) throws IOException {
        // get the first line of the file, this has the string of the key in it;
        byte[] keyBytes = symmetricKey;

        // decrypt keyBytes into the actual symmetric key
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");

            cipher.init(Cipher.DECRYPT_MODE, getKeyFromString(privateKey));
            return cipher.doFinal(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("inside no such algorithm");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static void setContext(Context ctx) {
        context = ctx;
    }

    private static Key getKeyFromString(String privateKey) {
        System.out.println("the private key: " + getKey(PRIVATE_KEY));
        PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.DEFAULT));

        System.out.println("the length of the private key is: " + Base64.decode(privateKey, Base64.DEFAULT).length);
        PrivateKey privKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privKey = kf.generatePrivate(specPriv);
        }
        catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return privKey;
    }

    public static byte[] createRSAKeys(String text) {
        byte[] input = text.getBytes();

        try {
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
            SecureRandom random = new SecureRandom();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

            generator.initialize(1024);

            KeyPair pair = generator.generateKeyPair();
            Key pubKey = pair.getPublic();

            Key privKey = pair.getPrivate();

            cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);
            byte[] encryptedKey = cipher.doFinal(input);
//            System.out.println("the public key is: \n\n" + Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT) + "\nENDPUBLIC");
//            System.out.println("the private key is: \n\n " + Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT) + "\nENDPRIVATE");

//            System.out.println("The private key num bytes on create: " + (Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT).getBytes().length));

            saveKeyToSharedPreferences(Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT), PUBLIC_KEY);
            saveKeyToSharedPreferences(Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT), PRIVATE_KEY);
            saveEncryptedKeyToSharedPreferences(encryptedKey);


            byte[] endResult = getSymmetricKey(encryptedKey, Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT));
            System.out.println("the end result is: " + new String(endResult));
            return encryptedKey;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException | IOException e) {
            e.printStackTrace();
            System.out.println("there was an error :(");
            return null;
        }
    }

    public static void saveKeyToSharedPreferences(String key, String keyName) {
        SharedPreferences sp = context.getSharedPreferences("KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(keyName, key);
        editor.commit();
    }

    public static String getKey(String spKey) {
        SharedPreferences sp = context.getSharedPreferences("KEY", Context.MODE_PRIVATE);
        String key = sp.getString(spKey, null);
        return key;
    }

    public static void saveEncryptedKeyToSharedPreferences(byte[] encryptedKey) {
        // convert cipher text to byte 64 string
        String keyAsString = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        System.out.println("THE ENCRYPTED SYMMETRIC KEY");
        System.out.println(keyAsString);
        saveKeyToSharedPreferences(keyAsString, ENCRYPTED_KEY);

    }

//    public static byte[] getEncryptedKey() {
//        String keyAsString = getKey(ENCRYPTED_KEY);
//        return Base64.decode(keyAsString, Base64.DEFAULT);
//    }
}
