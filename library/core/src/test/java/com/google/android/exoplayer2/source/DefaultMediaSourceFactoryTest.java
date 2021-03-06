/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.source;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import app.judo.shaded.exoplayer2.source.ClippingMediaSource;
import app.judo.shaded.exoplayer2.source.DefaultMediaSourceFactory;
import app.judo.shaded.exoplayer2.source.MediaSource;
import app.judo.shaded.exoplayer2.source.MergingMediaSource;
import app.judo.shaded.exoplayer2.source.ProgressiveMediaSource;
import app.judo.shaded.exoplayer2.C;
import app.judo.shaded.exoplayer2.MediaItem;
import app.judo.shaded.exoplayer2.source.ads.AdsLoader;
import app.judo.shaded.exoplayer2.source.ads.AdsMediaSource;
import app.judo.shaded.exoplayer2.util.MimeTypes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link DefaultMediaSourceFactory}. */
@RunWith(AndroidJUnit4.class)
public final class DefaultMediaSourceFactoryTest {

  private static final String URI_MEDIA = "http://exoplayer.dev/video";
  private static final String URI_TEXT = "http://exoplayer.dev/text";

  @Test
  public void createMediaSource_fromMediaItem_returnsSameMediaItemInstance() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem = new MediaItem.Builder().setUri(URI_MEDIA).build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource.getMediaItem()).isSameInstanceAs(mediaItem);
  }

  @Test
  public void createMediaSource_withoutMimeType_progressiveSource() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem = new MediaItem.Builder().setUri(URI_MEDIA).build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(ProgressiveMediaSource.class);
  }

  @Test
  @SuppressWarnings("deprecation") // Testing deprecated MediaSource.getTag() still works.
  public void createMediaSource_withTag_tagInSource_deprecated() {
    Object tag = new Object();
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem = new MediaItem.Builder().setUri(URI_MEDIA).setTag(tag).build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource.getTag()).isEqualTo(tag);
  }

  @Test
  public void createMediaSource_withPath_progressiveSource() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem = new MediaItem.Builder().setUri(URI_MEDIA + "/file.mp3").build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(ProgressiveMediaSource.class);
  }

  @Test
  public void createMediaSource_withNull_usesNonNullDefaults() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem = new MediaItem.Builder().setUri(URI_MEDIA).build();

    MediaSource mediaSource =
        defaultMediaSourceFactory
            .setDrmSessionManager(null)
            .setDrmHttpDataSourceFactory(null)
            .setLoadErrorHandlingPolicy(null)
            .createMediaSource(mediaItem);

    assertThat(mediaSource).isNotNull();
  }

  @Test
  public void createMediaSource_withSubtitle_isMergingMediaSource() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    List<MediaItem.Subtitle> subtitles =
        Arrays.asList(
            new MediaItem.Subtitle(Uri.parse(URI_TEXT), MimeTypes.APPLICATION_TTML, "en"),
            new MediaItem.Subtitle(
                Uri.parse(URI_TEXT), MimeTypes.APPLICATION_TTML, "de", C.SELECTION_FLAG_DEFAULT));
    MediaItem mediaItem = new MediaItem.Builder().setUri(URI_MEDIA).setSubtitles(subtitles).build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(MergingMediaSource.class);
  }

  @Test
  @SuppressWarnings("deprecation") // Testing deprecated MediaSource.getTag() still works.
  public void createMediaSource_withSubtitle_hasTag_deprecated() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    Object tag = new Object();
    MediaItem mediaItem =
        new MediaItem.Builder()
            .setTag(tag)
            .setUri(URI_MEDIA)
            .setSubtitles(
                Collections.singletonList(
                    new MediaItem.Subtitle(Uri.parse(URI_TEXT), MimeTypes.APPLICATION_TTML, "en")))
            .build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource.getTag()).isEqualTo(tag);
  }

  @Test
  public void createMediaSource_withStartPosition_isClippingMediaSource() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem =
        new MediaItem.Builder().setUri(URI_MEDIA).setClipStartPositionMs(1000L).build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(ClippingMediaSource.class);
  }

  @Test
  public void createMediaSource_withEndPosition_isClippingMediaSource() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem =
        new MediaItem.Builder().setUri(URI_MEDIA).setClipEndPositionMs(1000L).build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(ClippingMediaSource.class);
  }

  @Test
  public void createMediaSource_relativeToDefaultPosition_isClippingMediaSource() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem =
        new MediaItem.Builder().setUri(URI_MEDIA).setClipRelativeToDefaultPosition(true).build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(ClippingMediaSource.class);
  }

  @Test
  public void createMediaSource_defaultToEnd_isNotClippingMediaSource() {
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());
    MediaItem mediaItem =
        new MediaItem.Builder()
            .setUri(URI_MEDIA)
            .setClipEndPositionMs(C.TIME_END_OF_SOURCE)
            .build();

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(ProgressiveMediaSource.class);
  }

  @Test
  public void getSupportedTypes_coreModule_onlyOther() {
    int[] supportedTypes =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext())
            .getSupportedTypes();

    assertThat(supportedTypes).asList().containsExactly(C.TYPE_OTHER);
  }

  @Test
  public void createMediaSource_withAdTagUri_callsAdsLoader() {
    Uri adTagUri = Uri.parse(URI_MEDIA);
    MediaItem mediaItem = new MediaItem.Builder().setUri(URI_MEDIA).setAdTagUri(adTagUri).build();
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext())
            .setAdsLoaderProvider(ignoredAdTagUri -> mock(AdsLoader.class))
            .setAdViewProvider(mock(AdsLoader.AdViewProvider.class));

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isInstanceOf(AdsMediaSource.class);
  }

  @Test
  public void createMediaSource_withAdTagUri_adProvidersNotSet_playsWithoutAdNoException() {
    MediaItem mediaItem =
        new MediaItem.Builder().setUri(URI_MEDIA).setAdTagUri(Uri.parse(URI_MEDIA)).build();
    DefaultMediaSourceFactory defaultMediaSourceFactory =
        new DefaultMediaSourceFactory((Context) ApplicationProvider.getApplicationContext());

    MediaSource mediaSource = defaultMediaSourceFactory.createMediaSource(mediaItem);

    assertThat(mediaSource).isNotInstanceOf(AdsMediaSource.class);
  }

  @Test
  public void createMediaSource_withAdTagUriProvidersNull_playsWithoutAdNoException() {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    MediaItem mediaItem =
        new MediaItem.Builder().setUri(URI_MEDIA).setAdTagUri(Uri.parse(URI_MEDIA)).build();

    MediaSource mediaSource =
        new DefaultMediaSourceFactory(applicationContext).createMediaSource(mediaItem);

    assertThat(mediaSource).isNotInstanceOf(AdsMediaSource.class);
  }
}
