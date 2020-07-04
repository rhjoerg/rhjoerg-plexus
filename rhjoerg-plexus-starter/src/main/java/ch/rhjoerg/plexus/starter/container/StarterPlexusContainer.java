package ch.rhjoerg.plexus.starter.container;

import static ch.rhjoerg.commons.tool.ClassLoaders.contextClassLoader;
import static com.google.inject.name.Names.named;
import static org.codehaus.plexus.PlexusConstants.PLEXUS_KEY;
import static org.codehaus.plexus.PlexusConstants.REALM_VISIBILITY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.MapOrientedComponentConfigurator;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.eclipse.sisu.bean.BeanManager;
import org.eclipse.sisu.bean.LifecycleManager;
import org.eclipse.sisu.inject.DefaultBeanLocator;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.inject.DeferredProvider;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.plexus.ClassRealmManager;
import org.eclipse.sisu.plexus.DefaultPlexusBeanLocator;
import org.eclipse.sisu.plexus.Hints;
import org.eclipse.sisu.plexus.PlexusBean;
import org.eclipse.sisu.plexus.PlexusBeanConverter;
import org.eclipse.sisu.plexus.PlexusBeanLocator;
import org.eclipse.sisu.plexus.PlexusBindingModule;
import org.eclipse.sisu.plexus.PlexusDateTypeConverter;
import org.eclipse.sisu.plexus.PlexusLifecycleManager;
import org.eclipse.sisu.plexus.PlexusXmlBeanConverter;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.LoadedClass;
import org.eclipse.sisu.wire.ParameterKeys;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

import ch.rhjoerg.commons.tool.ExcludingClassLoader;

public class StarterPlexusContainer implements MutablePlexusContainer
{
	// See DefaultPlexusContainer for origin

	public final static String STARTER_REALM_ID = "plexus.starter";

	public final static List<String> STARTER_CLASSLOADER_EXCLUSIONS = //
			List.of("META-INF/plexus/components.xml", "META-INF/sisu/javax.inject.Named");

	public final static BeanScanning STARTER_BEAN_SCANNING = BeanScanning.OFF;
	public final static String STARTER_COMPONENT_VISIBILITY = REALM_VISIBILITY;

	static
	{
		System.setProperty("guice.disable.misplaced.annotation.check", "true");
	}

	final AtomicInteger plexusRank = new AtomicInteger();

	private final LoggerManagerProvider loggerManagerProvider = new LoggerManagerProvider();
	private final MutableBeanLocator qualifiedBeanLocator = new DefaultBeanLocator();
	final ThreadLocal<ClassRealm> lookupRealm = new ThreadLocal<ClassRealm>();

	private final Context context;

	private final ExcludingClassLoader excludingClassLoader;
	private final ClassRealm containerRealm;
	private final ClassRealmManager classRealmManager;

	private final PlexusBeanLocator plexusBeanLocator;
	private final BeanManager plexusBeanManager;

	private LoggerManager loggerManager = new ConsoleLoggerManager();
	private Logger logger;

	private boolean disposing = false;

	public StarterPlexusContainer(Module... customModules)
	{
		context = new DefaultContext();
		context.put(PLEXUS_KEY, this);

		excludingClassLoader = createClassLoader();
		containerRealm = createContainerRealm(excludingClassLoader);

		classRealmManager = new ClassRealmManager(qualifiedBeanLocator);
		containerRealm.getWorld().addListener(classRealmManager);

		plexusBeanLocator = new DefaultPlexusBeanLocator(qualifiedBeanLocator, STARTER_COMPONENT_VISIBILITY);

		plexusBeanManager = new PlexusLifecycleManager(Providers.of(context), loggerManagerProvider, //
				new SLF4JLoggerFactoryProvider(), new LifecycleManager());

		setLookupRealm(containerRealm);

		List<Module> modules = new ArrayList<Module>();

		modules.add(new ContainerModule());
		Collections.addAll(modules, customModules);
		modules.add(new PlexusBindingModule(plexusBeanManager, List.of()));
		modules.add(new DefaultsModule());
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	@Override
	public Object lookup(String role) throws ComponentLookupException
	{
		return lookup(role, "");
	}

	@Override
	public Object lookup(String role, String hint) throws ComponentLookupException
	{
		return lookup(null, role, hint);
	}

	@Override
	public <T> T lookup(Class<T> role) throws ComponentLookupException
	{
		return lookup(role, "");
	}

	@Override
	public <T> T lookup(Class<T> role, String hint) throws ComponentLookupException
	{
		return lookup(role, null, hint);
	}

	@Override
	public <T> T lookup(Class<T> type, String role, String hint) throws ComponentLookupException
	{
		try
		{
			return locate(role, type, hint).iterator().next().getValue();
		}
		catch (RuntimeException e)
		{
			throw new ComponentLookupException(e, null != type ? type.getName() : role, hint);
		}
	}

	@Override
	public List<Object> lookupList(String role) throws ComponentLookupException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public Map<String, Object> lookupMap(String role) throws ComponentLookupException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public boolean hasComponent(String role)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public boolean hasComponent(String role, String hint)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public boolean hasComponent(Class<?> role)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public boolean hasComponent(Class<?> role, String hint)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public boolean hasComponent(Class<?> type, String role, String hint)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public void addComponent(Object component, String role)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public <T> void addComponent(T component, Class<?> role, String hint)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public <T> void addComponentDescriptor(ComponentDescriptor<T> descriptor) throws CycleDetectedInComponentGraphException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public ComponentDescriptor<?> getComponentDescriptor(String role, String hint)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public <T> ComponentDescriptor<T> getComponentDescriptor(Class<T> type, String role, String hint)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public List<ComponentDescriptor<?>> getComponentDescriptorList(String role)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> type, String role)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public Map<String, ComponentDescriptor<?>> getComponentDescriptorMap(String role)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap(Class<T> type, String role)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public List<ComponentDescriptor<?>> discoverComponents(ClassRealm classRealm) throws PlexusConfigurationException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	private <T> Iterable<PlexusBean<T>> locate(final String role, final Class<T> type, final String... hints)
	{
		if (disposing)
		{
			return List.of();
		}

		final String[] canonicalHints = Hints.canonicalHints(hints);

		if (null == role || null != type && type.getName().equals(role))
		{
			return plexusBeanLocator.locate(TypeLiteral.get(type), canonicalHints);
		}

		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public ClassRealm getContainerRealm()
	{
		return containerRealm;
	}

	@Override
	public ClassRealm setLookupRealm(ClassRealm realm)
	{
		ClassRealm oldLookupRealm = lookupRealm.get();

		lookupRealm.set(realm);

		return oldLookupRealm;
	}

	@Override
	public ClassRealm getLookupRealm()
	{
		return lookupRealm.get();
	}

	@Override
	public ClassRealm createChildRealm(String id)
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public void release(Object component) throws ComponentLifecycleException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public void releaseAll(Map<String, ?> components) throws ComponentLifecycleException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public void releaseAll(List<?> components) throws ComponentLifecycleException
	{
		throw new UnsupportedOperationException("not yet implemented.");
	}

	@Override
	public void dispose()
	{
		disposing = true;

		plexusBeanManager.unmanage();
		containerRealm.setParentRealm(null);
		qualifiedBeanLocator.clear();

		lookupRealm.remove();

		containerRealm.getWorld().removeListener(classRealmManager);
	}

	@Override
	public synchronized LoggerManager getLoggerManager()
	{
		return loggerManager;
	}

	@Override
	@Inject(optional = true)
	public synchronized void setLoggerManager(LoggerManager loggerManager)
	{
		if (loggerManager == null)
		{
			this.loggerManager = new ConsoleLoggerManager();
		}
		else
		{
			this.loggerManager = loggerManager;
		}

		this.logger = null;
	}

	@Override
	public synchronized Logger getLogger()
	{
		if (logger == null)
		{
			logger = loggerManager.getLoggerForComponent(PlexusContainer.class.getName(), null);
		}

		return logger;
	}

	@Override
	public ClassWorld getClassWorld()
	{
		return containerRealm.getWorld();
	}

	private static ExcludingClassLoader createClassLoader()
	{
		return new ExcludingClassLoader(contextClassLoader(), STARTER_CLASSLOADER_EXCLUSIONS);
	}

	private static ClassRealm createContainerRealm(ExcludingClassLoader excludingClassLoader)
	{
		ClassWorld classWorld = new ClassWorld(STARTER_REALM_ID, excludingClassLoader);

		return classWorld.getClassRealm(STARTER_REALM_ID);
	}

	private class LoggerManagerProvider implements DeferredProvider<LoggerManager>
	{
		@Override
		public LoggerManager get()
		{
			return getLoggerManager();
		}

		@Override
		public DeferredClass<LoggerManager> getImplementationClass()
		{
			return new LoadedClass<LoggerManager>(get().getClass());
		}
	}

	private class LoggerProvider implements DeferredProvider<Logger>
	{
		@Override
		public Logger get()
		{
			return getLogger();
		}

		@Override
		public DeferredClass<Logger> getImplementationClass()
		{
			return new LoadedClass<Logger>(get().getClass());
		}

	}

	private class SLF4JLoggerFactoryProvider implements Provider<Object>
	{
		@Override
		public Object get()
		{
			return plexusBeanLocator.locate(TypeLiteral.get(org.slf4j.ILoggerFactory.class)).iterator().next().getValue();
		}
	}

	private class ContainerModule extends AbstractModule
	{
		@Override
		protected void configure()
		{
			binder().requireExplicitBindings();

			bind(Context.class).toInstance(context);
			bind(ParameterKeys.PROPERTIES).toInstance(context.getContextData());

			bind(MutableBeanLocator.class).toInstance(qualifiedBeanLocator);
			bind(PlexusBeanLocator.class).toInstance(plexusBeanLocator);
			bind(BeanManager.class).toInstance(plexusBeanManager);

			bind(PlexusContainer.class).to(MutablePlexusContainer.class);
			bind(MutablePlexusContainer.class).to(StarterPlexusContainer.class);
			bind(StarterPlexusContainer.class).toProvider(Providers.of(StarterPlexusContainer.this));
		}
	}

	private class DefaultsModule extends AbstractModule
	{
		@Override
		protected void configure()
		{
			bind(LoggerManager.class).toProvider(loggerManagerProvider);
			bind(Logger.class).toProvider(new LoggerProvider());

			Key<RankingFunction> plexusRankingKey = Key.get(RankingFunction.class, Names.named("plexus"));

			bind(plexusRankingKey).toInstance(new DefaultRankingFunction(plexusRank.incrementAndGet()));
			bind(RankingFunction.class).to(plexusRankingKey);

			install(new PlexusDateTypeConverter());

			bind(PlexusBeanConverter.class).to(PlexusXmlBeanConverter.class);

			bind(ComponentConfigurator.class).annotatedWith(named("basic")) //
					.to(BasicComponentConfigurator.class).in(Singleton.class);

			bind(ComponentConfigurator.class).annotatedWith(named("map-oriented")) //
					.to(MapOrientedComponentConfigurator.class).in(Singleton.class);
		}
	}
}
