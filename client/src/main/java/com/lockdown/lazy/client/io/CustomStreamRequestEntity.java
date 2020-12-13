package com.lockdown.lazy.client.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.httpclient.methods.RequestEntity;

@Log4j2
public class CustomStreamRequestEntity implements RequestEntity {
  private final InputStream is;
  private final long contentLength;

  public CustomStreamRequestEntity(InputStream is, long contentLength) {
    this.is = is;
    this.contentLength = contentLength;
  }
  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public void writeRequest(OutputStream outputStream) throws IOException {
    try(is) {
      byte[] data = new byte[4096];
      int count = is.read(data);
      int totalBytes = count;
      while(count > 0) {
        log.info("Written {} bytes of data to post request", totalBytes);
        outputStream.write(data, 0, count);
        count = is.read(data);
        totalBytes += count;
      }
      log.info("**********CLIENT COMPLETED WRITING******************");
    }
  }

  @Override
  public long getContentLength() {
    return contentLength;
  }

  @Override
  public String getContentType() {
    return "application/octet-stream";
  }
}
