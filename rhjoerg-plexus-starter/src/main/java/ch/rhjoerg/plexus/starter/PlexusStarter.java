package ch.rhjoerg.plexus.starter;

import static ch.rhjoerg.plexus.starter.container.ContainerFactory.plexusStarterContainer;

import org.codehaus.plexus.DefaultPlexusContainer;

public class PlexusStarter
{
	public static void start(Class<?> applicationType) throws Exception
	{
		DefaultPlexusContainer container = plexusStarterContainer(applicationType);

		if (Runnable.class.isAssignableFrom(applicationType))
		{
			Runnable runnable = Runnable.class.cast(container.lookup(applicationType));

			runnable.run();
		}
	}
}
