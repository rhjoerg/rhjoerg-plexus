package ch.rhjoerg.plexus.starter.container;

import static ch.rhjoerg.plexus.starter.container.ContainerUtils.plexusStarterContainer;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.junit.jupiter.api.Test;

import ch.rhjoerg.plexus.starter.container.target.TargetApplication;

public class StarterModuleTests
{
	@Test
	public void test() throws Exception
	{
		DefaultPlexusContainer container = plexusStarterContainer(TargetApplication.class);

		container.lookup(ComponentConfigurator.class, "basic");
		container.lookup(ComponentConfigurator.class, "map-oriented");
	}
}
