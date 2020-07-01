package ch.rhjoerg.plexus.starter.container;

import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;
import static ch.rhjoerg.plexus.starter.container.ContainerUtils.plexusStarterContainer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.URLClassSpace;
import org.junit.jupiter.api.Test;

import ch.rhjoerg.plexus.starter.container.target.TargetApplication;

public class ClassSpaceTests
{
	@Test
	public void test() throws Exception
	{
		assertTrue(contextClassLoader().getResources("META-INF/plexus/components.xml").hasMoreElements());

		DefaultPlexusContainer container = plexusStarterContainer(TargetApplication.class);
		ClassSpace space = new URLClassSpace(container.getContainerRealm());

		assertFalse(space.getResources("META-INF/plexus/components.xml").hasMoreElements());
	}
}
