/*
 * Copyright (c) 2019 Leo Shneyderis
 * All rights reserved
 */

package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.ColorTools
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting

class CustomSearch : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_search)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<Switch>(R.id.drawersearchbar).isChecked = Settings["drawersearchbarenabled", true]
        findViewById<View>(R.id.searchcolorprev).background = ColorTools.colorCircle(Settings["searchcolor", 0x33000000])
        findViewById<View>(R.id.searchtxtcolorprev).background = ColorTools.colorCircle(Settings["searchtxtcolor", -0x1])
        findViewById<SeekBar>(R.id.searchradiusslider).progress = Settings["searchradius", 0]

        findViewById<Switch>(R.id.docksearchbar).isChecked = Settings["docksearchbarenabled", false]
        findViewById<View>(R.id.docksearchcolorprev).background = ColorTools.colorCircle(Settings["docksearchcolor", -0x22000001])
        findViewById<View>(R.id.docksearchtxtcolorprev).background = ColorTools.colorCircle(Settings["docksearchtxtcolor", -0x1000000])
        findViewById<SeekBar>(R.id.docksearchradiusslider).progress = Settings["dock:search:radius", 30]
        findViewById<Switch>(R.id.dockSearchBarBelowAppsSwitch).isChecked = Settings["dock:search:below_apps", false]

        findViewById<View>(R.id.uiBgColorPrev).background = ColorTools.colorCircle(Settings["searchUiBg", -0x78000000])
        findViewById<View>(R.id.searchhintcolorprev).background = ColorTools.colorCircle(Settings["searchhintcolor", -0x1])
        findViewById<TextView>(R.id.hinttxt).text = Settings["searchhinttxt", "Search.."]
    }

    fun picksearchcolor(v: View) = ColorTools.pickColor(this, Settings["searchcolor", 0x33000000]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Main.setDrawerSearchbarBGColor(it)
    }
    fun picksearchtxtcolor(v: View) = ColorTools.pickColor(this, Settings["searchtxtcolor", -0x1]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Main.setDrawerSearchbarFGColor(it)
    }
    fun pickdocksearchcolor(v: View) = ColorTools.pickColor(this, Settings["docksearchcolor", -0x22000001]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Main.setDockSearchbarBGColor(it)
    }
    fun pickdocksearchtxtcolor(v: View) = ColorTools.pickColor(this, Settings["docksearchtxtcolor", -0x1000000]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Main.setDockSearchbarFGColor(it)
    }
    fun picksearchhintcolor(v: View) = ColorTools.pickColor(this, Settings["searchhintcolor", -0x1]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["searchhintcolor"] = it
    }
    fun pickSearchUiBgColor(v: View) = ColorTools.pickColor(this, Settings["searchUiBg", -0x78000000]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["searchUiBg"] = it
    }

    override fun onPause() {
        Main.setDrawerSearchbarRadius(findViewById<SeekBar>(R.id.searchradiusslider).progress)
        Main.setDockSearchbarRadius(findViewById<SeekBar>(R.id.docksearchradiusslider).progress)
        Main.setDrawerSearchBarVisible(findViewById<Switch>(R.id.drawersearchbar).isChecked)
        Main.setDockSearchBarVisible(findViewById<Switch>(R.id.docksearchbar).isChecked)
        Main.setDockSearchbarBelowApps(findViewById<Switch>(R.id.dockSearchBarBelowAppsSwitch).isChecked)
        Main.setSearchHintText(findViewById<TextView>(R.id.hinttxt).text.toString())
        super.onPause()
    }
}