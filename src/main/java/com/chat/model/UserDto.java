package com.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {

	private String username;

	private int roomId;

	private String firstName;

	private String lastName;

	private String email;

	private String password;

	private String phone;

}
