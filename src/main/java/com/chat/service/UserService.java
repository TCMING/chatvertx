package com.chat.service;

import com.chat.model.UserDto;
import com.chat.model.UserRequest;
import com.chat.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;


/**
 * @Author tianchengming
 * @Date 2021年7月3日 20:14
 * @Version 1.0
 */
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto queryUserByName(String userName){
        UserDto userDto = userRepository.queryUser(userName);
        return userDto;
    }

    public boolean userPasswordCheck(String username, String password){
        UserDto userDto = userRepository.queryUser(username);
        if(userDto != null && StringUtils.equals(password,userDto.getPassword())){
            return true;
        }
        return false;
    }

    public boolean registryUser(UserRequest user){
        UserDto userDto = userRepository.queryUser(user.getUsername());
        if(userDto != null){
            return false;
        }
        userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPassword(user.getPassword());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        return userRepository.saveUser(userDto);

    }

}
