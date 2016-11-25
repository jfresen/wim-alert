package nl.jellow.wimalert.util;

import android.util.Log;
import android.util.Pair;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import nl.jellow.wimalert.App;

/**
 * Created by Jelle on 12-11-2016.
 */

public class Aes {

	public static final int BYTE_LENGTH = 16;
	public static final int BIT_LENGTH = BYTE_LENGTH * 8;

	private static final String TAG = App.TAG;
	private static final String ALGORITHM = "AES/CBC/NoPadding";

	public static byte[] getKeyFromPassword(final String password) {
		// Get the SHA-256 hash of the password
		// Use the first 16 bytes as the key
		final byte[] key = new byte[BYTE_LENGTH];
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			final byte[] hash = digest.digest(password.getBytes("UTF-8"));
			System.arraycopy(hash, 0, key, 0, BYTE_LENGTH);
		} catch (final Exception e) {
			Log.e(TAG, "Couldn't generate key from password", e);
		}
		return key;
	}

	private static byte[] padd(final byte[] input) {
		if (input.length % BYTE_LENGTH == 0) {
			return input;
		}
		final byte[] output = new byte[(input.length + BYTE_LENGTH - 1) / BYTE_LENGTH * BYTE_LENGTH];
		Arrays.fill(output, (byte)0);
		System.arraycopy(input, 0, output, 0, input.length);
		return output;
	}

	public static Pair<byte[], byte[]> encrypt(final String message, final byte[] key) {
		try {
			final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
			final Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, keySpec);
			final byte[] iv = c.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
			final byte[] encrypted = c.doFinal(padd(message.getBytes("UTF-8")));
			return new Pair<>(iv, encrypted);
		} catch (final Exception e) {
			Log.e(TAG, "Couldn't encrypt message", e);
		}
		return new Pair<>(new byte[0], new byte[0]);
	}

}
