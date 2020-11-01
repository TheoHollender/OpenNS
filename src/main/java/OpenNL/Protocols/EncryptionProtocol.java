package OpenNL.Protocols;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.crypto.tink.KeysetHandle;

import OpenNL.Protocols.Crypto.KeySetSystem;

public class EncryptionProtocol extends Protocol{

	/**
	 * TODO add Encryption Subsystem using Tink
	 */
	
	public static KeysetHandle pri;
	public static KeysetHandle pub;
	
	@Override
	public String toSocket(String s) {
		try {
			return KeySetSystem.crypt(pub, s);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public String fromSocket(String s) {
		try {
			return KeySetSystem.decrypt(pri, s);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

}
