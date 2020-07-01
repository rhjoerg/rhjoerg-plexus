package ch.rhjoerg.plexus.starter.container;

import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;
import static ch.rhjoerg.plexus.starter.container.ContainerUtils.plexusStarterClassLoader;
import static java.util.Collections.list;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ContainerUtilsTests
{
	@Test
	public void testPlexusStarterClassLoader() throws Exception
	{
		ClassLoader contextClassLoader = contextClassLoader();
		ClassLoader plexusClassLoader = plexusStarterClassLoader();

		assertEquals(1, list(contextClassLoader.getResources("META-INF/plexus/components.xml")).size());
		assertEquals(1, list(contextClassLoader.getResources("META-INF/sisu/javax.inject.Named")).size());

		assertEquals(0, list(plexusClassLoader.getResources("META-INF/plexus/components.xml")).size());
		assertEquals(0, list(plexusClassLoader.getResources("META-INF/sisu/javax.inject.Named")).size());
	}
}
