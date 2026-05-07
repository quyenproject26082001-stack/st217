package com.pony.avatar.ocmaker.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pony.avatar.ocmaker.core.helper.AssetHelper
import com.pony.avatar.ocmaker.core.helper.InternetHelper
import com.pony.avatar.ocmaker.core.helper.MediaHelper
import com.pony.avatar.ocmaker.core.service.RetrofitClient
import com.pony.avatar.ocmaker.core.service.RetrofitPreventive
import com.pony.avatar.ocmaker.core.utils.DataLocal.isFailBaseURL
import com.pony.avatar.ocmaker.core.utils.key.AssetsKey
import com.pony.avatar.ocmaker.core.utils.key.DomainKey
import com.pony.avatar.ocmaker.core.utils.key.ValueKey
import com.pony.avatar.ocmaker.core.utils.state.HandleState
import com.pony.avatar.ocmaker.data.model.DataAPI
import com.pony.avatar.ocmaker.data.model.PartAPI
import com.pony.avatar.ocmaker.data.model.custom.ColorModel
import com.pony.avatar.ocmaker.data.model.custom.CustomizeModel
import com.pony.avatar.ocmaker.data.model.custom.LayerListModel
import com.pony.avatar.ocmaker.data.model.custom.LayerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.collections.forEachIndexed

class DataViewModel() : ViewModel() {
    private val _allData = MutableStateFlow<ArrayList<CustomizeModel>>(arrayListOf())
    val allData: StateFlow<ArrayList<CustomizeModel>> = _allData.asStateFlow()
    private val _getDataAPI = MutableLiveData<List<PartAPI>>()
    val getDataAPI: LiveData<List<PartAPI>> get() = _getDataAPI

    fun saveAndReadData(context: Context) {
        viewModelScope.launch {
            val timeStart = System.currentTimeMillis()
            val list = withContext(Dispatchers.IO) {
                // Lần đầu vào app -> Load data Asset -> Lưu file internal
                if (!MediaHelper.checkFileInternal(context, ValueKey.DATA_FILE_INTERNAL)) {
                    AssetHelper.getDataFromAsset(context)
                }

                val totalData = MediaHelper.readListFromFile<CustomizeModel>(context, ValueKey.DATA_FILE_INTERNAL)
                    .toCollection(ArrayList())
                var dataApi = MediaHelper.readListFromFile<CustomizeModel>(context, ValueKey.DATA_FILE_API_INTERNAL)
                    ?: arrayListOf()
                if (InternetHelper.checkInternet(context)) {
                    getAllParts(context).collect { state ->
                        when (state) {
                            HandleState.LOADING -> {}
                            HandleState.SUCCESS -> {
                                dataApi = MediaHelper.readListFromFile<CustomizeModel>(context, ValueKey.DATA_FILE_API_INTERNAL)
                            }

                            else -> {}
                        }
                    }
                }
                totalData.addAll(dataApi)
                // Sort all data by level (ascending order)
                totalData.sortBy { it.level }
                totalData
            }
            _allData.value = list
            val timeEnd = System.currentTimeMillis()
            Log.d("nbhieu", "time load data: ${timeEnd - timeStart}")
        }
    }

    fun ensureData(context: Context) {
        if (_allData.value.isEmpty()) {
            saveAndReadData(context)
        }
    }

    fun getAllParts(context: Context): Flow<HandleState> = flow {
        emit(HandleState.LOADING)
        Log.d("PATTERN_P", "========================================")
        Log.d("PATTERN_P", "🚀 Firing both URLs concurrently...")
        Log.d("PATTERN_P", "   PRIMARY   : ${DomainKey.BASE_URL}")
        Log.d("PATTERN_P", "   PREVENTIVE: ${DomainKey.BASE_URL_PREVENTIVE}")
        data class ApiResult(val response: retrofit2.Response<Map<String, List<PartAPI>>>?, val isPreventive: Boolean)
        val result = coroutineScope {
            val primaryDeferred = async { withTimeoutOrNull(5_000) { try { RetrofitClient.api.getAllData() } catch (e: Exception) { Log.e("PATTERN_P", "❌ PRIMARY failed: ${e.javaClass.simpleName} - ${e.message}"); null } } }
            val preventiveDeferred = async { withTimeoutOrNull(5_000) { try { RetrofitPreventive.api.getAllData() } catch (e: Exception) { Log.e("PATTERN_P", "❌ PREVENTIVE failed: ${e.javaClass.simpleName} - ${e.message}"); null } } }
            val primary = primaryDeferred.await()
            if (primary != null && primary.isSuccessful) {
                preventiveDeferred.cancel()
                Log.d("PATTERN_P", "✅ PRIMARY won — PREVENTIVE cancelled")
                ApiResult(primary, false)
            } else {
                Log.w("PATTERN_P", "⚠️ PRIMARY failed or null, waiting for PREVENTIVE...")
                val preventive = preventiveDeferred.await()
                if (preventive != null && preventive.isSuccessful) {
                    Log.d("PATTERN_P", "✅ PREVENTIVE won")
                } else {
                    Log.e("PATTERN_P", "❌ Both URLs failed")
                }
                ApiResult(preventive, true)
            }
        }
        isFailBaseURL = result.isPreventive
        val response = result.response

        if (response != null && response.isSuccessful && response.body() != null) {
            val dataMap = ArrayList<DataAPI>()
            response.body()?.forEach { (key, dataBody) ->
                dataMap.add(DataAPI(key, dataBody))
            }
            withContext(Dispatchers.IO) {
                getDataAPI(context, dataMap)
            }
            emit(HandleState.SUCCESS)
        } else {
            val file = File(context.filesDir, ValueKey.DATA_FILE_API_INTERNAL)
            if (file.exists()) file.delete()
            emit(HandleState.FAIL)
        }
    }

    fun getDataAPI(context: Context, dataList:
    ArrayList<DataAPI>) {
        val file = context.getFileStreamPath(ValueKey.DATA_FILE_API_INTERNAL)
        val gson = Gson()


        context.openFileOutput(ValueKey.DATA_FILE_API_INTERNAL,
            Context.MODE_PRIVATE)
            .bufferedWriter().use { writer ->
                writer.write("[")
                dataList.forEachIndexed { indexCharacter,
                                          data ->
                    val baseDomain = if (!isFailBaseURL)
                        DomainKey.BASE_URL else DomainKey.BASE_URL_PREVENTIVE
                    val avatarCharacter =
                        "$baseDomain${DomainKey.SUB_DOMAIN}/${data.name}/${DomainKey.AVATAR_CHARACTER_API}"
                    val layerList =
                        ArrayList<LayerListModel>(data.parts.size)
                    val sortedParts = data.parts.sortedBy {
                        it.level }

                    sortedParts.forEachIndexed { indexLayer,
                                                 dataLayer ->
                        val layerName = if
                                                (dataLayer.parts.contains("-")) {
                            dataLayer.parts.split("-")
                        } else {
                            dataLayer.parts.split("_")
                        }
                        val positionCustom =
                            layerName.first().toInt() - 1
                        val positionNavigation =
                            layerName.last().toInt() - 1
                        val imageNavigation =
                            "${baseDomain}${DomainKey.SUB_DOMAIN}/${data.name}/${dataLayer.parts}/${DomainKey.IMAGE_NAVIGATION}"
                        val layer = getDataLayer(baseDomain,
                            dataLayer, dataLayer.parts)

                        layerList.add(LayerListModel(positionCustom,
                            positionNavigation, imageNavigation, layer))
                    }
                    layerList.sortBy { it.positionNavigation
                    }

                    val characterLevel =
                        sortedParts.minOfOrNull { it.level } ?: 100
                    val dataApi = CustomizeModel(
                        dataName = data.name,
                        avatar = avatarCharacter,
                        layerList = layerList,
                        level = characterLevel,
                        isFromAPI = true
                    )

                    // Stream từng item vào file, không giữ trong memory
                            if (indexCharacter > 0)
                                writer.write(",")
                    gson.toJson(dataApi, writer)

                    Log.d("nbhieu", "avatar:${dataApi.avatar}")
                }
                writer.write("]")
            }
    }

    private fun getDataLayer(baseDomain: String, partData: PartAPI, layer: String): ArrayList<LayerModel> {
        return if (partData.colorArray != "" || partData.colorArray.isNotEmpty()) {
            getDataAPIColor(baseDomain, partData, layer)
        } else {
            getDataAPINoColor(baseDomain, partData, layer)
        }
    }

    private fun getDataAPINoColor(baseDomain: String, part: PartAPI, layer: String): ArrayList<LayerModel> {
        val layerPath = ArrayList<LayerModel>(part.quantity)
        val prefix = "$baseDomain${DomainKey.SUB_DOMAIN}/${part.position}/${layer}/"
        val suffix = DomainKey.LAYER_EXTENSION
        for (i in 1..part.quantity) {
            layerPath.add(
                LayerModel(
                    "$prefix${i}$suffix",
                    false,
                    arrayListOf()
                )
            )
        }
        return layerPath
    }

    private fun getDataAPIColor(baseDomain: String, part: PartAPI, layer: String): ArrayList<LayerModel> {
        val layerPath = ArrayList<LayerModel>(part.quantity)
        val getColorCode = part.colorArray.split(",")
        val prefix = "$baseDomain${DomainKey.SUB_DOMAIN}/${part.position}/${layer}/"
        val suffix = DomainKey.LAYER_EXTENSION

        for (i in 1..part.quantity) {
            val listColor = ArrayList<ColorModel>(getColorCode.size)
            for (j in 0 until getColorCode.size) {
                listColor.add(
                    ColorModel(
                        "#${getColorCode[j]}",
                        "$prefix${getColorCode[j]}/${i}$suffix"
                    )
                )
            }
            layerPath.add(LayerModel(listColor.first().path, true, listColor))
        }
        return layerPath
    }
}