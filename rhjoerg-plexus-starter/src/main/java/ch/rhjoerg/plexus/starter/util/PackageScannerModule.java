package ch.rhjoerg.plexus.starter.util;

import static ch.rhjoerg.commons.reflect.Packages.normalizePackages;

import java.util.SortedSet;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.sisu.plexus.PlexusTypeBinder;
import org.eclipse.sisu.plexus.PlexusTypeVisitor;
import org.eclipse.sisu.space.ClassFinder;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.DefaultClassFinder;
import org.eclipse.sisu.space.QualifiedTypeBinder;
import org.eclipse.sisu.space.QualifiedTypeVisitor;
import org.eclipse.sisu.space.SpaceScanner;
import org.eclipse.sisu.space.SpaceVisitor;
import org.eclipse.sisu.space.URLClassSpace;

import com.google.inject.AbstractModule;

public class PackageScannerModule extends AbstractModule
{
	private final ClassSpace space;
	private final SortedSet<String> packages;

	public PackageScannerModule(ClassRealm containerRealm, Iterable<String> packages)
	{
		this.space = new URLClassSpace(containerRealm);
		this.packages = normalizePackages(packages);
	}

	@Override
	protected void configure()
	{
		for (String pkg : packages)
		{
			ClassFinder finder = new DefaultClassFinder(pkg);
			SpaceScanner scanner = new SpaceScanner(space, finder);
			SpaceVisitor qualifiedVisitor = new QualifiedTypeVisitor(new QualifiedTypeBinder(binder()));
			SpaceVisitor plexusVisitor = new PlexusTypeVisitor(new PlexusTypeBinder(binder()));

			scanner.accept(qualifiedVisitor);
			scanner.accept(plexusVisitor);
		}
	}
}
