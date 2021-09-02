package com.chat.repository;


import com.chat.dao.UserRedisDao;
import com.chat.model.UserDto;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author tianchengming
 * @Date 2021年7月3日 17:45
 * @Version 1.0
 */
public class UserRepository {

    private UserRedisDao userRedisDao ;

    public UserRepository(UserRedisDao userRedisDao){
        this.userRedisDao = userRedisDao;
    }

    /**
     * 内存维护所有用户信息
     */
    private ConcurrentHashMap<String, UserDto> usersCache;

    public UserDto queryUser(String userName){
        UserDto userDto = usersCache.get(userName);
        return userDto;
    }

    public boolean saveUser(UserDto userDto){
        userRedisDao.createUser(userDto);
        usersCache.put(userDto.getUsername(), userDto);
        return true;
    }

    //用户名与房间关系放到缓存，暂时不用
    @Deprecated
    public boolean updateUser(int roomId , String username){
//        return userDao.updateUser(roomId , username) <= 1;
        return false;
    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        usersCache = new ConcurrentHashMap<>(2048);
////        List<UserDto> roomDtos = userRedisDao.queryAll();
////        if(!CollectionUtils.isEmpty(roomDtos)){
////            for(UserDto userDto: roomDtos){
////                usersCache.put(userDto.getUsername(),userDto);
////            }
////        }
//    }

}
