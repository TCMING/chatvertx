package com.chat.service;

import com.chat.model.MessageDto;
import com.chat.model.UserDto;
import com.chat.model.MessageRetrive;
import com.chat.model.QueryControlData;
import com.chat.repository.MessageRepository;

import java.util.List;

public class MessageService {

	private MessageRepository messageRepository;


	public boolean sendMessage(String username, String id, String text){
		//TODO 幂等性是不是返回true
//		MessageDto messageDto = messageRepository.queryMessage(id);
		//幂等性
//		if(messageDto != null){
//			return false;
//		}

		if(messageRepository.queryMessage(id)){
			return false;
		}

		long curTime = System.currentTimeMillis();
		// TODO: 2021/7/8 查用户的房间
		UserDto userDto = null;
		if(userDto==null || userDto.getRoomId()==0){
			return false;
		}
		MessageDto messageDto = new MessageDto(id,text, username, userDto.getRoomId(),curTime);
		return messageRepository.saveMessage(messageDto);
	}

	public List<MessageRetrive> pullMessage(int roomId, QueryControlData controlData){
		List<MessageRetrive> messageRetriveList =  messageRepository.queryMessages(roomId,
				controlData.getPageIndex(),controlData.getPageSize());
		return messageRetriveList;
	}

}
