package ch.rhjoerg.plexus.starter.container;

import static ch.rhjoerg.plexus.starter.container.ContainerFactory.plexusStarterContainer;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.junit.jupiter.api.Test;

import ch.rhjoerg.plexus.starter.container.target.NamedTarget;
import ch.rhjoerg.plexus.starter.container.target.TargetApplication;
import ch.rhjoerg.plexus.starter.container.target.TargetComponent;

public class ScannerModuleTests
{
	@Test
	public void test() throws Exception
	{
		DefaultPlexusContainer container = plexusStarterContainer(TargetApplication.class);

		NamedTarget namedTarget = container.lookup(NamedTarget.class, "foo");
		TargetComponent targetComponent = container.lookup(TargetComponent.class, "foo");

		assertTrue(namedTarget == targetComponent.namedTarget);
	}
}
