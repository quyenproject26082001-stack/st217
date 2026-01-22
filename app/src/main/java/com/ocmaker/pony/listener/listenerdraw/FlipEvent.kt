package com.ocmaker.pony.listener.listenerdraw

import android.view.MotionEvent
import com.ocmaker.pony.core.custom.drawview.DrawView
import com.ocmaker.pony.core.utils.key.DrawKey


class FlipEvent : DrawEvent {
    override fun onActionDown(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionMove(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionUp(tattooView: DrawView?, event: MotionEvent?) {
        if (tattooView != null && tattooView.getStickerCount() > 0) tattooView.flipCurrentDraw(
            DrawKey.FLIP_HORIZONTALLY)
    }
}