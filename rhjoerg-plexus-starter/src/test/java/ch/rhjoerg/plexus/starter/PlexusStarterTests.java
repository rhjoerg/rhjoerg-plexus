package ch.rhjoerg.plexus.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.rhjoerg.plexus.starter.container.target.TargetApplication;

public class PlexusStarterTests
{
	@Test
	public void test() throws Exception
	{
		PlexusStarter.start(TargetApplication.class);

		TargetApplication app = TargetApplication.container.lookup(TargetApplication.class);

		assertEquals(1, app.run);
	}
}
