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

package com.perol.asdpl.pixivez.fragments


import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.activity.UserFollowActivity
import com.perol.asdpl.pixivez.databindingadapter.GlideLoadImage
import com.perol.asdpl.pixivez.responses.UserDetailResponse
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import kotlinx.android.synthetic.main.fragment_user_message.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [UserMessageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserMessageFragment : Fragment() {


    // TODO: Rename and change types of parameters
    private var mParam1: UserDetailResponse? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initdata()
    }

    private fun initdata() {
        textView_tacomment.autoLinkMask = Linkify.WEB_URLS
        if (mParam1!!.user != null || mParam1!!.user.comment != "")
            textView_tacomment!!.text = mParam1!!.user.comment
        else
            textView_tacomment!!.text = "~"
        GlideLoadImage(imageview_user_bg,mParam1!!.profile.background_image_url)
        val mInflater = LayoutInflater.from(requireActivity())
        textView_user_id!!.text = mParam1!!.user.id.toString()
        textView_fans!!.text = mParam1!!.profile.total_mypixiv_users.toString()
        textView_fans!!.setOnClickListener {
            val intent = Intent(requireActivity().applicationContext, UserFollowActivity::class.java)
            val bundle = Bundle()
            bundle.putLong("user", mParam1!!.user.id.toLong())
            bundle.putBoolean("isfollower", true)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        textView5!!.text = mParam1!!.profile.total_follow_users.toString()
        textView5!!.setOnClickListener { BreaktoUserFollow(mParam1!!.user.id.toLong()) }
        val strings = ArrayList<String>()
        strings.add("twitter@" + mParam1!!.profile.twitter_account)
        strings.add("pawoo")
        strings.add("ta的作品" + mParam1!!.profile.total_illusts)
        strings.add("ta的收藏" + mParam1!!.profile.total_illust_bookmarks_public)
        strings.add(mParam1!!.profile.gender)
        strings.add(mParam1!!.profile.birth)
        strings.add(mParam1!!.profile.region)
        strings.add(mParam1!!.profile.job)
        strings.add(mParam1!!.workspace.tool)
        strings.add(mParam1!!.workspace.tablet)
        strings.add(mParam1!!.workspace.printer)
        strings.add(mParam1!!.workspace.monitor)
        strings.add(mParam1!!.workspace.chair)
        val iterator = strings.iterator()
        val removelist = ArrayList<String>()
        while (iterator.hasNext()) {
            val k = iterator.next()
            if (k == " " || k == "") {
                removelist.add(k)
            }
        }
        strings -= removelist
        if (strings.size <= 2) strings.add("╮(╯▽╰)╭")
        search_page_flowlayout!!.setOnTagClickListener(object : TagFlowLayout.OnTagClickListener {
            override fun onTagClick(view: View, position: Int, parent: FlowLayout): Boolean {
                when (position) {
                    0 -> {
                        run {
                            if (mParam1!!.profile.twitter_url == null)
                                return true
                            else if (mParam1!!.profile.twitter_url.isNotBlank()) {
                                val uri = Uri.parse(mParam1!!.profile.twitter_url)
                                val intent = Intent()
                                intent.action = Intent.ACTION_VIEW
                                intent.data = uri
                                startActivity(intent)
                            }
                        }
//                        run {
//                            if (mParam1!!.profile.pawoo_url == null)
//                                return true
//                            else if (mParam1!!.profile.pawoo_url.isNotBlank()) {
//                                val uri = Uri.parse(mParam1!!.profile.pawoo_url)
//                                val intent = Intent()
//                                intent.action = Intent.ACTION_VIEW
//                                intent.data = uri
//                                startActivity(intent)
//                            }
//
//
//                        }
                    }
                    1 -> {
                        if (mParam1!!.profile.pawoo_url == null)
                            return true
                        else if (mParam1!!.profile.pawoo_url != "") {
                            val uri = Uri.parse(mParam1!!.profile.pawoo_url)
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data = uri
                            startActivity(intent)
                        }
                    }
                }
                return true
            }
        })
        search_page_flowlayout!!.adapter = object : TagAdapter<String>(strings) {
            override fun getView(parent: FlowLayout, position: Int, s: String): View {
                when (position) {
                    0 -> {
                        val tv = mInflater.inflate(R.layout.picture_tag_single, search_page_flowlayout, false) as TextView
                        tv.text = s
                        tv.setTextColor(Color.BLUE)
                        return tv
                    }
                    1 -> {
                        val tv = mInflater.inflate(R.layout.picture_tag_single, search_page_flowlayout, false) as TextView
                        tv.text = s
                        tv.setTextColor(Color.YELLOW)
                        return tv
                    }
                    else -> {
                        val tv = mInflater.inflate(R.layout.picture_tag_single, search_page_flowlayout, false) as TextView
                        tv.text = s
                        return tv
                    }
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getSerializable(ARG_PARAM1) as UserDetailResponse

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_user_message, container, false)
    }


    private fun BreaktoUserFollow(userid: Long?) {
        val intent = Intent(activity, UserFollowActivity::class.java)
        val bundle = Bundle()
        bundle.putLong("user", userid!!)
        bundle.putBoolean("isfollower", false)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment UserMessageFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: UserDetailResponse): Fragment {
            val fragment = UserMessageFragment()
            val args = Bundle()
            args.putSerializable(ARG_PARAM1, param1)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
