package ch.rhjoerg.plexus.starter.security;

import java.io.File;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Named;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.PasswordDecryptor;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import com.google.inject.Inject;

@Named("default")
@Typed(SecDispatcher.class)
public class PlexusSecDispatcher extends DefaultSecDispatcher
{
	@Inject
	public PlexusSecDispatcher(Logger logger, PlexusCipher cipher, Map<String, PasswordDecryptor> decryptors)
	{
		this.enableLogging(logger);
		this._cipher = cipher;
		this._decryptors = decryptors;

		File userHome = new File(System.getProperty("user.home"));
		File settingsFile = new File(userHome, ".settings-security.xml");

		this._configurationFile = settingsFile.getAbsolutePath();
	}
}
