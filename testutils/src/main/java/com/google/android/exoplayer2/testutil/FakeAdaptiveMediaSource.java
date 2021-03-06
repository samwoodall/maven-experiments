/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.google.android.exoplayer2.testutil;

import androidx.annotation.Nullable;
import app.judo.shaded.exoplayer2.Timeline;
import app.judo.shaded.exoplayer2.Timeline.Period;
import app.judo.shaded.exoplayer2.drm.DrmSessionEventListener;
import app.judo.shaded.exoplayer2.drm.DrmSessionManager;
import app.judo.shaded.exoplayer2.source.MediaSource;
import app.judo.shaded.exoplayer2.source.MediaSourceEventListener;
import app.judo.shaded.exoplayer2.source.TrackGroupArray;
import app.judo.shaded.exoplayer2.upstream.Allocator;
import app.judo.shaded.exoplayer2.upstream.TransferListener;
import app.judo.shaded.exoplayer2.util.Util;

/**
 * Fake {@link MediaSource} that provides a given timeline. Creating the period returns a
 * {@link FakeAdaptiveMediaPeriod} from the given {@link TrackGroupArray}.
 */
public class FakeAdaptiveMediaSource extends FakeMediaSource {

  private final FakeChunkSource.Factory chunkSourceFactory;

  public FakeAdaptiveMediaSource(
      Timeline timeline,
      TrackGroupArray trackGroupArray,
      FakeChunkSource.Factory chunkSourceFactory) {
    super(
        timeline,
        DrmSessionManager.DUMMY,
        /* trackDataFactory= */ (unusedFormat, unusedMediaPeriodId) -> {
          throw new RuntimeException("Unused TrackDataFactory");
        },
        trackGroupArray);
    this.chunkSourceFactory = chunkSourceFactory;
  }

  @Override
  protected FakeMediaPeriod createFakeMediaPeriod(
      MediaPeriodId id,
      TrackGroupArray trackGroupArray,
      Allocator allocator,
      MediaSourceEventListener.EventDispatcher mediaSourceEventDispatcher,
      DrmSessionManager drmSessionManager,
      DrmSessionEventListener.EventDispatcher drmEventDispatcher,
      @Nullable TransferListener transferListener) {
    Period period = Util.castNonNull(getTimeline()).getPeriodByUid(id.periodUid, new Period());
    return new FakeAdaptiveMediaPeriod(
        trackGroupArray,
        mediaSourceEventDispatcher,
        allocator,
        chunkSourceFactory,
        period.durationUs,
        transferListener);
  }

}
