package ch.rhjoerg.plexus.starter.test;

import static ch.rhjoerg.commons.reflect.Classes.walkClassTree;
import static ch.rhjoerg.plexus.starter.container.ContainerUtils.plexusStarterContainer;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import com.google.inject.Injector;

import ch.rhjoerg.commons.reflect.ClassVisitor;
import ch.rhjoerg.plexus.starter.PlexusConfigurations;

public class PlexusExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback
{
	public static Namespace NAMESPACE = Namespace.create(PlexusExtension.class);
	public static Object CONTAINER_KEY = DefaultPlexusContainer.class;
	public static Object INJECTOR_KEY = Injector.class;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		Class<?> configurationClass = discoverConfigurationClass(context);
		DefaultPlexusContainer container = plexusStarterContainer(configurationClass);
		Store store = context.getStore(NAMESPACE);

		store.put(CONTAINER_KEY, container);
		store.put(INJECTOR_KEY, container.lookup(Injector.class));
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
		Store store = context.getStore(NAMESPACE);
		DefaultPlexusContainer container = store.get(CONTAINER_KEY, DefaultPlexusContainer.class);

		store.remove(INJECTOR_KEY);
		store.remove(CONTAINER_KEY);

		if (container != null)
		{
			container.dispose();
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception
	{
		Injector injector = context.getStore(NAMESPACE).get(INJECTOR_KEY, Injector.class);

		injector.injectMembers(context.getRequiredTestInstance());
	}

	private Class<?> discoverConfigurationClass(ExtensionContext context)
	{
		Class<?> testClass = context.getRequiredTestClass();
		DiscoverConfigurationClassVisitor visitor = new DiscoverConfigurationClassVisitor();

		walkClassTree(testClass, visitor);

		return visitor.result == null ? testClass : visitor.result;
	}

	public static class DiscoverConfigurationClassVisitor implements ClassVisitor
	{
		public Class<?> result;

		@Override
		public boolean enterClass(Class<?> type)
		{
			return result == null;
		}

		@Override
		public boolean leaveClass(Class<?> type)
		{
			return result == null;
		}

		@Override
		public boolean visitClass(Class<?> type)
		{
			if (hasRequiredAnnotation(type))
			{
				result = type;
			}

			return result == null;
		}

		private boolean hasRequiredAnnotation(Class<?> type)
		{
			if (type.isAnnotationPresent(PlexusConfigurations.class))
			{
				return true;
			}

			return false;
		}
	}
}
