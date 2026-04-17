package com.assclk9000.app.util

import android.content.Context
import android.net.Uri
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ClickProfile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * JSON import/export utility for [ClickProfile] and associated [ClickAction] lists.
 *
 * Uses Gson for serialization. Exported JSON wraps the profile and its actions
 * together in a single [ExportData] envelope so they can be round-tripped as a unit.
 */
object ProfileExporter {

    /**
     * Envelope data class for JSON serialization.
     * Keeps the profile and its actions together in one JSON object.
     */
    private data class ExportData(
        @SerializedName("profile")
        val profile: ClickProfile,
        @SerializedName("actions")
        val actions: List<ClickAction>
    )

    private val gson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    /**
     * Serializes a profile and its actions to a JSON string.
     *
     * @param profile The click profile to export.
     * @param actions The list of actions belonging to the profile.
     * @return A pretty-printed JSON string containing both the profile and actions.
     */
    fun exportProfile(profile: ClickProfile, actions: List<ClickAction>): String {
        val exportData = ExportData(profile = profile, actions = actions)
        return gson.toJson(exportData)
    }

    /**
     * Deserializes a JSON string back into a profile and its actions.
     *
     * @param json The JSON string previously produced by [exportProfile].
     * @return A [Pair] of the deserialized [ClickProfile] and its [List] of [ClickAction]s.
     * @throws com.google.gson.JsonSyntaxException if the JSON is malformed.
     */
    fun importProfile(json: String): Pair<ClickProfile, List<ClickAction>> {
        val exportData = gson.fromJson(json, ExportData::class.java)
        return exportData.profile to exportData.actions
    }

    /**
     * Exports a profile and its actions to a file via a content URI.
     *
     * @param context Android context for content resolver access.
     * @param uri The destination URI (e.g., from SAF file picker).
     * @param profile The click profile to export.
     * @param actions The list of actions belonging to the profile.
     * @throws java.io.IOException if writing to the URI fails.
     */
    fun exportToFile(
        context: Context,
        uri: Uri,
        profile: ClickProfile,
        actions: List<ClickAction>
    ) {
        val json = exportProfile(profile, actions)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writer.write(json)
            }
        } ?: throw IllegalStateException("Unable to open output stream for URI: $uri")
    }

    /**
     * Imports a profile and its actions from a file via a content URI.
     *
     * @param context Android context for content resolver access.
     * @param uri The source URI (e.g., from SAF file picker).
     * @return A [Pair] of the deserialized [ClickProfile] and its [List] of [ClickAction]s.
     * @throws java.io.IOException if reading from the URI fails.
     * @throws com.google.gson.JsonSyntaxException if the file contains malformed JSON.
     */
    fun importFromFile(context: Context, uri: Uri): Pair<ClickProfile, List<ClickAction>> {
        val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalStateException("Unable to open input stream for URI: $uri")
        return importProfile(json)
    }
}
