package dev.davidv.translator

/**
 * Languages last picked in the screen / area translate toolbar. The text screen
 * opened from the host overlay (EXTRA_OVERLAY_LANGUAGES) starts with them, so typing
 * translates the same way as "Translate all" or a circled area. In memory only.
 */
object OverlayLanguageMemory {
  /** Null while the toolbar is on auto-detect. */
  @Volatile var sourceCode: String? = null

  /** Null until a target is picked: the toolbar then uses the default target. */
  @Volatile var targetCode: String? = null

  fun remember(
    source: Language?,
    target: Language?,
    autoSource: Boolean,
  ) {
    sourceCode = if (autoSource) null else source?.code
    targetCode = target?.code
  }
}
