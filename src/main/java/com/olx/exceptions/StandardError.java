package com.olx.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class StandardError {

	private Integer status;
	private LocalDateTime timestamp;
	private String error;
	private String path;


}
