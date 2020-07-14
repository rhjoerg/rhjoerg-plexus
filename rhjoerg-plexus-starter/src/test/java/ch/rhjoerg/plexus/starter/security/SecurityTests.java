package ch.rhjoerg.plexus.starter.security;

import org.junit.jupiter.api.Test;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import ch.rhjoerg.plexus.starter.StarterPlexusConfiguration;
import ch.rhjoerg.plexus.starter.StarterPlexusContainer;

@EnablePlexusSecurity
public class SecurityTests
{
	@Test
	public void test() throws Exception
	{
		StarterPlexusConfiguration configuration = new StarterPlexusConfiguration(SecurityTests.class);
		StarterPlexusContainer container = new StarterPlexusContainer(configuration);

		container.lookup(PlexusCipher.class, "default");
		container.lookup(SecDispatcher.class, "default");
	}
}
