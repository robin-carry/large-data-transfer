package com.lockdown.lazy.server.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 */
@RestController
@RequestMapping("/stream")
@Log4j2
public class OpServiceController {

  private static final int DATA_SIZE = 4096;

  private static final int IV_LENGTH = 16;

  private static byte[] random = new byte[IV_LENGTH];

  @PostMapping("/op1")
  public void performOperationOne(HttpServletRequest request, HttpServletResponse response) throws Exception {
    SecureRandom s = SecureRandom.getInstanceStrong();
    s.nextBytes(random);
    log.info("Generated Random Value {}", Base64.getEncoder().encode(random));
    byte[] data = new byte[DATA_SIZE];
    OutputStream os = response.getOutputStream();
    InputStream is = request.getInputStream();
    os.write(random, 0, 16);
    int count, totalBytes = 0;
    totalBytes = count = is.read(data);
    while (count > 0) {
      ByteBuffer buffer = ByteBuffer.allocate(6);
      buffer.putInt(count);
      os.write(buffer.array());
      os.write(data, 0, count);
      log.info("Written {} byte as modified response", totalBytes);
      count = is.read(data);
      totalBytes += count;
    }
  }

  @PostMapping("/op2")
  public void performOperationTwo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int count = 0;
    OutputStream os = response.getOutputStream();
    InputStream is = request.getInputStream();
    is.read(random, 0, IV_LENGTH);
    log.info("Ignore the initial random 16 bytes {}", random);
    int chunkToBeRead = getChunkToBeRead(is);
    log.info("First chunk size is {}", chunkToBeRead);
    byte[] data = new byte[chunkToBeRead];
    count = is.read(data);
    int totalBytes = 0;
    while (count > 0) {
      int intermCount = count;
      while (intermCount < chunkToBeRead) {
        intermCount += is.read(data, intermCount, chunkToBeRead - count);
      }
      totalBytes += chunkToBeRead;
      log.info("Sending back {} bytes of cumulative data", totalBytes);
      os.write(data, 0, chunkToBeRead);
      chunkToBeRead = getChunkToBeRead(is);
      data = new byte[chunkToBeRead];
      count = is.read(data);
    }
  }

  private int getChunkToBeRead(InputStream is) throws Exception {
    byte[] b = new byte[6];
    int count = is.read(b, 0, 6);
    return ByteBuffer.wrap(b).getInt();
  }
}
