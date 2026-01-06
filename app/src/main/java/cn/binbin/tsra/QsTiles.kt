package cn.binbin.tsra

import android.service.quicksettings.TileService

class QsTile120Service : TileService() {
    override fun onClick() {
        super.onClick()
        TouchRunner.run(applicationContext, 0)
    }
}

class QsTile180Service : TileService() {
    override fun onClick() {
        super.onClick()
        TouchRunner.run(applicationContext, 180)
    }
}

class QsTile240Service : TileService() {
    override fun onClick() {
        super.onClick()
        TouchRunner.run(applicationContext, 240)
    }
}

class QsTile360Service : TileService() {
    override fun onClick() {
        super.onClick()
        TouchRunner.run(applicationContext, 360)
    }
}
