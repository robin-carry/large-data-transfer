package com.lockdown.lazy.client.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ResponseModel {

  @Setter @Getter
  private String msg;
  @Setter @Getter
  private String error;
  @Setter @Getter
  private HttpStatus status;
}
