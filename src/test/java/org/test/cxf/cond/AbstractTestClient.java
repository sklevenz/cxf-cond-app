package org.test.cxf.cond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
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

public abstract class AbstractTestClient {

  private HttpClient client;
  private TestServer server;

  private static final String LAST_MODIFIED_DATE_MATCH = "Sat, 29 Oct 1994 10:00:00 GMT";
  private static final String LAST_MODIFIED_DATE_NO_MATCH = "Sat, 29 Oct 1994 09:00:00 GMT";

  private static final String FORMAT_STRING = "| %-6S | %-14s | %-12s | %-20s | %-32s | %6s | %8s | %1s%1s |";

  @Before
  public void before() throws Exception {
    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
    Date lastModifiedDate = df.parse(LAST_MODIFIED_DATE_MATCH);
    RestResource.setLastModifiedDate(lastModifiedDate);

    client = new DefaultHttpClient();
    server = new TestServer();
    server.start();
  }

  @After
  public void after() {
    server.stop();
  }

  @BeforeClass
  public static void beforeClass() {
    System.out.println(String.format(FORMAT_STRING, "Method", "eTag Header", "eTag Value", "LMD Header", "LMD Value", "Status", "Expected", "E", "L"));
    System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
  }

  @AfterClass
  public static void afterClass() {}

  protected abstract String eTagMatchString();

  protected abstract String eTagNoMatchString();

  @Test
  public void testPost_eTag() {
    RestResource.setLastModifiedDate(null);

    test(HttpMethodEnum.POST, null, null, null, null, Status.CREATED);

    test(HttpMethodEnum.POST, HttpHeaders.IF_MATCH, eTagNoMatchString(), null, null, Status.CREATED);
    test(HttpMethodEnum.POST, HttpHeaders.IF_MATCH, eTagMatchString(), null, null, Status.CREATED);
    test(HttpMethodEnum.POST, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), null, null, Status.CREATED);
    test(HttpMethodEnum.POST, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), null, null, Status.CREATED);
  }

  @Test
  public void testPost_lastModifiedDate_eTag() {
    RestResource.setETag(null);

    test(HttpMethodEnum.POST, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.CREATED);
    test(HttpMethodEnum.POST, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.CREATED);
    test(HttpMethodEnum.POST, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.CREATED);
    test(HttpMethodEnum.POST, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.CREATED);
  }

  @Test
  public void testPost_lastModifiedDate() {
    test(HttpMethodEnum.POST, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.CREATED);
    test(HttpMethodEnum.POST, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.CREATED);
    test(HttpMethodEnum.POST, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.CREATED);
    test(HttpMethodEnum.POST, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.CREATED);
  }

  @Test
  public void testPut_eTag() {
    RestResource.setLastModifiedDate(null);

    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagNoMatchString(), null, null, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagMatchString(), null, null, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), null, null, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), null, null, Status.PRECONDITION_FAILED);
  }

  @Test
  public void testPut_lastModifiedDate() {
    RestResource.setETag(null);

    test(HttpMethodEnum.PUT, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.PUT, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PUT, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
  }

  @Test
  public void testPut_lastModifiedDate_eTag() {

    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);

    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);

    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);

    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PUT, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
  }

  @Test
  public void test_428() {
    test(HttpMethodEnum.PATCH, null, null, null, null, 428);
    test(HttpMethodEnum.PUT, null, null, null, null, 428);
    test(HttpMethodEnum.DELETE, null, null, null, null, 428);
  }

  @Test
  @Ignore
  public void testPatch_eTag() {
    RestResource.setLastModifiedDate(null);

    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagNoMatchString(), null, null, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagMatchString(), null, null, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), null, null, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), null, null, Status.PRECONDITION_FAILED);
  }

  @Test
  @Ignore
  public void testPatch_lastModifiedDate() {
    RestResource.setETag(null);

    test(HttpMethodEnum.PATCH, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.PATCH, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PATCH, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
  }

  @Test
  @Ignore
  public void testPatch_lastModifiedDate_eTag() {

    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);

    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);

    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);

    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.OK);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.PATCH, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.OK);
  }

  @Test
  public void testDelete_eTag() {
    RestResource.setLastModifiedDate(null);

    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagNoMatchString(), null, null, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagMatchString(), null, null, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), null, null, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), null, null, Status.PRECONDITION_FAILED);
  }

  @Test
  public void testDelete_lastModifiedDate() {
    RestResource.setETag(null);

    test(HttpMethodEnum.DELETE, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, null, null, HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.DELETE, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.DELETE, null, null, HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NO_CONTENT);
  }

  @Test
  public void testDelete_lastModifiedDate_eTag() {

    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NO_CONTENT);

    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NO_CONTENT);

    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NO_CONTENT);

    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.NO_CONTENT);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_MODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NOT_MODIFIED);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_NO_MATCH, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.DELETE, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), HttpHeaders.IF_UNMODIFIED_SINCE, LAST_MODIFIED_DATE_MATCH, Status.NO_CONTENT);
  }

  @Test
  public void testGet() {
    RestResource.setLastModifiedDate(null);

    test(HttpMethodEnum.GET, null, null, null, null, Status.OK);

    test(HttpMethodEnum.GET, HttpHeaders.IF_MATCH, eTagNoMatchString(), null, null, Status.PRECONDITION_FAILED);
    test(HttpMethodEnum.GET, HttpHeaders.IF_MATCH, eTagMatchString(), null, null, Status.OK);
    test(HttpMethodEnum.GET, HttpHeaders.IF_NONE_MATCH, eTagNoMatchString(), null, null, Status.OK);
    test(HttpMethodEnum.GET, HttpHeaders.IF_NONE_MATCH, eTagMatchString(), null, null, Status.NOT_MODIFIED);
  }

  private void test(HttpMethodEnum method, String eTagHeader, String eTagHeaderValue, String lmdHeader, String lmdHeaderValue, Status status) {
    test(method, eTagHeader, eTagHeaderValue, lmdHeader, lmdHeaderValue, status.getStatusCode());
  }

  private void test(HttpMethodEnum method, String eTagHeader, String eTagHeaderValue, String lmdHeader, String lmdHeaderValue, int status) {
    HashMap<String, String> headers = new HashMap<String, String>();
    String eTagHeaderLabel = "x";
    String eTagHeaderValueLabel = "x";
    String lmdHeaderLabel = "x";
    String lmdHeaderValueLabel = "x";
    String eTagMatch = ".";
    String lmdMatch = ".";

    if (eTagHeader != null) {
      headers.put(eTagHeader, eTagHeaderValue);
      eTagHeaderLabel = eTagHeader;
      eTagHeaderValueLabel = eTagHeaderValue;

      eTagMatch = eTagHeaderValue.equals(eTagMatchString()) ? "+" : "-";
    }

    if (lmdHeader != null) {
      headers.put(lmdHeader, lmdHeaderValue);
      lmdHeaderLabel = lmdHeader;
      lmdHeaderValueLabel = lmdHeaderValue;
      lmdMatch = lmdHeaderValue.equals(LAST_MODIFIED_DATE_MATCH) ? "+" : "-";
    }

    HttpResponse response = execute(method, "/" + method.toString(), headers);
    assertNotNull(response);

    String expected = " ";
    if (status != response.getStatusLine().getStatusCode()) {
      expected = Integer.toString(status);
    }
    System.out.println(String.format(FORMAT_STRING, method.toString(), eTagHeaderLabel, eTagHeaderValueLabel, lmdHeaderLabel, lmdHeaderValueLabel, response.getStatusLine().getStatusCode(), expected, eTagMatch, lmdMatch));
//    assertEquals(status, response.getStatusLine().getStatusCode());
  }

  private HttpResponse execute(HttpMethodEnum method, String path, HashMap<String, String> headers) {
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
      case POST:
        hrb = new HttpPost(uri);
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

      if (null != headers) {
        for (String header : headers.keySet())
          hrb.setHeader(header, headers.get(header));
      }

      response = client.execute(hrb);
      hrb.reset();
      return response;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
