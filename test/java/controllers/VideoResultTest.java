package controllers;

import org.junit.Test;
import static org.junit.Assert.*;

public class VideoResultTest {

    @Test
    public void testGetters() {
        String title = "Test Title";
        String description = "Test Description";
        String videoId = "video123";
        String channelId = "channel123";
        String thumbnailUrl = "http://example.com/thumb.jpg";

        VideoResult videoResult = new VideoResult(title, description, videoId, channelId, thumbnailUrl);

        assertEquals(title, videoResult.getTitle());
        assertEquals(description, videoResult.getDescription());
        assertEquals("https://www.youtube.com/watch?v=" + videoId, videoResult.getVideoUrl());
        assertEquals("https://www.youtube.com/channel/" + channelId, videoResult.getChannelUrl());
        assertEquals(thumbnailUrl, videoResult.getThumbnailUrl());
    }
}
