//package com.chat.dao;
//
//import com.chat.model.MessageDto;
//
//import java.util.HashMap;
//import java.util.List;
//
//public class MessageRedisDao {
//
//
//    private String messageListKey = "messageList";
//
//    private String messageIdSet = "messageIdSet";
//
//    public void saveMessage(MessageDto messageDto) {
//        redisTemplate.opsForList().leftPush(messageDto.getRoomId()+messageListKey, messageDto);
//        // TODO: 2021/7/25 事务？
//        redisTemplate.opsForSet().add(messageIdSet,messageDto.getId());
//    }
//
//    public void saveMessage(List<MessageDto> list) {
//
//        List<String> result = redisTemplate.executePipelined(new RedisCallback() {
//            @Override
//            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                connection.openPipeline();
//                list.stream().forEach(new Consumer<MessageDto>() {
//                    @Override
//                    public void accept(MessageDto messageDto) {
//                        byte[] rawKey = redisTemplate.getKeySerializer().serialize(messageDto.getRoomId()+messageListKey);
//                        byte[] rawValue = redisTemplate.getValueSerializer().serialize(messageDto);
//                        connection.lPush(rawKey,rawValue);
//
//                        byte[] rawKeySet = redisTemplate.getKeySerializer().serialize(messageIdSet);
//                        byte[][] rawValuesSet = new byte[1][];
//                        rawValuesSet[0] = redisTemplate.getValueSerializer().serialize(messageDto.getId());
//                        connection.sAdd(rawKeySet, rawValuesSet);
//                    }
//                });
//                return null;
//            }
//        });
//    }
//
//    public boolean queryMessage(String id){
//        return redisTemplate.opsForSet().isMember(messageIdSet,id);
//    }
//
//    public List<MessageRetrive> queryAllMessage(HashMap<String,Integer> params){
//        String key = params.get("roomId")+messageListKey;
//        int startIndex = params.get("startIndex");
//        int endIndex = startIndex+params.get("pageSize")-1;
//        if (endIndex<0){
//            return new ArrayList<MessageRetrive>();
//        }
//        return redisTemplate.opsForList().range(key,params.get("startIndex"),endIndex);
//    }
//
//}
