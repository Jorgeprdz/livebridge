package com.appsfolder.livebridge.liveupdate

/** Never merge distinct packages or separate source identities based on contact name. */
internal object ConversationGroupingPolicy {
    fun conversationKey(
        packageName: String,
        isMessage: Boolean,
        shortcutId: String?,
        conversationTitle: String?,
        notificationTitle: String?,
        sourceNotificationKey: String? = null
    ): String? {
        if (!isMessage || packageName.isBlank()) return null
        val shortcut = shortcutId?.trim()?.takeIf(String::isNotEmpty)
        val title = conversationTitle?.trim()?.takeIf(String::isNotEmpty)
            ?: notificationTitle?.trim()?.takeIf(String::isNotEmpty)
        val fallbackKey = sourceNotificationKey?.trim()?.takeIf(String::isNotEmpty)
        // "Juanita" is a display label, not a unique conversation identifier.
        if (shortcut == null && (title == null || isGenericTitle(title))) return null
        val kind = if (shortcut != null) "shortcut" else "source"
        val identifier = shortcut ?: fallbackKey ?: return null
        return "conversation:${packageName.length}:$packageName:$kind:${identifier.length}:$identifier"
    }

    fun nextMessageCount(
        previousCount: Int?,
        previousText: String?,
        latestText: String,
        reportedCount: Int
    ): Int {
        val incoming = reportedCount.coerceAtLeast(1)
        val prior = previousCount ?: return incoming
        val fresh = latestText.isNotBlank() && latestText != previousText
        return maxOf(incoming, if (fresh) prior + 1 else prior)
    }

    fun summary(count: Int, latestMessage: String, language: String = "es"): String {
        if (count <= 1) return latestMessage
        val noun = when (language) {
            "es" -> "mensajes"
            "ru" -> "сообщений"
            "de" -> "Nachrichten"
            else -> "messages"
        }
        return "$count $noun · $latestMessage"
    }

    private fun isGenericTitle(title: String): Boolean {
        val normalized = title.trim().lowercase()
        return normalized in setOf("messages", "mensajes", "new messages", "nuevos mensajes",
            "whatsapp", "telegram", "signal", "notifications", "notificaciones") ||
            Regex("^\\d+\\s+(new\\s+)?(messages|mensajes|chats|conversations)$")
                .matches(normalized)
    }
}
