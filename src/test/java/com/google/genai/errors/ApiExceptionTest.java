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

package com.google.genai.errors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.junit.jupiter.api.Test;

class ApiExceptionTest {

  @Test
  void testThrowFromResponse_OKStatus_DoesNotThrow() {
    CloseableHttpResponse response = createFakeResponse(HttpStatus.SC_OK, "OK", "");

    assertDoesNotThrow(() -> ApiException.throwFromResponse(response));
  }

  @Test
  void testThrowFromResponse_ClientError_ThrowsClientException() {
    String status = "Bad Request";
    String message = "Invalid request";
    String jsonBody = String.format("{ \"error\": { \"message\": \"%s\" } }", message);
    CloseableHttpResponse response =
        createFakeResponse(HttpStatus.SC_BAD_REQUEST, status, jsonBody);

    ClientException thrown =
        assertThrows(ClientException.class, () -> ApiException.throwFromResponse(response));

    assertInstanceOf(ClientException.class, thrown);
    assertEquals(HttpStatus.SC_BAD_REQUEST, thrown.code());
    assertEquals(status, thrown.status());
    assertEquals(message, thrown.message());
    assertEquals(
        String.format("%d %s. %s", HttpStatus.SC_BAD_REQUEST, status, message),
        thrown.getMessage());
  }

  @Test
  void testThrowFromResponse_ServerError_ThrowsServerException() throws IOException {
      String status = "Internal Server Error";
      String message = "Server fault";
    String jsonBody = String.format("{ \"error\": { \"message\": \"%s\" } }", message);

    CloseableHttpResponse response =
        createFakeResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, status, jsonBody);

    // Act & Assert
    ServerException thrown =
        assertThrows(ServerException.class, () -> ApiException.throwFromResponse(response));

    assertInstanceOf(ServerException.class, thrown);
    assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, thrown.code());
    assertEquals(status, thrown.status());
    assertEquals(message, thrown.message());
    assertEquals(
        String.format("%d %s. %s", HttpStatus.SC_INTERNAL_SERVER_ERROR, status, message),
        thrown.getMessage());
  }

  @Test
  void testThrowFromResponse_OtherError_ThrowsApiException() throws IOException {
    String status = "Moved Permanently";

    CloseableHttpResponse response =
        createFakeResponse(HttpStatus.SC_MOVED_PERMANENTLY, status, "");

    ApiException thrown =
        assertThrows(ApiException.class, () -> ApiException.throwFromResponse(response));

    assertFalse(thrown instanceof ClientException);
    assertFalse(thrown instanceof ServerException);

    assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, thrown.code());
    assertEquals(status, thrown.status());
    assertEquals("", thrown.message());
    assertEquals(
        String.format("%d %s. ", HttpStatus.SC_MOVED_PERMANENTLY, status), thrown.getMessage());
  }

  @Test
  void testGetErrorMessage_ValidMessage() {
    CloseableHttpResponse response =
        createFakeResponse(
            HttpStatus.SC_BAD_REQUEST, "Bad", "{ \"error\": { \"message\": \"test message\" } }");
    String message = ApiException.getErrorMessageFromResponse(response);
    assertEquals("test message", message);
  }

  @Test
  void testGetErrorMessage_NoErrorNode() {
    CloseableHttpResponse response = createFakeResponse(HttpStatus.SC_BAD_REQUEST, "Bad", "{}");
    String message = ApiException.getErrorMessageFromResponse(response);
    assertEquals("", message);
  }

  @Test
  void testGetErrorMessage_NoMessageNode() {
    CloseableHttpResponse response =
        createFakeResponse(HttpStatus.SC_BAD_REQUEST, "Bad", "{ \"error\": {} }");
    String message = ApiException.getErrorMessageFromResponse(response);
    assertEquals("", message);
  }

  @Test
  void testGetErrorMessage_MessageNotText() {
    CloseableHttpResponse response =
        createFakeResponse(HttpStatus.SC_BAD_REQUEST, "Bad", "{ \"error\": {\"message\": 123} }");
    String message = ApiException.getErrorMessageFromResponse(response);
    assertEquals("", message);
  }

  @Test
  void testGetErrorMessage_InvalidJson() {
    CloseableHttpResponse response =
        createFakeResponse(HttpStatus.SC_BAD_REQUEST, "Bad", "not json");
    String message = ApiException.getErrorMessageFromResponse(response);
    // JsonProcessingException is caught and ignored, returns ""
    assertEquals("", message);
  }

  @Test
  void testGetErrorMessage_EmptyBody() {
    CloseableHttpResponse response = createFakeResponse(HttpStatus.SC_BAD_REQUEST, "Bad", "");
    String message = ApiException.getErrorMessageFromResponse(response);
    assertEquals("", message);
  }

  // Helper Methods
  private static CloseableHttpResponse createFakeResponse(
      int statusCode, String reasonPhrase, String responseBody) {
    HttpEntity entity = (responseBody != null) ? new FakeHttpEntity(responseBody) : null;
    return createFakeResponse(statusCode, reasonPhrase, entity);
  }

  private static CloseableHttpResponse createFakeResponse(
      int statusCode, String reasonPhrase, HttpEntity entity) {
    StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, reasonPhrase);
    return new FakeHttpResponse(statusLine, entity);
  }

  // Fake/Stub Implementations
  static class FakeHttpResponse implements CloseableHttpResponse {
    private final StatusLine statusLine;
    private final HttpEntity entity;

    FakeHttpResponse(StatusLine statusLine, HttpEntity entity) {
      this.statusLine = statusLine;
      this.entity = entity;
    }

    @Override
    public StatusLine getStatusLine() {
      return statusLine;
    }

    @Override
    public HttpEntity getEntity() {
      return entity;
    }

    @Override
    public void close() throws IOException {}

    // --- Minimal implementation of other methods ---
    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }

    @Override
    public void setStatusLine(StatusLine statusline) {}

    @Override
    public void setStatusLine(org.apache.http.ProtocolVersion ver, int code) {}

    @Override
    public void setStatusLine(org.apache.http.ProtocolVersion ver, int code, String reason) {}

    @Override
    public void setStatusCode(int code) throws IllegalStateException {}

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {}

    @Override
    public void setEntity(HttpEntity entity) {}

    @Override
    public void setLocale(Locale loc) {}

    @Override
    public org.apache.http.ProtocolVersion getProtocolVersion() {
      return HttpVersion.HTTP_1_1;
    }

    @Override
    public boolean containsHeader(String name) {
      return false;
    }

    @Override
    public Header[] getHeaders(String name) {
      return new Header[0];
    }

    @Override
    public Header getFirstHeader(String name) {
      return null;
    }

    @Override
    public Header getLastHeader(String name) {
      return null;
    }

    @Override
    public Header[] getAllHeaders() {
      return new Header[0];
    }

    @Override
    public void addHeader(Header header) {}

    @Override
    public void addHeader(String name, String value) {}

    @Override
    public void setHeader(Header header) {}

    @Override
    public void setHeader(String name, String value) {}

    @Override
    public void setHeaders(Header[] headers) {}

    @Override
    public void removeHeader(Header header) {}

    @Override
    public void removeHeaders(String name) {}

    @Override
    public org.apache.http.HeaderIterator headerIterator() {
      return null;
    }

    @Override
    public org.apache.http.HeaderIterator headerIterator(String name) {
      return null;
    }

    @Override
    public HttpParams getParams() {
      return null;
    }

    @Override
    public void setParams(HttpParams params) {}
  }

  static class FakeHttpEntity implements HttpEntity {
    private final InputStream content;
    private final long length;
    private final boolean repeatable;

    FakeHttpEntity(String contentString) {
      if (contentString != null) {
        byte[] bytes = contentString.getBytes(StandardCharsets.UTF_8);
        this.content = new ByteArrayInputStream(bytes);
        this.length = bytes.length;
        this.repeatable = true;
      } else {
        this.content = null;
        this.length = 0;
        this.repeatable = true;
      }
    }

    FakeHttpEntity(InputStream inputStream) {
      this.content = inputStream;
      this.length = -1;
      this.repeatable = inputStream.markSupported();
    }

    @Override
    public boolean isRepeatable() {
      return repeatable;
    }

    @Override
    public boolean isChunked() {
      return false;
    }

    @Override
    public long getContentLength() {
      return length;
    }

    @Override
    public Header getContentType() {
      return null;
    }

    @Override
    public Header getContentEncoding() {
      return null;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
      if (content == null) {
        return null; // EntityUtils.toString handles null stream input
      }
      // Reset if repeatable and mark is supported
      if (isRepeatable() && content.markSupported()) {
        try {
          // Ensure mark was called if needed, or assume start for ByteArrayInputStream
          if (content instanceof ByteArrayInputStream) {
            content.reset();
          }
        } catch (IOException e) {
          // Should not happen with ByteArrayInputStream that supports mark/reset
          throw new RuntimeException("Failed to reset repeatable stream", e);
        }
      }
      return content;
    }

    @Override
    public void writeTo(java.io.OutputStream outstream) throws IOException {}

    @Override
    public boolean isStreaming() {
      return !isRepeatable();
    }

    @Override
    public void consumeContent() throws IOException {}
  }

  static class FaultyInputStream extends InputStream {
    @Override
    public int read() throws IOException {
      throw new IOException("Simulated read error");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      throw new IOException("Simulated read error");
    }
  }
}
