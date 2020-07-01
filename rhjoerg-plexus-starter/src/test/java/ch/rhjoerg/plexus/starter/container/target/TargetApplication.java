package ch.rhjoerg.plexus.starter.container.target;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.PlexusContainer;

import com.google.inject.Inject;

@Named
@Singleton
public class TargetApplication implements Runnable
{
	public static PlexusContainer container;

	public int run;

	@Inject
	public TargetApplication(PlexusContainer container)
	{
		TargetApplication.container = container;
	}

	@Override
	public void run()
	{
		++run;
	}
}
