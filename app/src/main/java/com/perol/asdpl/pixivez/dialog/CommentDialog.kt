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

package com.perol.asdpl.pixivez.dialog


import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Pair
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.activity.UserMActivity
import com.perol.asdpl.pixivez.adapters.CommentAdapter
import com.perol.asdpl.pixivez.objects.ThemeUtil
import com.perol.asdpl.pixivez.objects.Toasty
import com.perol.asdpl.pixivez.repository.RetrofitRepository
import com.perol.asdpl.pixivez.services.PxEZApp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
//TODO: Refactor
class CommentDialog : DialogFragment() {

    val disposables = CompositeDisposable()
    fun Disposable.add() {
        disposables.add(this)
    }

    lateinit var recyclerviewPicture: RecyclerView

    lateinit var edittextComment: TextInputEditText

    lateinit var button: Button
    private var Authorization: String? = null
    private var commentAdapter: CommentAdapter? = null
    private var id: Long? = null
    private var Parent_comment_id = 1
    private val retrofitRepository  = RetrofitRepository.getInstance()
    var compositeDisposable = CompositeDisposable()
    private var callback: Callback? = null
    var nextUrl: String? = null
    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "ViewDialogFragment")
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    private fun getData() {
        retrofitRepository.getIllustComments(id!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                    button.isClickable = true
                    commentAdapter = CommentAdapter(
                        R.layout.view_comment_item,
                        it.comments,
                        context
                    )
                    recyclerviewPicture.isNestedScrollingEnabled = false
                    recyclerviewPicture.layoutManager =
                        LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    recyclerviewPicture.adapter = commentAdapter
                    recyclerviewPicture.addItemDecoration(
                        DividerItemDecoration(
                            context,
                            DividerItemDecoration.HORIZONTAL
                        )
                    )
                    commentAdapter!!.setOnItemClickListener { adapter, view, position ->
                        val builder = MaterialAlertDialogBuilder(requireActivity())
                        val comment = it.comments[position].comment
                        builder.setMessage(comment)
                        val dialog = builder.create()
                        dialog.show()
                    }
                    commentAdapter!!.addChildClickViewIds(R.id.commentuserimage, R.id.reply_to_hit)
                    commentAdapter!!.setOnItemChildClickListener { adapter, view, position ->
                        if (view.id == R.id.commentuserimage) {
                            val intent = Intent(context, UserMActivity::class.java)
                            intent.putExtra(
                                "data",
                                it.comments[position].user.id
                            )

                            if (PxEZApp.animationEnable) {
                                val options = ActivityOptions.makeSceneTransitionAnimation(
                                    context as Activity,
                                    Pair.create(view, "UserImage")
                                )
                                startActivity(intent, options.toBundle())
                            } else
                                startActivity(intent)
                        }
                        if (view.id == R.id.reply_to_hit) {
                            Parent_comment_id = it.comments[position].id
                            edittextComment.hint =
                                getString(R.string.reply_to) + ":" + it.comments[position].user.name
                        }
                    }
                    nextUrl = it.next_url
                    commentAdapter!!.loadMoreModule.setOnLoadMoreListener {
                        if (!nextUrl.isNullOrBlank()) {

                            retrofitRepository.getNextIllustComments(
                                nextUrl!!
                            ).subscribe({
                                    commentAdapter!!.addData(it.comments)
                                    nextUrl = it.next_url
                                    commentAdapter!!.loadMoreModule.loadMoreComplete()
                                }, {
                                    commentAdapter!!.loadMoreModule.loadMoreFail()
                                    it.printStackTrace()
                                }, {

                                }, {
                                    compositeDisposable.add(it)
                                }).add()
                        } else {
                            commentAdapter!!.loadMoreModule.loadMoreEnd()
                        }
                    }
                    button.setOnClickListener { commit() }
                },{},{}).add()
    }

    fun commit() {
        retrofitRepository
            .postIllustComment(
                id!!,
                edittextComment.text.toString(),
                if (Parent_comment_id == 1) null else Parent_comment_id
            ).subscribe({
                retrofitRepository.getIllustComments(
                    id!!
                ).subscribe({
                        commentAdapter!!.setNewData(it.comments)
                        Toast.makeText(context, getString(R.string.comment_successful), Toast.LENGTH_SHORT).show()
                        edittextComment.setText("")
                        Parent_comment_id = 1
                        edittextComment.hint = ""
                    },{e->
                        if ((e as HttpException).response()!!.code() == 403) {
                            Toasty.warning(requireContext(), getString(R.string.rate_limited), Toast.LENGTH_SHORT)
                                .show()
                        } else if (e.response()!!.code() == 404) {
                        }
                    },{}) .add()
            },{},{}).add()


    }


    override fun onStart() {
        super.onStart()
        val window = dialog!!.window
        val params = window!!.attributes
        params.gravity = Gravity.BOTTOM
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = params
        window.setBackgroundDrawable(ColorDrawable(ThemeUtil.transparent))
    }


    interface Callback {
        fun onClick()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initbind()
    }

    private fun initbind() {
        getData()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        id = bundle!!.getLong("id")
        val builder = MaterialAlertDialogBuilder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_comment, null)
        recyclerviewPicture = view.findViewById(R.id.recyclerview_picture)
        edittextComment = view.findViewById(R.id.edittext_comment)
        button = view.findViewById(R.id.button)
        builder.setView(view)
        getData()
        return builder.create()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback) {
            callback = context
        } else {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
        disposables.clear()
    }

    companion object {

        fun newInstance(id: Long?): CommentDialog {
            val commentDialog = CommentDialog()
            val bundle = Bundle()
            bundle.putLong("id", id!!)
            commentDialog.arguments = bundle
            return commentDialog
        }
    }
}
