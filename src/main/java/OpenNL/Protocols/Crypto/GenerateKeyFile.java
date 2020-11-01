package OpenNL.Protocols.Crypto;

import java.io.File;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;

import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.signature.SignatureConfig;
import com.google.crypto.tink.signature.SignatureKeyTemplates;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.config.TinkConfig;

public class GenerateKeyFile {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		/** 
		 * This is used to generate the KEY when they must change
		 */
		
		/**
		 * Part 1 - Generate Key
		 */
		
		// Initializing Signature Configuration
		//TinkConfig.init();
		
		/*for(int i=0; i<5; i++){
			String cipher64 = KeySetSystem.crypt("keyset-public2.json", "Bonjour");
			String plaintxt = KeySetSystem.decrypt("keyset-master2.json", cipher64);
			
			System.out.println(cipher64);
			System.out.println(plaintxt);
		}*/
		
		// Generating and Writing KeySetHandle
		// KeySetSystem.getPrivateKeysetHandle();
		
		KeysetHandle pri = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File("keyset-master2.json")));
		KeysetHandle pub = pri.getPublicKeysetHandle();
		
		/*HybridEncrypt hybrencr = pub.getPrimitive(HybridEncrypt.class);	
		byte[] ciphertext = hybrencr.encrypt(new String("Bonjour").getBytes(), new String("").getBytes());
		
		HybridDecrypt hybrdecr = pri.getPrimitive(HybridDecrypt.class);
		byte[] plaintext = hybrdecr.decrypt(ciphertext, new String("").getBytes());*/
		
		/*String s = new String(plaintext);
		System.out.println(s);
		Base64.Encoder encoder = Base64.getEncoder();
		String sc = new String(encoder.encode(ciphertext));
		System.out.println(sc);*/
		
	}

}
