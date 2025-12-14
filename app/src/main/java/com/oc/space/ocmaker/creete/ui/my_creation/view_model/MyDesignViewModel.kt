package com.oc.space.ocmaker.creete.ui.my_creation.view_model

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oc.space.ocmaker.creete.core.helper.MediaHelper
import com.oc.space.ocmaker.creete.core.utils.key.ValueKey
import com.oc.space.ocmaker.creete.core.utils.state.HandleState
import com.oc.space.ocmaker.creete.data.model.MyAlbumModel
import com.oc.space.ocmaker.creete.data.model.custom.SuggestionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MyDesignViewModel : ViewModel() {
    private val _myDesignList = MutableStateFlow<ArrayList<MyAlbumModel>>(arrayListOf())
    val myDesignList = _myDesignList.asStateFlow()
    private val _isLastItem = MutableStateFlow<Boolean>(false)
    val isLastItem: StateFlow<Boolean> = _isLastItem

    var positionCharacter = -1

    fun loadMyDesign(context: Context) {
        val editList = MediaHelper.readListFromFile<SuggestionModel>(context, ValueKey.EDIT_FILE_INTERNAL).map { MyAlbumModel(it.pathInternalEdit) }
        _myDesignList.value = editList.toCollection(ArrayList())
        checkLastItem()
    }

    fun showLongClick(positionSelect: Int) {
        _myDesignList.value = _myDesignList.value.mapIndexed { position, item ->
            item.copy(isSelected = position == positionSelect, isShowSelection = true)
        }.toCollection(ArrayList())
        checkLastItem()
    }

    private fun checkLastItem() {
        _isLastItem.value = _myDesignList.value.any { !it.isSelected }
    }

    suspend fun deleteItem(context: Context, pathList: ArrayList<String>){
        val originList = MediaHelper
            .readListFromFile<SuggestionModel>(context, ValueKey.EDIT_FILE_INTERNAL)
            .toCollection(ArrayList())

        val newOriginList = originList.filterNot { pathList.contains(it.pathInternalEdit) }.toCollection(ArrayList())

        MediaHelper.writeListToFile(context, ValueKey.EDIT_FILE_INTERNAL, newOriginList)
        MediaHelper.deleteFileByPathNotFlow(pathList)
    }

    fun toggleSelect(position: Int) {
        val list = _myDesignList.value.toMutableList()
        list[position] = list[position].copy(isSelected = !list[position].isSelected, isShowSelection = true)
        _myDesignList.value = list.toCollection(ArrayList())
        checkLastItem()
    }

    fun selectAll(shouldSelect: Boolean) {
        _myDesignList.value = _myDesignList.value.map {
            it.copy(isSelected = shouldSelect, isShowSelection = true)
        }.toCollection(ArrayList())
        checkLastItem()
    }

    fun getPathSelected() : ArrayList<String>{
        return _myDesignList.value.filter { it.isSelected }.map { it.path }.toCollection(ArrayList())
    }

    fun clearSelection() {
        _myDesignList.value = _myDesignList.value.map {
            it.copy(isSelected = false, isShowSelection = false)
        }.toCollection(ArrayList())
        checkLastItem()
    }

    suspend fun editItem(context: Context, pathInternal: String) {
        val originList = MediaHelper
            .readListFromFile<SuggestionModel>(context, ValueKey.EDIT_FILE_INTERNAL)
            .toCollection(ArrayList())

        val editModel = originList.first { it.pathInternalEdit == pathInternal }
        positionCharacter = 0 // Will be set properly when data is loaded
        MediaHelper.writeModelToFile(context, ValueKey.SUGGESTION_FILE_INTERNAL, editModel)
    }
}