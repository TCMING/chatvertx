package com.chat.repository;


import com.chat.dao.UserRedisDao;
import com.chat.model.UserDto;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author tianchengming
 * @Date 2021年7月3日 17:45
 * @Version 1.0
 */
public class UserRepository  {

    private UserRedisDao userRedisDao;

    /**
     * 内存维护所有用户信息
     */
    private ConcurrentHashMap<String, UserDto> usersCache;

    public UserDto queryUser(String userName){
        UserDto userDto = usersCache.get(userName);
        return userDto;
    }

    public boolean saveUser(UserDto userDto){
        try {
            userRedisDao.createUser(userDto);
            usersCache.put(userDto.getUsername(), userDto);
        } catch (Exception e) {
            // TODO: 2021/7/26 写内存失败再试一次，再失败就不管了，后面优化
            usersCache.put(userDto.getUsername(), userDto);
        }
        return true;
    }

    //用户名与房间关系放到缓存，暂时不用
    @Deprecated
    public boolean updateUser(int roomId , String username){
//        return userDao.updateUser(roomId , username) <= 1;
        return false;
    }

}
