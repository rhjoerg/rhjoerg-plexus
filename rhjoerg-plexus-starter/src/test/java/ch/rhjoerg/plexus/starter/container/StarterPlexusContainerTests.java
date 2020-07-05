package ch.rhjoerg.plexus.starter.container;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.junit.jupiter.api.Test;

import ch.rhjoerg.plexus.starter.StarterPlexusConfiguration;

public class StarterPlexusContainerTests
{
	@Test
	public void test() throws Exception
	{
		StarterPlexusConfiguration configuration = new StarterPlexusConfiguration();
		PlexusContainer container = new StarterPlexusContainer(configuration);

		assertTrue(container.getContext().get(PlexusConstants.PLEXUS_KEY) == container);

		container.dispose();
	}
}
