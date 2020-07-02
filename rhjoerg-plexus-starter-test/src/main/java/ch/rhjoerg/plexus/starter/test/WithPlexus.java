package ch.rhjoerg.plexus.starter.test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@ExtendWith(PlexusExtension.class)
public @interface WithPlexus
{
	Class<?>[] value() default {};
}
