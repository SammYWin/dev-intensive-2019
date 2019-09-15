package ru.skillbranch.devintensive.viewmodels

import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ru.skillbranch.devintensive.extensions.mutableLiveData
import ru.skillbranch.devintensive.models.data.Chat
import ru.skillbranch.devintensive.models.data.ChatItem
import ru.skillbranch.devintensive.models.data.ChatType
import ru.skillbranch.devintensive.repositories.ChatRepository
import ru.skillbranch.devintensive.utils.DataGenerator

class MainViewModel : ViewModel() {
    private val query = mutableLiveData("")
    private val chatRepository = ChatRepository
    private val archivedChats = mutableListOf<ChatItem>()
    private val chats = Transformations.map(chatRepository.loadChats()){ chats->
            val archivedChats = chats
                .filter { it.isArchived }
                .map { it.toChatItem() }
                .sortedBy { it.lastMessageDate }
            if(archivedChats.isEmpty()){
                return@map chats
                    .map { it.toChatItem() }
                    .sortedBy { it.id.toInt() }
            } else{
                val chatsWithArchiveItem = mutableListOf<ChatItem>()
                chatsWithArchiveItem.add(0, archivedChats.last())
                chatsWithArchiveItem.addAll((chats.filter { !it.isArchived }.map { it.toChatItem() }))
                return@map chatsWithArchiveItem
            }
    }

    fun getChatData() : LiveData<List<ChatItem>>{
        val result = MediatorLiveData<List<ChatItem>>()

        val FilterF = {
            val queryStr = query.value!!
            val resChats =  chats.value!!

            result.value = if(queryStr.isEmpty()) resChats
                            else resChats.filter { it.title.contains(queryStr, true) }

        }

        result.addSource(chats){FilterF.invoke()}
        result.addSource(query){FilterF.invoke()}

        return result
    }

//    private fun getLastArchivedChatItem() : ChatItem? {
//
//    }

    fun addToArchive(id: String) {
        val chat = chatRepository.find(id)
        chat ?: return
        chatRepository.update(chat.copy(isArchived = true))
    }

    fun restoreFromArchive(id: String) {
        val chat = chatRepository.find(id)
        chat ?: return
        chatRepository.update(chat.copy(isArchived = false))
    }

    fun handleSearchQuery(text: String?) {
        query.value = text
    }
}