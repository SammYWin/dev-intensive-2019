package ru.skillbranch.devintensive.models.data

import androidx.annotation.VisibleForTesting
import ru.skillbranch.devintensive.App
import ru.skillbranch.devintensive.R
import ru.skillbranch.devintensive.extensions.shortFormat
import ru.skillbranch.devintensive.models.BaseMessage
import ru.skillbranch.devintensive.models.TextMessage
import ru.skillbranch.devintensive.repositories.ChatRepository
import ru.skillbranch.devintensive.utils.Utils
import ru.skillbranch.devintensive.viewmodels.MainViewModel
import java.util.*

data class Chat(
    val id: String,
    val title: String,
    val members: List<User> = listOf(),
    var messages: MutableList<BaseMessage> = mutableListOf(),
    var isArchived: Boolean = false
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun unreadableMessageCount(): Int = messages.filter{ message -> !message.isReaded }.count()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun lastMessageDate(): Date? = if(messages.isEmpty()) null else messages.last().date

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun lastMessageShort(): Pair<String, String?>{
        val lastMessage = messages.lastOrNull()
        var first =  App.applicationContext().resources.getString(R.string.chat_no_messages)
        val second = "@" + lastMessage?.from?.firstName

        if (lastMessage != null) {
            first = if(lastMessage is TextMessage) lastMessage.text!!
                    else "${lastMessage.from.firstName} - ${App.applicationContext().resources.getString(R.string.send_photo)}"
        }

        return first to second
    }

    private fun isSingle(): Boolean = members.size == 1


    fun toChatItem(): ChatItem {
        return if (isSingle() && !isArchived) {
            val user = members.first()
            ChatItem(
                id,
                user.avatar,
                Utils.toInitials(user.firstName, user.lastName) ?: "??",
                "${user.firstName ?: ""} ${user.lastName ?: ""}",
                lastMessageShort().first,
                unreadableMessageCount(),
                lastMessageDate()?.shortFormat(),
                user.isOnline
            )
        } else if(!isArchived){
            ChatItem(
                id,
                null,
                "",
                title,
                lastMessageShort().first,
                unreadableMessageCount(),
                lastMessageDate()?.shortFormat(),
                false,
                ChatType.GROUP,
                lastMessageShort().second
            )
        } else{
            val archivedChats = ChatRepository.loadChats().value!!
                .filter { isArchived }
                .sortedBy { lastMessageDate()}
            ChatItem(
                "-1",
                null,
                "",
                App.applicationContext().resources.getString(R.string.item_archive_title),
                archivedChats.last().lastMessageShort().first,
                archivedChats.sumBy { unreadableMessageCount() },
                archivedChats.last().lastMessageDate()?.shortFormat(),
                chatType = ChatType.ARCHIVE,
                author = archivedChats.last().lastMessageShort().second
            )
        }
    }

    fun toArchiveChatItem() : ChatItem{
        return ChatItem(
            id,
            null,
            "",
            "",
            lastMessageShort().first,
            unreadableMessageCount(),
            lastMessageDate()?.shortFormat(),
            chatType = ChatType.ARCHIVE
        )
    }
}

enum class ChatType{
    SINGLE,
    GROUP,
    ARCHIVE
}



