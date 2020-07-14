package ch.rhjoerg.plexus.starter.test;

import javax.inject.Inject;

import org.codehaus.plexus.PlexusContainer;
import org.junit.jupiter.api.Test;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import ch.rhjoerg.plexus.starter.security.EnablePlexusSecurity;

@WithPlexus(PlexusSecurityTests.class)
@EnablePlexusSecurity
public class PlexusSecurityTests
{
	@Inject
	private PlexusContainer container;

	@Test
	public void test() throws Exception
	{
		container.lookup(PlexusCipher.class, "default");
		container.lookup(SecDispatcher.class, "default");
	}
}
