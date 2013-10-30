package org.test.cxf.cond.rest;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class RestResource {

  private static final DataResource data = new DataResource();

  @Context
  HttpHeaders httpHeaders;

  @GET
  @Path("serverDate")
  @Produces("text/html")
  public Response getServerDate() {
    Date date = data.getCurrentServerDate();
    String dateString = data.getCurrentServerDateToGmdString();
    return Response.ok(dateString).lastModified(date).build();
  }

  @GET
  @Path("etag")
  public Response getETag(@Context Request request) {
    ResponseBuilder rb = request.evaluatePreconditions(data.getETag());

    Response response;
    CacheControl cc = new CacheControl();
    cc.setMaxAge(30);

    if (rb != null) {
      response = rb.cacheControl(cc).build();
    } else {
      response = Response.ok(data.toString(), "text/html")
          .tag(data.getETag())
          .cacheControl(cc)
          .lastModified(data.getLastModifiedDate())
          .build();
    }
    return response;
  }

  @POST
  @Path("etag")
  public Response postETag(@Context Request request) {
    data.modify();
    Response response;

    CacheControl cc = new CacheControl();
    cc.setMaxAge(30);

    response = Response.ok(data.toString(), "text/html")
        .tag(data.getETag())
        .cacheControl(cc)
        .lastModified(data.getLastModifiedDate())
        .build();
    return response;
  }

  @GET
  @Path("lmd")
  public Response getLastModified(@Context Request request) {
    System.out.println("RestResource:IF_MODIFIED_SINCE:          " + httpHeaders.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE));
    ResponseBuilder rb = request.evaluatePreconditions(data.getLastModifiedDate());

//    System.out.println("RestResource:getLastModified data:       " + data.getLastModifiedDate());
    System.out.println("RestResource:getLastModified data (GMD): " + data.getLastModifiedDateToGmdString());

    Response response;
    CacheControl cc = new CacheControl();
    cc.setMaxAge(30);

    if (rb != null) {
      response = rb.cacheControl(cc).build();
    } else {
      response = Response.ok(data.toString(), "text/html")
          .tag(data.getETag())
          .cacheControl(cc)
          .lastModified(data.getLastModifiedDate())
          .build();
    }
    System.out.println("RestResource:getLastModified response:   " + response.getHeaderString(HttpHeaders.LAST_MODIFIED));
    return response;
  }

  @POST
  @Path("lmd")
  public Response postLastModified(@Context Request request) {
    data.modify();
    Response response;

    CacheControl cc = new CacheControl();
    cc.setMaxAge(30);

    response = Response.ok(data.toString(), "text/html")
        .tag(data.getETag())
        .cacheControl(cc)
        .lastModified(data.getLastModifiedDate())
        .build();
    return response;
  }
}

