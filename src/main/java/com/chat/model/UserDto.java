package com.chat.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {

	private String username;

	private int roomId;

	private String firstName;

	private String lastName;

	private String email;

	private String password;

	private String phone;

}
