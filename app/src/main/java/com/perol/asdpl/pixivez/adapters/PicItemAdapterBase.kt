/*
 * MIT License
 *
 * Copyright (c) 2020 ultranity
 * Copyright (c) 2019 Perol_Notsfsssf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.perol.asdpl.pixivez.adapters

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.activity.PictureActivity
import com.perol.asdpl.pixivez.objects.DataHolder
import com.perol.asdpl.pixivez.objects.IllustFilter
import com.perol.asdpl.pixivez.objects.InteractionUtil
import com.perol.asdpl.pixivez.objects.ThemeUtil
import com.perol.asdpl.pixivez.responses.Illust
import com.perol.asdpl.pixivez.services.GlideApp
import com.perol.asdpl.pixivez.services.PxEZApp
import com.perol.asdpl.pixivez.services.Works
import kotlin.math.max
import kotlin.math.min

// basic Adapter for image item
//TODO: reuse more code
abstract class PicItemAdapterBase(
    layoutResId: Int,
    data: List<Illust>?,
    val filter: IllustFilter
) :
    BaseQuickAdapter<Illust, BaseViewHolder>(layoutResId, data?.toMutableList()), LoadMoreModule{

    var colorPrimary: Int = R.color.colorPrimary
    var colorPrimaryDark: Int = R.color.colorPrimaryDark
    var colorTransparent: Int = ThemeUtil.halftrans
    var badgeTextColor: Int = R.color.yellow
    var quality = 0
    fun x_restrict(item: Illust): String{
        return if (PxEZApp.R18Private && item.x_restrict == 1) {
            "private"
        } else {
            "public"
        }
    }

    private fun setFullSpan(holder: RecyclerView.ViewHolder, isFullSpan:Boolean ) {
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = isFullSpan
        }
    }
    
    fun loadMoreEnd() {
        this.loadMoreModule.loadMoreEnd()
    }

    fun loadMoreComplete() {
        this.loadMoreModule.loadMoreComplete()
    }

    fun loadMoreFail() {
        this.loadMoreModule.loadMoreFail()
    }

    private fun setAction(CollectMode:Int){
        if (CollectMode == 2) {
            setOnItemClickListener { adapter, view, position ->
                (adapter.data as ArrayList<Illust>)[position].let {item ->
                    Works.imageDownloadAll(item)
                    setUIDownload(1, position)
                    if (!item.is_bookmarked){
                        InteractionUtil.like(item){ setUILike(true, position) }
                    }
                    if (!item.user.is_followed) {
                        InteractionUtil.follow(item){ setUIFollow(true, position) }
                    }
                }
            }
            setOnItemLongClickListener { _, view, position ->
                viewPics(view, position)
                true
            }
        }
        else {
            setOnItemClickListener { _, view, position ->
                viewPics(view, position)
            }
            setOnItemLongClickListener { adapter, view, position ->
                //show detail of illust
                (adapter.data as ArrayList<Illust>)[position].let { item ->
                    val detailstring = InteractionUtil.toDetailString(item)
                    MaterialAlertDialogBuilder(context as Activity)
                        .setMessage(detailstring)
                        .setTitle("Detail")
                        .setPositiveButton(R.string.save) { _, _ ->
                            Works.imageDownloadAll(item)
                            setUIDownload(1, position)
                        }
                        .setNeutralButton(R.string.like){ _, _ ->
                            InteractionUtil.like(item){ setUILike(true, position) }
                        }
                        .setNegativeButton(R.string.follow){ _, _ ->
                            InteractionUtil.follow(item){ setUIFollow(true, position) }
                        }
                        .create().show()
                }
                true
            }
        }
    }

    open fun viewPics(view: View, position: Int) {
        val bundle = Bundle()
        DataHolder.setIllustsList(
            this.data.subList(
                max(position - 30, 0), min(
                    this.data.size,
                    max(position - 30, 0) + 60
                )
            )
        )
        bundle.putInt("position", position - max(position - 30, 0))
        bundle.putLong("illustid", this.data[position].id)
        val intent = Intent(context, PictureActivity::class.java)
        intent.putExtras(bundle)
        if (PxEZApp.animationEnable) {
            val mainimage = view.findViewById<View>(R.id.item_img)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                context as Activity,
                Pair.create(mainimage, "mainimage")
            )
            ContextCompat.startActivity(context, intent, options.toBundle())
        } else
            ContextCompat.startActivity(context, intent, null)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addFooterView(LayoutInflater.from(context).inflate(R.layout.foot_list, null))
        setAnimationWithDefault(AnimationType.ScaleIn)
        animationEnable = PxEZApp.animationEnable
        this.loadMoreModule.preLoadNumber = 12
        colorPrimary = ThemeUtil.getColor(context, androidx.appcompat.R.attr.colorPrimary)
        colorPrimaryDark= ThemeUtil.getColor(context, androidx.appcompat.R.attr.colorPrimaryDark)
        badgeTextColor= ThemeUtil.getColor(context, com.google.android.material.R.attr.badgeTextColor)
        setAction(PxEZApp.CollectMode)
        quality = PreferenceManager.getDefaultSharedPreferences(context).getString("quality","0")?.toInt()?: 0
    }

    override fun convert(holder: BaseViewHolder, item: Illust) {
        if (filter.needHide(item) || filter.needBlock(item)) {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams.apply {
                height = 0
                width = 0
            }
            return
        }

        holder.itemView.visibility = View.VISIBLE
        holder.itemView.layoutParams.apply {
            height = LinearLayout.LayoutParams.WRAP_CONTENT
            width = LinearLayout.LayoutParams.MATCH_PARENT
        }
        //if (context.resources.configuration.orientation==1)
            setFullSpan(holder, (1.0*item.width/item.height > 2.1))

        val numLayout =
            holder.itemView.findViewById<View>(R.id.layout_num)
        numLayout.visibility = View.INVISIBLE
        when (item.type) {
            "illust" -> if (item.meta_pages.isNotEmpty()) {
                holder.setText(R.id.textview_num, item.meta_pages.size.toString())
                numLayout.visibility = View.VISIBLE
            }
            "ugoira" -> {
                holder.setText(R.id.textview_num, "GIF")
                numLayout.visibility = View.VISIBLE
            }
            else -> {
                holder.setText(R.id.textview_num, "C"+item.meta_pages.size.toString())
                numLayout.visibility = View.VISIBLE
            }
        }
        val mainImage = holder.getView<ImageView>(R.id.item_img)
        mainImage.setTag(R.id.tag_first, item.image_urls.medium)

        // Load Images
        val needSmall = if(quality == 1)
                            (1.0*item.height/item.width > 3) ||(item.width/item.height > 4)
                        else
                            item.height > 1800
        val loadUrl = if (needSmall) {
            item.image_urls.square_medium
        }
        else {
            item.image_urls.medium
        }
        //val isr18 = tags.contains("R-18") || tags.contains("R-18G")
        if (!filter.R18on && item.x_restrict == 1) {
                GlideApp.with(mainImage.context)
                    .load(R.drawable.h).transition(withCrossFade())
                    .placeholder(R.drawable.h)
                    .into(mainImage)
            }
        else {
            GlideApp.with(mainImage.context).load(loadUrl).transition(withCrossFade())
                .placeholder(ColorDrawable(ThemeUtil.halftrans))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(ContextCompat.getDrawable(mainImage.context, R.drawable.ai))
                .into(object : ImageViewTarget<Drawable>(mainImage) {
                    override fun setResource(resource: Drawable?) {
                        mainImage.setImageDrawable(resource)
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        if (mainImage.getTag(R.id.tag_first) === item.image_urls.medium) {
                            super.onResourceReady(resource, transition)
                        }

                    }
                })
        }
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener, recyclerView: RecyclerView?) {
        this.loadMoreModule.setOnLoadMoreListener(onLoadMoreListener)
    }

    override fun addData(newData: Collection<Illust>) {
        super.addData(newData)
        DataHolder.pictureAdapter?.notifyDataSetChanged().also{
            DataHolder.pictureAdapter=null
        }
    }

    fun getViewByAdapterPosition(position: Int, @IdRes viewId: Int): View? {
        return getViewByPosition(position+headerLayoutCount, viewId)
    }
    abstract fun setUILike(status: Boolean, position: Int)
    abstract fun setUIFollow(status: Boolean, position: Int)
    abstract fun setUIDownload(status:Int, position: Int)

    abstract fun setUILike(status: Boolean, view: View)
    abstract fun setUIFollow(status: Boolean, view: View)
    abstract fun setUIDownload(status:Int, view: View)
}
