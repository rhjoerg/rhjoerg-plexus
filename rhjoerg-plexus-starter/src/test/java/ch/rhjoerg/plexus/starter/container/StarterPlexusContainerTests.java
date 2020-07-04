package ch.rhjoerg.plexus.starter.container;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.junit.jupiter.api.Test;

public class StarterPlexusContainerTests
{
	@Test
	public void test() throws Exception
	{
		PlexusContainer container = new StarterPlexusContainer();

		assertTrue(container.getContext().get(PlexusConstants.PLEXUS_KEY) == container);

		container.dispose();
	}
}
