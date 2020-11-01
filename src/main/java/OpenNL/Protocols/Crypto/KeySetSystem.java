package OpenNL.Protocols.Crypto;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.hybrid.HybridKeyTemplates;

public class KeySetSystem {

	public static File keyset=new File("keyset-master.json");
	public static File keysetpub=new File("keyset-public.json");
	public static File keysetpub_extern=new File("keyset-public-extern.json");
	
	/** Loads a KeysetHandle from {@code keyset} or generate a new one if it doesn't exist. */
	public static KeysetHandle getPrivateKeysetHandle()
			throws GeneralSecurityException, IOException {
		if (keyset.exists()) {
			// Read the cleartext keyset from disk.
			// WARNING: reading cleartext keysets is a bad practice. Tink supports reading/writing
			// encrypted keysets, see
			// https://github.com/google/tink/blob/master/docs/JAVA-HOWTO.md#loading-existing-keysets.
			//return CleartextKeysetHandle.read(JsonKeysetReader.withFile(keyset));
		}
		KeysetHandle handle = KeysetHandle.generateNew(HybridKeyTemplates.ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM);
		CleartextKeysetHandle.write(handle, JsonKeysetWriter.withFile(keyset));
		KeysetHandle handlepub = handle.getPublicKeysetHandle();
		CleartextKeysetHandle.write(handlepub, JsonKeysetWriter.withFile(keysetpub));
		return handle;
	}
	
	public static String crypt(KeysetHandle pri, String plaintext) throws GeneralSecurityException, IOException{
		
		//KeysetHandle pri = CleartextKeysetHandle.read(
		//		JsonKeysetReader.withFile(new File(prikeyfile)));
		
		HybridEncrypt encrypt = pri.getPrimitive(HybridEncrypt.class);
		byte[] cypher = encrypt.encrypt(plaintext.getBytes(), "".getBytes());
		
		Base64.Encoder encoder = Base64.getEncoder(); 
		byte[] cypher64 = encoder.encode(cypher);
		return new String(cypher64);
	}
	
	public static String decrypt(KeysetHandle pub, String cipher64) throws GeneralSecurityException, IOException{
		
		System.out.println(cipher64);
		
		//KeysetHandle pub = CleartextKeysetHandle.read(
		//		JsonKeysetReader.withFile(new File(pubkeyfile)));
		
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] cypher = decoder.decode(cipher64);
		
		//System.out.println(new String(cypher));
		
		HybridDecrypt decrypt = pub.getPrimitive(HybridDecrypt.class);
		byte[] plaintext = decrypt.decrypt(cypher, "".getBytes());
		
		return new String(plaintext);
	}
	
}
