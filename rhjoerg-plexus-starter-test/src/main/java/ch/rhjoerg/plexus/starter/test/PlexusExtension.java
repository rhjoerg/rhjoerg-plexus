package ch.rhjoerg.plexus.starter.test;

import static ch.rhjoerg.commons.reflect.Classes.walkClassTree;
import static ch.rhjoerg.plexus.starter.container.ContainerFactory.plexusStarterContainer;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import com.google.inject.Injector;

import ch.rhjoerg.commons.reflect.ClassVisitor;
import ch.rhjoerg.plexus.starter.annotation.PlexusConfigurations;

public class PlexusExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback
{
	public static Namespace NAMESPACE = Namespace.create(PlexusExtension.class);
	public static Object CONTAINER_KEY = DefaultPlexusContainer.class;
	public static Object INJECTOR_KEY = Injector.class;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		Class<?>[] configurationClasses = discoverConfigurationClasses(context);
		DefaultPlexusContainer container = plexusStarterContainer(configurationClasses);
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

	private Class<?>[] discoverConfigurationClasses(ExtensionContext context)
	{
		Class<?> testClass = context.getRequiredTestClass();
		PlexusTestConfigurationClassVisitor visitor = createClassVisitor(context);

		walkClassTree(testClass, visitor);

		if (visitor.result.isEmpty())
		{
			visitor.result.add(testClass);
		}

		return visitor.result.toArray(Class<?>[]::new);
	}

	protected PlexusTestConfigurationClassVisitor createClassVisitor(ExtensionContext context)
	{
		return new PlexusTestConfigurationClassVisitor();
	}

	public static class PlexusTestConfigurationClassVisitor implements ClassVisitor
	{
		public final List<Class<?>> result = new ArrayList<>();

		@Override
		public boolean enterClass(Class<?> type)
		{
			return result.isEmpty();
		}

		@Override
		public boolean leaveClass(Class<?> type)
		{
			return result.isEmpty();
		}

		@Override
		public boolean visitClass(Class<?> type)
		{
			PlexusConfigurations configs = type.getAnnotation(PlexusConfigurations.class);
			WithPlexus withPlexus = type.getAnnotation(WithPlexus.class);

			if (configs != null)
			{
				result.addAll(List.of(configs.value()));
			}

			if (withPlexus != null)
			{
				result.addAll(List.of(withPlexus.value()));
			}

			return result.isEmpty();
		}
	}
}
