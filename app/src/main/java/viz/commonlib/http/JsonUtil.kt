package viz.commonlib.http

import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonUtil {
    private val mapJson = mutableMapOf<String, JsonObject>()
    private var isAddJson = false

    fun addJson(urlTag: String, key: String, value: Any): JsonUtil {
        if (mapJson[urlTag] == null) {
            mapJson[urlTag] = JsonObject()
        }
        val jsonObject = mapJson[urlTag]!!
        when (value) {
            is String -> {
                jsonObject.addProperty(key, value)
            }
            is Number -> {
                jsonObject.addProperty(key, value)
            }
            is Char -> {
                jsonObject.addProperty(key, value)
            }
            is Boolean -> {
                jsonObject.addProperty(key, value)
            }
        }
        isAddJson = true
        return this
    }

    fun parseJson(jsonStr: String): ResponseBody {
        return ResponseBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonStr
        )
    }

    fun parseAndAddJson(urlTag: String, jsonStr: String): JsonUtil {
        try {
            val jsonObjectNew = JSONObject(jsonStr)
            if (mapJson[urlTag] == null) {
                mapJson[urlTag] = JsonObject()
            }
            val jsonObject = mapJson[urlTag]!!
            jsonObjectNew.keys().forEach { key ->
                when (val value = jsonObjectNew.get(key)) {
                    is String -> {
                        jsonObject.addProperty(key, value)
                    }
                    is Number -> {
                        jsonObject.addProperty(key, value)
                    }
                    is Char -> {
                        jsonObject.addProperty(key, value)
                    }
                    is Boolean -> {
                        jsonObject.addProperty(key, value)
                    }
                }
            }
            isAddJson = true
        } catch (jsonEx: JSONException) {
            jsonEx.message?.apply {
                if (contains("type org.json.JSONArray cannot be converted to JSONObject")) {
                    if (mapJson[urlTag] == null) {
                        mapJson[urlTag] = JsonObject()
                    }
                    val jsonObject = mapJson[urlTag]!!
                    jsonObject.addProperty("数组", jsonStr)
                    isAddJson = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    fun has(urlTag: String): Boolean {
        return mapJson.containsKey(urlTag)
    }

    fun remove(urlTag: String) {
        mapJson.remove(urlTag)
    }

    fun toString(urlTag: String): String {
        val jsonObject = mapJson[urlTag] ?: JsonObject()
        remove(urlTag)
        return jsonObject.toString()
    }

    fun getBody(urlTag: String): RequestBody {
        val jsonObject = mapJson[urlTag] ?: JsonObject()
        remove(urlTag)
        return RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )
    }

    fun getBodyByJSON(jsonStr: String): RequestBody {
        return RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonStr
        )
    }
}