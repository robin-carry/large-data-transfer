package com.lockdown.lazy.client.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import com.lockdown.lazy.client.io.CustomStreamRequestEntity;
import com.lockdown.lazy.client.model.ResponseModel;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class is a simple spring boot mvc controller acting as a client which is sent a GIANT FILE (100MB-1GB)
 * Idea is that the stream is sent to another service running in a sidecar pattern
 * THIS PROJECT IS SERVICE "A"
 *
 * @author robin_carry
 */
@RestController
@RequestMapping("/upload")
@Log4j2
public class UploadController {

  private static final String tempFileDir = System.getProperty("java.io.tmpdir");

  private static final int DATA_SIZE = 65536;

  @PutMapping("/op")
  public ResponseEntity triggerOperation(@RequestParam("inputFile") MultipartFile inputFile,
    @RequestParam("operation") String op, @RequestParam("fileNameSuffix") String fileNameSuffix) {
    try {
      long startTime = System.nanoTime();
      File f = new File(tempFileDir,
        new StringBuffer(op).append("_").append(System.currentTimeMillis()).append("_").append(fileNameSuffix)
          .toString());
      // Not using custom one to have logging
      //      InputStreamRequestEntity requestEntity = new InputStreamRequestEntity((inputFile.getInputStream(), inputFile.getSize()));
      /* Associating the inputFile stream as Request Entity - inputFile is the GIANT FILE */
      CustomStreamRequestEntity requestEntity = new CustomStreamRequestEntity(inputFile.getInputStream(),
        inputFile.getSize());
      HttpClient client = new HttpClient();
      /* Sending it to another service running as sidecar pattern */
      log.info("Triggering the call to remote service...");
      PostMethod postMethod = new PostMethod("http://localhost:8181/stream/" + op);
      postMethod.setRequestEntity(requestEntity);
      client.executeMethod(postMethod);
      int count, totalBytes = 0;
      byte[] data = new byte[DATA_SIZE];
      log.info("Received response from client and begin processing...");
      /* After the data is processed by the locally running (sidecar pattern) SERVICE B, the data is written to another file */
      InputStream is = postMethod.getResponseBodyAsStream();
      try (OutputStream os = new FileOutputStream(f)) {
        count = is.read(data);
        while (count > 0) {
          totalBytes += count;
          os.write(data, 0, count);
          count = is.read(data);
        }
        ResponseModel responseModel = new ResponseModel();
        responseModel.setMsg(MessageFormat
          .format("Completed transferring of {0} bytes of data to file {1}", totalBytes, f.getAbsolutePath()));
        responseModel.setStatus(HttpStatus.OK);
        log.info("Completed Operation in {}ms", (System.nanoTime() - startTime) / 1000000);
        return ResponseEntity.ok(responseModel);
      }
    } catch (IOException e) {
      ResponseModel responseModel = new ResponseModel();
      responseModel.setMsg("Internal Server Error");
      responseModel.setError(MessageFormat.format("Failed to transmit data due to exception {0}", e.getMessage()));
      responseModel.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
      return ResponseEntity.ok(responseModel);
    }
  }

}
