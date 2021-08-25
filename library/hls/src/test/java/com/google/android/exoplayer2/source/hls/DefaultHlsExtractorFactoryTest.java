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
package com.google.android.exoplayer2.source.hls;

import static com.google.common.truth.Truth.assertThat;

import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import app.judo.shaded.exoplayer2.source.hls.BundledHlsMediaChunkExtractor;
import app.judo.shaded.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import app.judo.shaded.exoplayer2.source.hls.WebvttExtractor;
import app.judo.shaded.exoplayer2.Format;
import app.judo.shaded.exoplayer2.extractor.DefaultExtractorsFactory;
import app.judo.shaded.exoplayer2.extractor.ExtractorInput;
import app.judo.shaded.exoplayer2.extractor.mp3.Mp3Extractor;
import app.judo.shaded.exoplayer2.extractor.ts.Ac3Extractor;
import app.judo.shaded.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.testutil.FakeExtractorInput;
import com.google.android.exoplayer2.testutil.TestUtil;
import app.judo.shaded.exoplayer2.util.MimeTypes;
import app.judo.shaded.exoplayer2.util.TimestampAdjuster;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link DefaultExtractorsFactory}. */
@RunWith(AndroidJUnit4.class)
public class DefaultHlsExtractorFactoryTest {

  private Uri tsUri;
  private Format webVttFormat;
  private TimestampAdjuster timestampAdjuster;
  private Map<String, List<String>> ac3ResponseHeaders;

  @Before
  public void setUp() {
    tsUri = Uri.parse("http://path/filename.ts");
    webVttFormat = new Format.Builder().setSampleMimeType(MimeTypes.TEXT_VTT).build();
    timestampAdjuster = new TimestampAdjuster(/* firstSampleTimestampUs= */ 0);
    ac3ResponseHeaders = new HashMap<>();
    ac3ResponseHeaders.put("Content-Type", Collections.singletonList(MimeTypes.AUDIO_AC3));
  }

  @Test
  public void createExtractor_withFileTypeInFormat_returnsExtractorMatchingFormat()
      throws Exception {
    ExtractorInput webVttExtractorInput =
        new FakeExtractorInput.Builder()
            .setData(
                TestUtil.getByteArray(
                    ApplicationProvider.getApplicationContext(), "media/webvtt/typical"))
            .build();

    BundledHlsMediaChunkExtractor result =
        new DefaultHlsExtractorFactory()
            .createExtractor(
                tsUri,
                webVttFormat,
                /* muxedCaptionFormats= */ null,
                timestampAdjuster,
                ac3ResponseHeaders,
                webVttExtractorInput);

    assertThat(result.extractor.getClass()).isEqualTo(WebvttExtractor.class);
  }

  @Test
  public void
      createExtractor_withFileTypeInResponseHeaders_returnsExtractorMatchingResponseHeaders()
          throws Exception {
    ExtractorInput ac3ExtractorInput =
        new FakeExtractorInput.Builder()
            .setData(
                TestUtil.getByteArray(
                    ApplicationProvider.getApplicationContext(), "media/ts/sample.ac3"))
            .build();

    BundledHlsMediaChunkExtractor result =
        new DefaultHlsExtractorFactory()
            .createExtractor(
                tsUri,
                webVttFormat,
                /* muxedCaptionFormats= */ null,
                timestampAdjuster,
                ac3ResponseHeaders,
                ac3ExtractorInput);

    assertThat(result.extractor.getClass()).isEqualTo(Ac3Extractor.class);
  }

  @Test
  public void createExtractor_withFileTypeInUri_returnsExtractorMatchingUri() throws Exception {
    ExtractorInput tsExtractorInput =
        new FakeExtractorInput.Builder()
            .setData(
                TestUtil.getByteArray(
                    ApplicationProvider.getApplicationContext(), "media/ts/sample_ac3.ts"))
            .build();

    BundledHlsMediaChunkExtractor result =
        new DefaultHlsExtractorFactory()
            .createExtractor(
                tsUri,
                webVttFormat,
                /* muxedCaptionFormats= */ null,
                timestampAdjuster,
                ac3ResponseHeaders,
                tsExtractorInput);

    assertThat(result.extractor.getClass()).isEqualTo(TsExtractor.class);
  }

  @Test
  public void createExtractor_withFileTypeNotInMediaInfo_returnsExpectedExtractor()
      throws Exception {
    ExtractorInput mp3ExtractorInput =
        new FakeExtractorInput.Builder()
            .setData(
                TestUtil.getByteArray(
                    ApplicationProvider.getApplicationContext(), "media/mp3/bear-id3.mp3"))
            .build();

    BundledHlsMediaChunkExtractor result =
        new DefaultHlsExtractorFactory()
            .createExtractor(
                tsUri,
                webVttFormat,
                /* muxedCaptionFormats= */ null,
                timestampAdjuster,
                ac3ResponseHeaders,
                mp3ExtractorInput);

    assertThat(result.extractor.getClass()).isEqualTo(Mp3Extractor.class);
  }

  @Test
  public void createExtractor_withNoMatchingExtractor_fallsBackOnTsExtractor() throws Exception {
    ExtractorInput emptyExtractorInput = new FakeExtractorInput.Builder().build();

    BundledHlsMediaChunkExtractor result =
        new DefaultHlsExtractorFactory()
            .createExtractor(
                tsUri,
                webVttFormat,
                /* muxedCaptionFormats= */ null,
                timestampAdjuster,
                ac3ResponseHeaders,
                emptyExtractorInput);

    assertThat(result.extractor.getClass()).isEqualTo(TsExtractor.class);
  }
}
