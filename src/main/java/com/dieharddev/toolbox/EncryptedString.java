package com.dieharddev.toolbox;

import lombok.Data;

@Data
public class EncryptedString {
	private String value;

	public EncryptedString(String value) {
		super();
		this.value = value;
	}
}
