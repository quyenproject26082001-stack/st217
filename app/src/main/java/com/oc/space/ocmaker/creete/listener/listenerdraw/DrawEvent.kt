package com.oc.space.ocmaker.creete.listener.listenerdraw

import android.view.MotionEvent
import com.oc.space.ocmaker.creete.core.custom.drawview.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}