package modules;

import com.google.inject.AbstractModule;
import services.YouTubeService;

/**
 * Module to configure dependency injection.
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(YouTubeService.class).toProvider(YouTubeServiceProvider.class).asEagerSingleton();
    }
}
