package com.perol.asdpl.pixivez.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.chip.Chip
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.objects.AdapterRefreshEvent
import com.perol.asdpl.pixivez.sql.entity.BlockTagEntity
import com.perol.asdpl.pixivez.viewmodel.BlockViewModel
import com.perol.asdpl.pixivez.databinding.FragmentBlockTagBinding
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlockTagFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlockTagFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var viewModel: BlockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        viewModel = ViewModelProvider(this).get(BlockViewModel::class.java)
    }

    @SuppressLint("SetTextI18n")
    private fun getChip(blockTagEntity: BlockTagEntity): Chip {
        val chip = Chip(requireContext())
        val paddingDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 5f,
            resources.displayMetrics
        ).toInt()
        chip.text = "${blockTagEntity.name} ${blockTagEntity.translateName}"

        chip.setOnLongClickListener {
            runBlocking {
                viewModel.deleteSingleTag(blockTagEntity)
                getTagList()
            }
            EventBus.getDefault().post(AdapterRefreshEvent())
            true
        }
        return chip
    }
    private lateinit var binding:FragmentBlockTagBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentBlockTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        getTagList()
    }

    private fun getTagList() {
        runBlocking {
            val it = viewModel.getAllTags()
            binding.chipgroup.removeAllViews()
            it.forEach { v ->
                binding.chipgroup.addView(getChip(v))
            }
            val chip = Chip(requireContext())
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5f,
                resources.displayMetrics
            ).toInt()
            chip.text = "+"
            chip.setOnClickListener {
                MaterialDialog(requireContext()).show {
                    title(R.string.block_tag)
                    input { dialog, text ->
                        if (text.isBlank()) return@input
                        runBlocking {
                            viewModel.insertBlockTag(
                                BlockTagEntity(
                                    text.toString(),
                                    text.toString()
                                )
                            )
                            getTagList()
                        }

                        EventBus.getDefault().post(AdapterRefreshEvent())
                    }
                    positiveButton()
                    negativeButton()
                }
            }
            binding.chipgroup.addView(chip)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlockTagFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BlockTagFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
