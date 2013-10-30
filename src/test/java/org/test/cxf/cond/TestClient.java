package org.test.cxf.cond;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
  public void printDates() throws Exception {
    Date d = new Date(System.currentTimeMillis());
    System.out.println("TestClient:Current Client Date:          " + StringHelper.dateToGmdString(d));

    URI uri = URI.create(server.getEndpoint() + "/serverDate");
    HttpGet get = new HttpGet(uri);
    HttpResponse response = client.execute(get);
    System.out.println("TestClient:Current Server Date:          " + StringHelper.httpEntityToString(response.getEntity()));
    System.out.println("TestClient:Last Modified  Date:          " + response.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue());
  }  
  
  @Test
  public void notModifiedWithLastModified() throws Exception {
    URI uri = URI.create(server.getEndpoint() + "/lmd");

    HttpGet get1 = new HttpGet(uri);
    HttpResponse response1 = client.execute(get1);
    String lastModified = response1.getFirstHeader(HttpHeaders.LAST_MODIFIED).getValue();
    get1.reset();

    HttpGet get2 = new HttpGet(uri);
    get2.setHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
    HttpResponse response2 = client.execute(get2);

    assertEquals(304, response2.getStatusLine().getStatusCode());
    get2.reset();
  }

  @Test
  public void notModifiedWithETag() throws Exception {
    URI uri = URI.create(server.getEndpoint() + "/etag");

    HttpGet get1 = new HttpGet(uri);
    HttpResponse response1 = client.execute(get1);
    String etag1 = response1.getFirstHeader(HttpHeaders.ETAG).getValue();
    get1.reset();

    HttpGet get2 = new HttpGet(uri);
    get2.setHeader(HttpHeaders.IF_NONE_MATCH, etag1);
    HttpResponse response2 = client.execute(get2);

    assertEquals(304, response2.getStatusLine().getStatusCode());
    get2.reset();
  }

  @Test
  public void modifiedWithETag() throws Exception {
    URI uri = URI.create(server.getEndpoint() + "/etag");

    HttpGet get1 = new HttpGet(uri);
    HttpResponse response1 = client.execute(get1);

    String etag1 = response1.getFirstHeader(HttpHeaders.ETAG).getValue();
    get1.reset();

    HttpPost post = new HttpPost(uri);
    client.execute(post);
    post.reset();

    HttpGet get2 = new HttpGet(uri);
    get2.setHeader(HttpHeaders.IF_NONE_MATCH, etag1);
    HttpResponse response2 = client.execute(get2);
    get2.reset();

    assertEquals(200, response2.getStatusLine().getStatusCode());
  }
}
