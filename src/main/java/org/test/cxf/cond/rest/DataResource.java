package org.test.cxf.cond.rest;

import java.util.Date;
import java.util.Random;

import javax.ws.rs.core.EntityTag;

import org.test.cxf.cond.util.StringHelper;

public class DataResource {

  private String data;
  private Date lastModifiedDate;

  public DataResource() {
    modify();
  }

  public synchronized void modify() {
    data = Long.toString(new Random(System.currentTimeMillis()).nextLong());
    lastModifiedDate = new Date(System.currentTimeMillis());
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }

  @Override
  public String toString() {
    return data;
  }

  public EntityTag getETag() {
    return new EntityTag(Integer.toHexString(data.hashCode()), true);
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public String getLastModifiedDateToGmdString() {
    return StringHelper.dateToGmdString(lastModifiedDate);
  }

  public String getCurrentServerDateToGmdString() {
    return StringHelper.dateToGmdString(new Date(System.currentTimeMillis()));
  }

  public Date getCurrentServerDate() {
    return new Date(System.currentTimeMillis());
  }
}
