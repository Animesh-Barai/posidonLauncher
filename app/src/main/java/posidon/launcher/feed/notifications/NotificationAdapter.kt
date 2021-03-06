package posidon.launcher.feed.notifications

import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.LauncherMenu
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.dp
import posidon.launcher.view.SwipeableLayout


class NotificationAdapter(private val context: Context, private val window: Window) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(internal val view: ViewGroup, internal val card: SwipeableLayout, internal val linearLayout: LinearLayout) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): NotificationViewHolder {
        val view = RelativeLayout(context)
        val hMargin = Settings["feed:card_margin_x", 16].dp.toInt()
        val vMargin = 9.dp.toInt()
        view.setPadding(hMargin, vMargin, hMargin, vMargin)

        val ll = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Settings["notificationbgcolor", -0x1])
        }

        val card = SwipeableLayout(ll).apply {
            setIconColor(if (ColorTools.useDarkText(Main.accentColor)) 0xff000000.toInt() else 0xffffffff.toInt())
            setSwipeColor(Main.accentColor and 0xffffff or 0xdd000000.toInt())
            preventCornerOverlap = true
            elevation = 0f
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            radius = Settings["feed:card_radius", 15].dp
        }
        view.addView(card)

        return NotificationViewHolder(view, card, ll)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, i: Int) {
        val groups = NotificationService.notificationGroups
        if (groups.size != 0) for (notificationI in groups[i].indices) {
            val notification = groups[i][notificationI]
            val retView: View
            val view: View = if (notification.isSummary) {
                holder.card.onSwipeAway = {
                    Main.instance.runOnUiThread {
                        val iter = groups[i].iterator()
                        for (n in iter) n.cancel()
                        groups.removeAt(i)
                    }
                }
                retView = LayoutInflater.from(context).inflate(R.layout.notification_normal_summary, null)
                retView
            } else {
                val v1: View
                if (notification.bigPic == null) {
                    v1 = LayoutInflater.from(context).inflate(R.layout.notification_normal, null)
                } else {
                    v1 = LayoutInflater.from(context).inflate(R.layout.notification_big_pic, null)
                    v1.findViewById<ImageView>(R.id.bigPic).setImageDrawable(notification.bigPic)
                }
                retView = SwipeableLayout(v1) {
                    try {
                        val group = groups[i]
                        group.remove(notification)
                        if (group.size == 1 && group[0].isSummary) {
                            group[0].cancel()
                            groups.removeAt(i)
                        }
                        notification.cancel()
                    }
                    catch (e: Exception) { e.printStackTrace() }
                    NotificationService.update()
                }.apply {
                    setIconColor(if (ColorTools.useDarkText(Main.accentColor)) 0xff000000.toInt() else 0xffffffff.toInt())
                    setSwipeColor(Main.accentColor and 0xffffff or 0xdd000000.toInt())
                }
                v1.apply {
                    val padding = 8.dp.toInt()
                    when {
                        notificationI == 0 || (notificationI == 1 && groups[i][0].isSummary) -> {
                            if (notificationI == groups[i].lastIndex) setPadding(padding, padding, padding, padding)
                            else setPadding(padding, padding, padding, 0)
                        }
                        notificationI == groups[i].lastIndex -> setPadding(padding, 0, padding, padding)
                        else -> setPadding(padding, 0, padding, 0)
                    }
                }
                if (notification.actions != null && Settings["notificationActionsEnabled", false]) {
                    v1.findViewById<LinearLayout>(R.id.action_list).visibility = View.VISIBLE
                    for (action in notification.actions) {
                        val a = TextView(context)
                        a.text = action.title
                        a.textSize = 14f
                        a.setTextColor(Settings["notificationActionTextColor", -0xdad9d9])
                        val r = 24.dp
                        val out = ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null))
                        out.paint.color = Settings["notificationActionBGColor", 0x88e0e0e0.toInt()]
                        a.background = out
                        val vPadding = 10.dp.toInt()
                        val hPadding = 15.dp.toInt()
                        a.setPadding(hPadding, vPadding, hPadding, vPadding)
                        v1.findViewById<LinearLayout>(R.id.action_list).addView(a)
                        (a.layoutParams as LinearLayout.LayoutParams).leftMargin = 6.dp.toInt()
                        (a.layoutParams as LinearLayout.LayoutParams).rightMargin = 6.dp.toInt()
                        a.setOnClickListener {
                            try {
                                val oldInputs = action.remoteInputs
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && oldInputs != null) {
                                    v1.findViewById<View>(R.id.bottomSeparator).visibility = View.VISIBLE
                                    v1.findViewById<View>(R.id.reply).apply lin@ {
                                        visibility = View.VISIBLE
                                        val imm = getSystemService(context, InputMethodManager::class.java)!!
                                        val textArea = findViewById<EditText>(R.id.replyText).apply {
                                            setTextColor(Settings["notificationtitlecolor", -0xeeeded])
                                            setHintTextColor(Settings["notificationtxtcolor", -0xdad9d9])
                                            requestFocus()
                                            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                                            setOnFocusChangeListener { _, hasFocus ->
                                                if (!hasFocus) {
                                                    text.clear()
                                                    this@lin.visibility = View.GONE
                                                    v1.findViewById<View>(R.id.bottomSeparator).visibility = View.GONE
                                                    imm.hideSoftInputFromWindow(windowToken, 0)
                                                }
                                            }
                                        }
                                        findViewById<ImageView>(R.id.cancel).apply {
                                            imageTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded])
                                            backgroundTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded] and 0xffffff or 0x33ffffff)
                                            setOnClickListener {
                                                textArea.text.clear()
                                                this@lin.visibility = View.GONE
                                                v1.findViewById<View>(R.id.bottomSeparator).visibility = View.GONE
                                            }
                                        }
                                        findViewById<ImageView>(R.id.replySend).apply {
                                            imageTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded])
                                            backgroundTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded] and 0xffffff or 0x33ffffff)
                                            setOnClickListener {
                                                val intent = Intent()
                                                val bundle = Bundle()
                                                val actualInputs: ArrayList<RemoteInput> = ArrayList()
                                                for (input in oldInputs) {
                                                    bundle.putCharSequence(input.resultKey, textArea.text)
                                                    val builder = RemoteInput.Builder(input.resultKey)
                                                    builder.setLabel(input.label)
                                                    builder.setChoices(input.choices)
                                                    builder.setAllowFreeFormInput(input.allowFreeFormInput)
                                                    builder.addExtras(input.extras)
                                                    actualInputs.add(builder.build())
                                                }
                                                val inputs = actualInputs.toArray(arrayOfNulls<RemoteInput>(actualInputs.size))
                                                RemoteInput.addResultsToIntent(inputs, intent, bundle)
                                                action.actionIntent.send(context, 0, intent)
                                                textArea.text.clear()
                                                this@lin.visibility = View.GONE
                                                v1.findViewById<View>(R.id.bottomSeparator).visibility = View.GONE
                                            }
                                        }
                                    }
                                } else {
                                    action.actionIntent.send()
                                }
                            }
                            catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                }
                v1
            }

            view.findViewById<TextView>(R.id.title).text = notification.title
            view.findViewById<TextView>(R.id.txt).text = notification.text

            view.findViewById<ImageView>(R.id.iconimg).setImageDrawable(notification.icon)
            view.findViewById<TextView>(R.id.title).setTextColor(Settings["notificationtitlecolor", -0xeeeded])
            view.findViewById<TextView>(R.id.txt).setTextColor(Settings["notificationtxtcolor", -0xdad9d9])

            view.setOnClickListener { notification.open() }
            view.setOnLongClickListener(LauncherMenu(context, window))
            holder.linearLayout.addView(retView)
        }
    }

    override fun getItemCount() = NotificationService.notificationGroups.size
}
