package org.test.cxf.cond.rest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/")
public class RestResource {

  @Context
  HttpHeaders httpHeaders;

  public static final String ETAG = "W/\"123abc\"";
  private EntityTag eTag = new EntityTag(ETAG);
  public static final String LAST_MODIFIED_DATE = "Sat, 29 Oct 1994 10:00:00 GMT";
  private Date lastModifiedDate;

  {
    try {
      DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
      lastModifiedDate = df.parse(LAST_MODIFIED_DATE);
    } catch (ParseException e) {
      e.printStackTrace(System.err);
    }
  }

  @GET
  @Path("fetch")
  public Response fetch() {
    return Response.ok("ok", "text/html").lastModified(lastModifiedDate).tag(eTag).build();
  }

  @PUT
  @Path("put")
  public Response put(@Context Request request) {
    ResponseBuilder builder = request.evaluatePreconditions();
    Response response;

    if (builder == null) {
      response = Response.ok("ok", "text/html").lastModified(lastModifiedDate).tag(eTag).build();
    } else {
      response = builder.build();
    }

    return response;
  }

  //  @GET
  //  @Path("serverDate")
  //  @Produces("text/html")
  //  public Response getServerDate() {
  //    Date date = data.getCurrentServerDate();
  //    String dateString = data.getCurrentServerDateToGmdString();
  //    return Response.ok(dateString).lastModified(date).build();
  //  }
  //
  //  @GET
  //  @Path("etag")
  //  public Response getETag(@Context Request request) {
  //    ResponseBuilder rb = request.evaluatePreconditions(data.getETag());
  //
  //    Response response;
  //    CacheControl cc = new CacheControl();
  //    cc.setMaxAge(30);
  //
  //    if (rb != null) {
  //      response = rb.cacheControl(cc).build();
  //    } else {
  //      response = Response.ok(data.toString(), "text/html")
  //          .tag(data.getETag())
  //          .cacheControl(cc)
  //          .lastModified(data.getLastModifiedDate())
  //          .build();
  //    }
  //    return response;
  //  }
  //
  //  @POST
  //  @Path("etag")
  //  public Response postETag(@Context Request request) {
  //    data.modify();
  //    Response response;
  //
  //    CacheControl cc = new CacheControl();
  //    cc.setMaxAge(30);
  //
  //    response = Response.ok(data.toString(), "text/html")
  //        .tag(data.getETag())
  //        .cacheControl(cc)
  //        .lastModified(data.getLastModifiedDate())
  //        .build();
  //    return response;
  //  }
  //
  //  @GET
  //  @Path("lmd")
  //  public Response getLastModified(@Context Request request) {
  //    System.out.println("RestResource:IF_MODIFIED_SINCE:          " + httpHeaders.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE));
  //    ResponseBuilder rb = request.evaluatePreconditions(data.getLastModifiedDate());
  //
  ////    System.out.println("RestResource:getLastModified data:       " + data.getLastModifiedDate());
  //    System.out.println("RestResource:getLastModified data (GMD): " + data.getLastModifiedDateToGmdString());
  //
  //    Response response;
  //    CacheControl cc = new CacheControl();
  //    cc.setMaxAge(30);
  //
  //    if (rb != null) {
  //      response = rb.cacheControl(cc).build();
  //    } else {
  //      response = Response.ok(data.toString(), "text/html")
  //          .tag(data.getETag())
  //          .cacheControl(cc)
  //          .lastModified(data.getLastModifiedDate())
  //          .build();
  //    }
  //    System.out.println("RestResource:getLastModified response:   " + response.getHeaderString(HttpHeaders.LAST_MODIFIED));
  //    return response;
  //  }
  //
  //  @POST
  //  @Path("lmd")
  //  public Response postLastModified(@Context Request request) {
  //    data.modify();
  //    Response response;
  //
  //    CacheControl cc = new CacheControl();
  //    cc.setMaxAge(30);
  //
  //    response = Response.ok(data.toString(), "text/html")
  //        .tag(data.getETag())
  //        .cacheControl(cc)
  //        .lastModified(data.getLastModifiedDate())
  //        .build();
  //    return response;
  //  }
}
