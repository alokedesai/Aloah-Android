package hu.ait.android.aloke.aloah.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.util.Base64;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import hu.ait.android.aloke.aloah.R;
import hu.ait.android.aloke.aloah.task.UploadEncryptedKey;

/**
 * Created by Aloke on 4/16/15.
 */
public class CryptoUtils {
    public static final String PRIVATE_KEY = "PRIVATE_KEY";
    public static final String PUBLIC_KEY = "PUBLIC_KEY";
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

            //read bytes of the file that will be encrypted/decrypted into inputBytes
            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            // write the file after encryption/decryption into outputBytes
            byte[] outputBytes = cipher.doFinal(inputBytes);
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

    /**
     * @return the decrypted symmetric key based on the encrypted key and private key
     * stored in shared preferences
     * @throws IOException
     */
    private static byte[] getSymmetricKey() throws IOException {

        String private_key = getKeyFromSharedPreferences(PRIVATE_KEY);
//        byte[] keyBytes = getEncryptedKey();

        // get the first line from keyfile and
        String firstLine = getKeyFromSharedPreferences(ENCRYPTED_KEY);
        System.out.println("the first line in getSymmetricKey is: \n" + firstLine);
        byte[] keyBytes = Base64.decode(firstLine, Base64.DEFAULT);
        return getSymmetricKey(keyBytes, private_key);
    }

    /**
     * @param symmetricKey the encrypted symmetric key as a byte array
     * @param privateKey the private key as a string
     * @return the decrypted symmetric key
     * @throws IOException
     */
    private static byte[] getSymmetricKey(byte[] symmetricKey, String privateKey) throws IOException {

        byte[] keyBytes = symmetricKey;

        try {
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");

            cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromString(privateKey));
            byte[] result = cipher.doFinal(keyBytes);
            System.out.println("the length of the key after decryption: " + result.length);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void setContext(Context ctx) {
        context = ctx;
    }


    private static Key getPrivateKeyFromString(String privateKey) {
        PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.DEFAULT));
        PrivateKey privKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privKey = kf.generatePrivate(specPriv);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return privKey;
    }

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(4096);
        return generator.generateKeyPair();
    }

    /**
     * generates public and private keys, saves to shared preferences and prints out
     */
    public static void createRSAKeys() {
        try {
            KeyPair pair = generateRSAKeyPair();
            Key pubKey = pair.getPublic();
            Key privKey = pair.getPrivate();

            System.out.println("The new pub key is:\n" + Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT).replaceAll("\n", ""));
            System.out.println("The new priv key is:\n" + Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT).replaceAll("\n", ""));

            saveRSAKeysToSharedPreferences(pubKey, privKey);
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            System.out.println("there was an error :(");
        }
    }

    /* SHARED PREFERENCES METHODS
        ------------------------------------------------------------------------------------------------
     */
    public static void saveRSAKeysToSharedPreferences(Key pubKey, Key privKey) {
        saveKeyToSharedPreferences(Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT), PUBLIC_KEY);
        saveKeyToSharedPreferences(Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT), PRIVATE_KEY);
    }

    public static void saveKeyToSharedPreferences(String key, String keyName) {
        SharedPreferences sp = context.getSharedPreferences("KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(keyName, key);
        editor.commit();
    }

    public static String getKeyFromSharedPreferences(String spKey) {
        SharedPreferences sp = context.getSharedPreferences("KEY", Context.MODE_PRIVATE);
        String key = sp.getString(spKey, null);
        System.out.println("the symmetric key in getKeyFromSharedPreferences is: " + key);
        return key;
    }

    public static void saveEncryptedKeyToSharedPreferences(byte[] encryptedKey) {
        // convert cipher text to byte 64 string
        String keyAsString = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        System.out.println("THE ENCRYPTED SYMMETRIC KEY");
        System.out.println(keyAsString);
        saveKeyToSharedPreferences(keyAsString, ENCRYPTED_KEY);
    }
    /*
    END SHARED PREFERENCES METHODS
    ------------------------------------------------
     */

    public static Key createSymmetricKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(128); // for example
            SecretKey secretKey = keyGen.generateKey();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * creates a new symmetric key, and encrypts the symmetric key with each user's public key.
     * Uploads each file to azure blob
     */
    public static void symmetricKeyHandshake() {
        final Key symmetricKey = createSymmetricKey();


        final ArrayList<String> publicKeys = new ArrayList<>();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("User");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> users, ParseException ex) {
                if (ex == null) {
                    for (ParseObject user: users) {
                        publicKeys.add(user.getString("publickey"));

                        // decode key into a key object
                        Key publicKey = getPublicKeyFromString(user.getString("publickey"));

                        byte[] encryptedSymmetricKey = null;

                        try {
                            SecureRandom random = new SecureRandom();
                            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
                            cipher.init(Cipher.ENCRYPT_MODE, publicKey, random);
                            encryptedSymmetricKey = cipher.doFinal(symmetricKey.getEncoded());

                        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException
                                | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                            e.printStackTrace();
                        }

                        uploadEncryptedSymmetricKey(user, encryptedSymmetricKey);
                        saveEncryptedKeyToSharedPreferences(encryptedSymmetricKey);


                    }
                }
            }
        });


                //String[] publicKeys = context.getResources().getStringArray(R.array.public_keys);

    }

    private static void uploadEncryptedSymmetricKey(ParseObject user, byte[] encryptedSymmetricKey) {
        AsyncTask<String, Void, Boolean> asyncTask = new UploadEncryptedKey(context, user);
        asyncTask.execute(Base64.encodeToString(encryptedSymmetricKey, Base64.DEFAULT).replaceAll("\n", ""));
    }

    /**
     * Takes a string that is an encoded private key and returns the underlying private key object
     *
     * @param key a public key encoded as a base64 string
     * @return A Private Key
     */
    public static Key getPublicKeyFromString(String key) {
        Key publicKey = null;
        try {
            byte[] publicKeyBytes = Base64.decode(key, Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            publicKey = fact.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static byte[] encryptSymmetricKeyWithPublicKey(String pubKey) {
        Key publicKey = getPublicKeyFromString(pubKey);
        byte[] symmetricKey = null;
        try {
            symmetricKey = getSymmetricKey();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] encryptedSymmetricKey = null;

        SecureRandom random = new SecureRandom();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, random);
            encryptedSymmetricKey = cipher.doFinal(symmetricKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return encryptedSymmetricKey;
    }

}
