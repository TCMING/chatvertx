package com.chat.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author tianchengming
 * @Date 2021年7月3日 15:23
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class UserResponse {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

}
