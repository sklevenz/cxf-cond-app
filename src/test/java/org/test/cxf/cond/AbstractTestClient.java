package org.test.cxf.cond;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.HashMap;

import javax.ws.rs.core.EntityTag;
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
import org.junit.Test;
import org.test.cxf.cond.rest.RestResource;

public abstract class AbstractTestClient {

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

  protected abstract EntityTag eTagMatch( );
  protected abstract String eTagMatchString( );
  protected abstract String eTagNoMatchString( );
  
  @Test
  public void get_200() throws Exception {
    HttpResponse response = execute(HttpMethodEnum.GET, "/get", null);

    assertNotNull(response);
    assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertNotNull(response.getFirstHeader(HttpHeaders.LAST_MODIFIED));
    assertNotNull(response.getFirstHeader(HttpHeaders.ETAG));
    String lastModifiedDateHeader = response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();
    String eTagHeader = response.getFirstHeader(HttpHeaders.ETAG).getValue();

    assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    assertEquals(RestResource.LAST_MODIFIED_DATE, lastModifiedDateHeader);
    assertEquals("\"" + eTagMatchString() + "\"", eTagHeader);
  }

  @Test
  public void post_201() throws Exception {
    HttpResponse response = execute(HttpMethodEnum.POST, "/post", null);
    assertNotNull(response);
    assertEquals(Status.CREATED.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void put_428() throws Exception {
    HttpResponse response = execute(HttpMethodEnum.PUT, "/put", null);
    assertNotNull(response);
    assertEquals(428, response.getStatusLine().getStatusCode());
  }

  @Test
  public void patch_428() throws Exception {
    HttpResponse response = execute(HttpMethodEnum.PATCH, "/patch", null);
    assertNotNull(response);
    assertEquals(428, response.getStatusLine().getStatusCode());
  }

  @Test
  public void delete_428() throws Exception {
    HttpResponse response = execute(HttpMethodEnum.DELETE, "/delete", null);
    assertNotNull(response);
    assertEquals(428, response.getStatusLine().getStatusCode());
  }

  @Test
  public void put_IF_MATCH_200() throws Exception {
    HashMap<String, String> headers = new HashMap<String, String>();   
    headers.put(HttpHeaders.IF_MATCH, eTagMatchString());
    
    HttpResponse response = execute(HttpMethodEnum.PUT, "/put", headers);
    assertNotNull(response);
    assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void put_IF_MATCH_412() throws Exception {
    HashMap<String, String> headers = new HashMap<String, String>();   
    headers.put(HttpHeaders.IF_MATCH, eTagNoMatchString());
    
    HttpResponse response = execute(HttpMethodEnum.PUT, "/put", headers);
    assertNotNull(response);
    assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatusLine().getStatusCode());
  }
  
  @Test
  public void get_IF_MATCH_200() throws Exception {
    HashMap<String, String> headers = new HashMap<String, String>();   
    headers.put(HttpHeaders.IF_MATCH, eTagMatchString());
    
    HttpResponse response = execute(HttpMethodEnum.GET, "/get", headers);
    assertNotNull(response);
    assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void get_IF_MATCH_412() throws Exception {
    HashMap<String, String> headers = new HashMap<String, String>();   
    headers.put(HttpHeaders.IF_MATCH, eTagNoMatchString());
    
    HttpResponse response = execute(HttpMethodEnum.GET, "/get", headers);
    assertNotNull(response);
    assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatusLine().getStatusCode());
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
