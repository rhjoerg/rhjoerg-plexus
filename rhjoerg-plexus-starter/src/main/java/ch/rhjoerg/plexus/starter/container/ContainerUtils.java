package ch.rhjoerg.plexus.starter.container;

import static ch.rhjoerg.commons.annotation.Annotations.findAnnotations;
import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;

import com.google.inject.Module;

import ch.rhjoerg.commons.tool.ExcludingClassLoader;
import ch.rhjoerg.plexus.starter.PlexusPackages;

public interface ContainerUtils
{
	public final static String PLEXUS_STARTER_REALM_ID = "plexus.starter";

	public static List<String> plexusStarterExclusions()
	{
		return List.of("META-INF/plexus/components.xml", "META-INF/sisu/javax.inject.Named");
	}

	public static ExcludingClassLoader plexusStarterClassLoader()
	{
		return new ExcludingClassLoader(contextClassLoader(), plexusStarterExclusions());
	}

	public static ClassWorld plexusStarterClassWorld()
	{
		return new ClassWorld(PLEXUS_STARTER_REALM_ID, plexusStarterClassLoader());
	}

	public static DefaultContainerConfiguration plexusStarterConfiguration() throws Exception
	{
		DefaultContainerConfiguration configuration = new DefaultContainerConfiguration();
		ClassWorld classWorld = plexusStarterClassWorld();

		configuration.setClassWorld(classWorld);
		configuration.setRealm(classWorld.getRealm(PLEXUS_STARTER_REALM_ID));
		configuration.setAutoWiring(true);
		configuration.setJSR250Lifecycle(true);

		return configuration;
	}

	public static List<String> plexusStarterPackages(Class<?> applicationType)
	{
		List<PlexusPackages> annotations = findAnnotations(PlexusPackages.class, applicationType);
		List<String> packages = new ArrayList<String>();

		for (PlexusPackages annotation : annotations)
		{
			packages.addAll(List.of(annotation.value()));
		}

		if (packages.isEmpty())
		{
			packages.add(applicationType.getPackageName());
		}

		return packages;
	}

	public static Module[] plexusStarterModules(ContainerConfiguration configuration, Class<?> applicationType)
	{
		Module[] modules = new Module[2];
		Iterable<String> packages = plexusStarterPackages(applicationType);

		modules[0] = new StarterModule();
		modules[1] = new ScannerModule(configuration, packages);

		return modules;
	}

	public static DefaultPlexusContainer plexusStarterContainer(Class<?> applicationType) throws Exception
	{
		ContainerConfiguration configuration = plexusStarterConfiguration();
		Module[] modules = plexusStarterModules(configuration, applicationType);

		return new DefaultPlexusContainer(configuration, modules);
	}
}
