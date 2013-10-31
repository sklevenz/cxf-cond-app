package org.test.cxf.cond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.test.cxf.cond.rest.RestResource;

@Ignore
public class TestClient {

  private HttpClient client;
  private TestServer server;

  @Before
  public void before() throws Exception {
    client = new DefaultHttpClient();
    server = new TestServer();
    server.start();
  }

  @After
  public void after() {
    server.stop();
  }

  @BeforeClass
  public static void beforeClass() {}

  @AfterClass
  public static void afterClass() {}

  @Test
  public void fetch() throws Exception {
    HttpResponse response = execute(HttpMethodEnum.GET, "/fetch", null, null);

    assertNotNull(response);
    assertEquals(200, response.getStatusLine().getStatusCode());
    assertNotNull(response.getFirstHeader(HttpHeaders.LAST_MODIFIED));
    assertNotNull(response.getFirstHeader(HttpHeaders.ETAG));
    String lastModifiedDateHeader = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();
    String eTagHeader = response.getFirstHeader(HttpHeaders.ETAG).getValue();

    assertEquals(200, response.getStatusLine().getStatusCode());
    assertEquals(RestResource.LAST_MODIFIED_DATE, lastModifiedDateHeader);
    assertEquals("\"" + RestResource.ETAG + "\"", eTagHeader);
  }

  @Test
  public void put() throws Exception {
    HttpResponse response = execute(HttpMethodEnum.PUT, "/put", null, null);

    assertNotNull(response);
    assertEquals(200, response.getStatusLine().getStatusCode());
  }

  private HttpResponse execute(HttpMethodEnum method, String path, String eTagHeaderValue, String lastModifiedDateHeaderValue) {
    try {
      URI uri = URI.create(server.getEndpoint() + path);
      HttpResponse response;
      HttpRequestBase hrb;
      switch (method) {
      case GET:
        hrb = new HttpGet(uri);
        break;
      case PUT:
        hrb = new HttpPut(uri);
        break;
      case PATCH:
        hrb = new HttpPatch(uri);
        break;
      //      case Merge:
      //        hrb = new HttpMerge(uri);
      //        break;
      case DELETE:
        hrb = new HttpDelete(uri);
        break;
      default:
        throw new RuntimeException("Method unsupported: " + method);
      }

      if (null != eTagHeaderValue) {
        hrb.setHeader(HttpHeaders.ETAG, eTagHeaderValue);
      }

      if (null != lastModifiedDateHeaderValue) {
        hrb.setHeader(HttpHeaders.LAST_MODIFIED, lastModifiedDateHeaderValue);
      }

      response = client.execute(hrb);
      hrb.reset();
      return response;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
