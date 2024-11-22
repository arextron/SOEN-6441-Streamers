package controllers;

import models.VideoResult;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VideoResultTest {

    @Test
    public void testConstructorAndGetters() {
        String title = "Test Title";
        String description = "Test Description";
        String videoId = "videoId123";
        String channelId = "channelId123";
        String thumbnailUrl = "http://thumbnail.url";
        String channelTitle = "Channel Title";
        List<String> tags = Arrays.asList("tag1", "tag2");

        VideoResult videoResult = new VideoResult(
                title,
                description,
                videoId,
                channelId,
                thumbnailUrl,
                channelTitle,
                tags
        );

        assertEquals(title, videoResult.getTitle());
        assertEquals(description, videoResult.getDescription());
        assertEquals("https://www.youtube.com/watch?v=" + videoId, videoResult.getVideoUrl());
        assertEquals("https://www.youtube.com/channel/" + channelId, videoResult.getChannelUrl());
        assertEquals(thumbnailUrl, videoResult.getThumbnailUrl());
        assertEquals(channelTitle, videoResult.getChannelTitle());
        assertEquals(channelId, videoResult.getChannelId());
        assertEquals(tags, videoResult.getTags());
    }

    @Test
    public void testConstructorWithNullTags() {
        VideoResult videoResult = new VideoResult(
                "Title",
                "Description",
                "videoId",
                "channelId",
                "thumbnailUrl",
                "channelTitle",
                null
        );

        assertNotNull(videoResult.getTags());
        assertTrue(videoResult.getTags().isEmpty());
    }

    @Test
    public void testGetVideoUrl() {
        String videoId = "videoId123";
        VideoResult videoResult = new VideoResult(
                "Title",
                "Description",
                videoId,
                "channelId",
                "thumbnailUrl",
                "channelTitle",
                null
        );

        String expectedUrl = "https://www.youtube.com/watch?v=" + videoId;
        assertEquals(expectedUrl, videoResult.getVideoUrl());
    }

    @Test
    public void testGetChannelUrl() {
        String channelId = "channelId123";
        VideoResult videoResult = new VideoResult(
                "Title",
                "Description",
                "videoId",
                channelId,
                "thumbnailUrl",
                "channelTitle",
                null
        );

        String expectedUrl = "https://www.youtube.com/channel/" + channelId;
        assertEquals(expectedUrl, videoResult.getChannelUrl());
    }

    @Test
    public void testGetTagsEmptyList() {
        VideoResult videoResult = new VideoResult(
                "Title",
                "Description",
                "videoId",
                "channelId",
                "thumbnailUrl",
                "channelTitle",
                Collections.emptyList()
        );

        assertNotNull(videoResult.getTags());
        assertTrue(videoResult.getTags().isEmpty());
    }
}
