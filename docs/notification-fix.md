# Notification tap and conservative conversation mirrors

Based on appsfolder/livebridge v2.2.1 (GPL-3.0-or-later). The fork's UI Hotfix parent commit is preserved.

- Tapping a mirror launches MirrorTapActivity (not a broadcast trampoline). It forwards an activity PendingIntent, or falls back to the original app launcher. Only then is LiveBridge's mirror dismissed.
- Per-app BYPASS message notifications share a stable mirror keyed by package + conversation shortcut ID. Without a shortcut, a source notification key is used, never a contact display name alone.
- The latest message and estimated update count are shown. The count is not an authoritative unread counter; overlapping histories or edited messages can affect it.
- Original messages are not read or deleted. Live Updates outside eligible BYPASS chat notifications are unchanged.
- Test on a Samsung Android 16 device before declaring tap, Now Bar, app-specific deep link, or dismissal validation complete.

Generated review: source anchors checked against fork main and upstream v2.2.1. Android/Flutter CI is required; Kotlin policy smoke checks alone are not sufficient for release.
