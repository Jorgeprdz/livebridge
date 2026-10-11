package com.appsfolder.livebridge.liveupdate

import org.junit.Assert.*
import org.junit.Test

class ConversationGroupingPolicyTest {
    @Test fun sameConversationGroupsDifferentContactsDoNot() {
        val juanita1 = ConversationGroupingPolicy.conversationKey("com.whatsapp", true, "jid-a", null, "Juanita")
        val juanita2 = ConversationGroupingPolicy.conversationKey("com.whatsapp", true, "jid-a", null, "Juanita")
        val pedro = ConversationGroupingPolicy.conversationKey("com.whatsapp", true, "jid-b", null, "Pedro")
        assertEquals(juanita1, juanita2)
        assertNotEquals(juanita1, pedro)
    }

    @Test fun sameNameDifferentNotificationKeysDoNotCollide() {
        val one = ConversationGroupingPolicy.conversationKey("com.whatsapp", true, null, null, "Juanita", "one")
        val two = ConversationGroupingPolicy.conversationKey("com.whatsapp", true, null, null, "Juanita", "two")
        assertNotEquals(one, two)
        assertEquals(one, ConversationGroupingPolicy.conversationKey("com.whatsapp", true, null, null, "Juanita", "one"))
        assertNull(ConversationGroupingPolicy.conversationKey("com.whatsapp", true, null, null, "Juanita"))
    }

    @Test fun differentAppsNeverGroup() {
        assertNotEquals(
            ConversationGroupingPolicy.conversationKey("com.whatsapp", true, "jid-a", null, "Juanita"),
            ConversationGroupingPolicy.conversationKey("org.telegram.messenger", true, "jid-a", null, "Juanita")
        )
    }

    @Test fun fiveUpdatesCountWithoutDuplicateReplays() {
        val messages = listOf("Hola", "jajaja", "Buenos días", "¿Cómo estás?", "Bien")
        var count: Int? = null
        var oldText: String? = null
        messages.forEachIndexed { i, value ->
            count = ConversationGroupingPolicy.nextMessageCount(count, oldText, value, 1)
            assertEquals(i + 1, count)
            oldText = value
        }
        assertEquals(5, ConversationGroupingPolicy.nextMessageCount(count, oldText, "Bien", 1))
    }

    @Test fun noGroupForNonMessagesOrGenericTitle() {
        assertNull(ConversationGroupingPolicy.conversationKey("com.whatsapp", false, null, null, "Juanita", "one"))
        assertNull(ConversationGroupingPolicy.conversationKey("com.whatsapp", true, null, null, "3 new messages", "one"))
        assertNull(ConversationGroupingPolicy.conversationKey("com.whatsapp", true, null, null, null, "one"))
    }

    @Test fun textShowsCountAndLatest() {
        assertEquals("¿Cómo estás?", ConversationGroupingPolicy.summary(1, "¿Cómo estás?"))
        assertEquals("4 mensajes · ¿Cómo estás?", ConversationGroupingPolicy.summary(4, "¿Cómo estás?"))
    }
}
