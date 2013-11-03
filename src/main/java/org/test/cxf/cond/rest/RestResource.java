package org.test.cxf.cond.rest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.olingo.odata2.core.rest.PATCH;

@Path("/")
public class RestResource {

  @Context
  HttpHeaders httpHeaders;

  private static EntityTag eTag = null;

  public static void setETag(EntityTag eTag) {
    RestResource.eTag = eTag;
  }

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
  @Path("get")
  @Produces("text/html")
  public Response get(@Context Request request) {
    ResponseBuilder builder = evaluatePreconditions(request);
    Response response;

    if (builder == null) {
      response = Response.ok().entity("ok").lastModified(lastModifiedDate).tag(eTag).build();
    } else {
      response = builder.build();
    }

    return response;
  }

  @POST
  @Path("post")
  @Produces("text/html")
  public Response post(@Context Request request) {
    return Response.status(Status.CREATED).entity("created").lastModified(lastModifiedDate).tag(eTag).build();
  }

  @DELETE
  @Path("delete")
  @Produces("text/html")
  public Response delete(@Context Request request) {
    ResponseBuilder builder = evaluatePreconditions(request);
    Response response;

    if (builder == null) {
      response = Response.status(Status.NO_CONTENT).lastModified(lastModifiedDate).tag(eTag).build();
    } else {
      response = builder.build();
    }

    return response;
  }

  @PUT
  @Path("put")
  @Produces("text/html")
  public Response put(@Context Request request) {
    ResponseBuilder builder = evaluatePreconditions(request);
    Response response;

    if (builder == null) {
      response = Response.ok().entity("ok").lastModified(lastModifiedDate).tag(eTag).build();
    } else {
      response = builder.build();
    }

    return response;
  }

  @PATCH
  @Path("patch")
  @Produces("text/html")
  public Response patch(@Context Request request) {
    ResponseBuilder builder = evaluatePreconditions(request);
    Response response;

    if (builder == null) {
      response = Response.ok().entity("ok").lastModified(lastModifiedDate).tag(eTag).build();
    } else {
      response = builder.build();
    }

    return response;
  }

  ResponseBuilder evaluatePreconditions(Request request) {
    ResponseBuilder responseBuilder = null;

    responseBuilder = request.evaluatePreconditions(eTag);

    if (responseBuilder == null &&
        request.getMethod().equalsIgnoreCase("put") || request.getMethod().equalsIgnoreCase("patch")
        || request.getMethod().equalsIgnoreCase("delete")) {
      boolean cond = httpHeaders.getHeaderString(HttpHeaders.IF_MATCH) == null &&
          httpHeaders.getHeaderString(HttpHeaders.IF_MATCH) == null &&
          httpHeaders.getHeaderString(HttpHeaders.IF_MATCH) == null &&
          httpHeaders.getHeaderString(HttpHeaders.IF_MATCH) == null;
      if (cond) {
        responseBuilder = Response.status(428).entity("precondition required");
      }
    }

    return responseBuilder;
  }
}
