package modules;

import com.google.inject.Inject;
import com.google.inject.Provider;
import models.YouTubeService;

/**
 * Provider for YouTubeService to allow dependency injection.
 */
public class YouTubeServiceProvider implements Provider<YouTubeService> {

    @Inject
    public YouTubeServiceProvider() {
        // Constructor can be used to inject configurations if needed
    }

    @Override
    public YouTubeService get() {
        return new YouTubeService();
    }
}
