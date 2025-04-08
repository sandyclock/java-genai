/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Auto-generated code. Do not edit.

package com.google.genai.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.genai.JsonSerializable;
import java.util.Optional;

/**
 * Configuration of session resumption mechanism.
 *
 * <p>Included in `BidiGenerateContentSetup.session_resumption`. If included server will send
 * SessionResumptionUpdate messages.
 */
@AutoValue
@JsonDeserialize(builder = LiveClientSessionResumptionConfig.Builder.class)
public abstract class LiveClientSessionResumptionConfig extends JsonSerializable {
  /**
   * Session resumption handle of previous session (session to restore).
   *
   * <p>If not present new session will be started.
   */
  @JsonProperty("handle")
  public abstract Optional<String> handle();

  /**
   * If set the server will to send `last_consumed_client_message_index` in the
   * `session_resumption_update` messages to allow for transparent reconnections.
   */
  @JsonProperty("transparent")
  public abstract Optional<Boolean> transparent();

  /** Instantiates a builder for LiveClientSessionResumptionConfig. */
  public static Builder builder() {
    return new AutoValue_LiveClientSessionResumptionConfig.Builder();
  }

  /** Creates a builder with the same values as this instance. */
  public abstract Builder toBuilder();

  /** Builder for LiveClientSessionResumptionConfig. */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * For internal usage. Please use `LiveClientSessionResumptionConfig.builder()` for
     * instantiation.
     */
    @JsonCreator
    private static Builder create() {
      return new AutoValue_LiveClientSessionResumptionConfig.Builder();
    }

    @JsonProperty("handle")
    public abstract Builder handle(String handle);

    @JsonProperty("transparent")
    public abstract Builder transparent(boolean transparent);

    public abstract LiveClientSessionResumptionConfig build();
  }

  /** Deserializes a JSON string to a LiveClientSessionResumptionConfig object. */
  public static LiveClientSessionResumptionConfig fromJson(String jsonString) {
    return JsonSerializable.fromJsonString(jsonString, LiveClientSessionResumptionConfig.class);
  }
}
