package com.chat.repository;

import com.chat.model.QueryControlData;
import com.chat.model.RoomDto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author tianchengming
 * @Date 2021年7月3日 15:35
 * @Version 1.0
 */
public class RoomRepository {


    //暂时不用内存淘汰，维护全量的room信息
    private ConcurrentHashMap<Integer,String> roomsCache;

    /**
     * TODO 增加事务保证
     * @param name
     * @return
     */
    public String saveRoom(String name){
        RoomDto roomDto = new RoomDto();
        roomDto.setName(name);
//      TODO  int roomId = roomRedisDao.createRoomId();
        int roomId = 0;
        roomDto.setId(roomId);
//        roomRedisDao.createRoom(roomDto);
        try {
            roomsCache.put(roomId,name);
        } catch (Exception e) {
            roomsCache.put(roomId,name);
        }
        return String.valueOf(roomId);
    }

    public List<RoomDto> queryRoomRecord(QueryControlData controlData){
        int startIndex = controlData.getPageIndex()*controlData.getPageSize();
//        List<RoomVo> roomVos = new ArrayList<>();
//        List<RoomDto> roomDtos = roomRedisDao.queryRoomRecord(startIndex,controlData.getPageSize());
        List<RoomDto> roomDtos = new ArrayList<>();
//        if(!CollectionUtils.isEmpty(roomDtos)){
//            for(RoomDto roomDto: roomDtos){
//                roomVos.add(new RoomVo(String.valueOf(roomDto.getId()),roomDto.getName()));
//            }
//        }
//        return roomVos;
        return roomDtos;
    }


    public RoomDto queryRoomById(int roomId){
        String roomName = roomsCache.getOrDefault(roomId,null);
        if(StringUtils.isNotBlank(roomName)){
            return new RoomDto(roomId,roomName);
        }
        return null;
//        RoomDto roomDto = roomDao.queryRoomById(roomId);
//        if(roomDto != null){
//            roomsCache.put(roomDto.getId(),roomDto.getName());
//        }
//        return roomDto;
    }

    public RoomDto queryRoomByName(String name){
//        return roomDao.queryRoomByName(name);
        return new RoomDto();
    }

    public void afterPropertiesSet() throws Exception {
//        roomRedisDao.initRoomId();
        roomsCache = new ConcurrentHashMap<>(2048);
//        List<RoomDto> roomDtos = roomDao.queryAll();
        List<RoomDto> roomDtos = this.queryAll();
        if(roomDtos != null){
            for(RoomDto roomDto: roomDtos){
                roomsCache.put(roomDto.getId(),roomDto.getName());
            }
        }
    }

    public List<RoomDto> queryAll(){
//        List<RoomDto> roomDtos = roomDao.queryAll();
//        List<RoomDto> roomDtos = roomRedisDao.queryAll();
        return new ArrayList<>();
    }

}
