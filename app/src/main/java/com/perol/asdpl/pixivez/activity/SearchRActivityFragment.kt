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

package com.perol.asdpl.pixivez.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.adapters.TagsTextAdapter
import com.perol.asdpl.pixivez.responses.Tags
import com.perol.asdpl.pixivez.viewmodel.TagsTextViewModel
import com.perol.asdpl.pixivez.databinding.FragmentSearchRBinding
/**
 * A placeholder fragment containing a simple view.
 */
class SearchRActivityFragment : Fragment() {

    private lateinit var binding: FragmentSearchRBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
		binding = FragmentSearchRBinding.inflate(inflater, container, false)
		return binding.root
    }

    lateinit var tagsTextViewModel: TagsTextViewModel
    lateinit var tagsTextAdapter: TagsTextAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagsTextAdapter = TagsTextAdapter(R.layout.tagstext_item)
        binding.recyclerview.layoutManager = LinearLayoutManager(activity)
        binding.recyclerview.adapter = tagsTextAdapter
        tagsTextAdapter.setOnItemClickListener { adapter, view, position ->
            val s_tag = tags[position]
            if (s_tag.translated_name != null && s_tag.translated_name.isNotEmpty())
                tagsTextViewModel.addhistory(s_tag.name + "|" + s_tag.translated_name)
            else tagsTextViewModel.addhistory(s_tag.name)
            val bundle = Bundle()
            bundle.putString("searchword", tags[position].name)
            val intent = Intent(requireActivity(), SearchResultActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, 775)
        }
    }

    val tags = ArrayList<Tags>()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tagsTextViewModel =
            ViewModelProvider(requireActivity()).get(TagsTextViewModel::class.java)
        tagsTextViewModel.tags.observe(viewLifecycleOwner, Observer {
            tagsTextAdapter.setNewData(it.toMutableList())
            tags.clear()
            tags.addAll(it)
        })

    }
}
