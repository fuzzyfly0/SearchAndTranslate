/*
 *
 *  * Copyright (C) 2025 AKS-Labs (original author)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fuzzyfly0.searchandtranslate.utils

import android.content.Context
import android.content.SharedPreferences

class UIPreferences(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_SEARCH_ENGINE_ORDER = "search_engine_order"
        private const val KEY_GOOGLE_ENABLED = "google_searches_enabled"
        private const val KEY_LASSO_TRANSLATE = "lasso_translate"
        private const val KEY_CIRCLE_ACTION = "circle_action"

        /** What happens after an area is circled. */
        const val CIRCLE_ACTION_ASK = "ask"
        const val CIRCLE_ACTION_TRANSLATE = "translate"
        const val CIRCLE_ACTION_SEARCH = "search"
    }

    /** The Google tab in Multi-Search; on by default. */
    fun isGoogleEnabled(): Boolean {
        return prefs.getBoolean(KEY_GOOGLE_ENABLED, true)
    }

    fun setGoogleEnabled(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_GOOGLE_ENABLED, isEnabled).apply()
    }

    fun getSearchEngineOrder(): String? {
        return prefs.getString(KEY_SEARCH_ENGINE_ORDER, null)
    }

    fun setSearchEngineOrder(order: String) {
        prefs.edit().putString(KEY_SEARCH_ENGINE_ORDER, order).apply()
    }

    /**
     * Action after circling an area: [CIRCLE_ACTION_ASK] (show a Search | Translate
     * chooser, default), [CIRCLE_ACTION_TRANSLATE] or [CIRCLE_ACTION_SEARCH].
     * Falls back to the older on/off "translate lasso selection" setting if it was
     * explicitly changed before this option existed.
     */
    fun getCircleAction(): String {
        prefs.getString(KEY_CIRCLE_ACTION, null)?.let { return it }
        if (prefs.contains(KEY_LASSO_TRANSLATE)) {
            return if (prefs.getBoolean(KEY_LASSO_TRANSLATE, true)) CIRCLE_ACTION_TRANSLATE else CIRCLE_ACTION_SEARCH
        }
        return CIRCLE_ACTION_ASK
    }

    fun setCircleAction(action: String) {
        prefs.edit().putString(KEY_CIRCLE_ACTION, action).apply()
    }
}
