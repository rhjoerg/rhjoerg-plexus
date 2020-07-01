package ch.rhjoerg.plexus.starter.container;

import static com.google.inject.name.Names.named;

import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.MapOrientedComponentConfigurator;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class StarterModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(ComponentConfigurator.class).annotatedWith(named("basic")) //
				.to(BasicComponentConfigurator.class).in(Singleton.class);

		bind(ComponentConfigurator.class).annotatedWith(named("map-oriented")) //
				.to(MapOrientedComponentConfigurator.class).in(Singleton.class);
	}
}
