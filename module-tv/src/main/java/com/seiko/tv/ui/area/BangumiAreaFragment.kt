package com.seiko.tv.ui.area

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.leanback.widget.*
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.seiko.tv.R
import com.seiko.tv.databinding.FragmentAreaBinding
import com.seiko.tv.ui.adapter.BangumiRelateAdapter
import com.seiko.tv.ui.adapter.BangumiSeasonAdapter
import com.seiko.common.ui.adapter.OnItemClickListener
import com.seiko.tv.vm.BangumiAreaViewModel
import com.seiko.common.data.ResultData
import androidx.activity.addCallback
import androidx.activity.requireDispatchKeyEventDispatcher
import com.seiko.tv.ui.widget.SpaceItemDecoration
import com.seiko.common.util.extensions.lazyAndroid
import com.seiko.common.util.toast.toast
import com.seiko.tv.data.db.model.BangumiIntroEntity
import com.seiko.tv.data.model.HomeImageBean
import com.seiko.tv.data.model.api.BangumiSeason
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.ref.WeakReference

class BangumiAreaFragment : Fragment(),
    OnItemClickListener {

    companion object {
//        private const val GRID_VIEW_LEFT_PX = 50 //80->60
//        private const val GRID_VIEW_RIGHT_PX = 50 //50->40
        private const val GRID_VIEW_TOP_PX = 25 //30->20
        private const val GRID_VIEW_BOTTOM_PX = 25 //50->40

        private const val ITEM_TOP_PADDING_PX = 15 //15->25
        private const val ITEM_RIGHT_PADDING_PX = 25
        
        private const val ARGS_SEASON_SELECTED_POSITION = "ARGS_SEASON_SELECTED_POSITION"
        private const val ARGS_BANGUMI_SELECTED_POSITION = "ARGS_BANGUMI_SELECTED_POSITION"
    }

    private val viewModel: BangumiAreaViewModel by inject()

    private lateinit var binding: FragmentAreaBinding

    private lateinit var seasonAdapter: BangumiSeasonAdapter
    private lateinit var bangumiAdapter: BangumiRelateAdapter

    /**
     * 记录位置
     */
    private var seasonSelectedPosition: Int = -1
    private var bangumiSelectedPosition: Int = -1

    private val handler by lazyAndroid { AreaHandler(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerKeyEvent()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAreaBinding.inflate(inflater, container, false)
        setupSeason()
        setupBangumi()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkSelectPosition(savedInstanceState)
        bindViewModel()
    }

    /**
     * 注销Item选择监听
     */
    override fun onDestroyView() {
        super.onDestroyView()
        unBindViewModel()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * 保存视图状态
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("onSaveInstanceState")
        outState.putInt(ARGS_SEASON_SELECTED_POSITION, seasonSelectedPosition)
        outState.putInt(ARGS_BANGUMI_SELECTED_POSITION, bangumiSelectedPosition)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Timber.d("onViewStateRestored")
    }

    private fun checkSelectPosition(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ARGS_SEASON_SELECTED_POSITION)) {
                seasonSelectedPosition = savedInstanceState.getInt(ARGS_SEASON_SELECTED_POSITION)
            }
            if (savedInstanceState.containsKey(ARGS_BANGUMI_SELECTED_POSITION)) {
                bangumiSelectedPosition = savedInstanceState.getInt(ARGS_BANGUMI_SELECTED_POSITION)
            }
        }
        if (seasonSelectedPosition >= 0) {
            binding.gridSeason.selectedPosition = seasonSelectedPosition
            seasonAdapter.setSelectPosition(seasonSelectedPosition)
        }
        if (bangumiSelectedPosition >= 0) {
            binding.gridBangumi.selectedPosition = bangumiSelectedPosition
            binding.gridBangumi.requestFocus()
        } else {
            binding.gridSeason.requestFocus()
        }
    }

    private fun setupSeason() {
        seasonAdapter = BangumiSeasonAdapter()
        seasonAdapter.setOnItemClickListener(this)

        binding.gridSeason.setNumColumns(1)
        binding.gridSeason.setOnChildViewHolderSelectedListener(mItemSelectedListener)
        binding.gridSeason.adapter = seasonAdapter
    }

    private fun setupBangumi() {
        bangumiAdapter = BangumiRelateAdapter()
        bangumiAdapter.setOnItemClickListener(this)
        // 自动计算count，由于用到了width，需要等界面绘制完，因此在post里运行
        binding.gridBangumi.post {
            val top = ITEM_TOP_PADDING_PX
            val right = ITEM_RIGHT_PADDING_PX
            binding.gridBangumi.addItemDecoration(SpaceItemDecoration(top, right))

            // recView宽度，item宽度
            val width = binding.gridBangumi.width
            val itemWidth = requireContext().resources.getDimension(R.dimen.homeFragment_area_width).toInt()
            // 算出并排数、左右间距
            val count = width / (itemWidth + ITEM_RIGHT_PADDING_PX)
            val padding = (width % (itemWidth + ITEM_RIGHT_PADDING_PX)) / 2
            binding.gridBangumi.setPadding(padding,
                GRID_VIEW_TOP_PX, padding,
                GRID_VIEW_BOTTOM_PX
            )

            binding.gridBangumi.setNumColumns(count)
            binding.gridBangumi.setOnChildViewHolderSelectedListener(mItemSelectedListener)
            val pool = RecyclerView.RecycledViewPool()
            pool.setMaxRecycledViews(0, 100)
            binding.gridBangumi.setRecycledViewPool(pool)

            binding.gridBangumi.adapter = bangumiAdapter
        }
    }

    private fun bindViewModel() {
        viewModel.bangumiSeasons.observe(this) { seasons ->
            seasonAdapter.submitList(seasons)
            if (seasons.isNotEmpty()) {
                var position = seasonSelectedPosition
                if (position == -1 || position >= seasons.size) {
                    position = 0
                }
                viewModel.season.value = seasons[position]
//                viewModel.getBangumiListWithSeason(seasons[position], false)
            }
        }
        viewModel.bangumiList.observe(this::getLifecycle, this::updateBangumiList)
    }

    private fun unBindViewModel() {
        viewModel.bangumiSeasons.removeObservers(this)
    }

    /**
     * 加载动漫合集
     */
    private fun updateBangumiList(data: ResultData<List<HomeImageBean>>) {
        when(data) {
            is ResultData.Loading -> {
                binding.progress.visibility = View.VISIBLE
            }
            is ResultData.Error -> {
                binding.progress.visibility = View.GONE
                toast(data.exception.toString())
            }
            is ResultData.Success -> {
                binding.progress.visibility = View.GONE
                bangumiAdapter.submitList(data.data)
            }
        }
    }

    override fun onClick(holder: RecyclerView.ViewHolder, item: Any, position: Int) {
        when(item) {
            is BangumiIntroEntity -> {
//                findNavController().navigate(
//                    BangumiAreaFragmentDirections.actionBangumiAreaFragmentToBangumiDetailsFragment(
//                        item.animeId
//                    )
//                )
            }
        }
    }

    /**
     * Item选择监听回调
     */
    private val mItemSelectedListener : OnChildViewHolderSelectedListener by lazyAndroid {
        object : OnChildViewHolderSelectedListener() {
            override fun onChildViewHolderSelected(
                parent: RecyclerView?,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subposition: Int
            ) {
                when(parent?.id) {
                    R.id.grid_season -> {
                        if (seasonSelectedPosition == position) return
                        seasonSelectedPosition = position
                        seasonAdapter.setSelectPosition(position)
                        // 请求动画
                        val item = seasonAdapter.get(position) ?: return
                        handler.send(item)
                    }
                    R.id.grid_bangumi -> {
                        if (bangumiSelectedPosition == position) return
                        bangumiSelectedPosition = position
                    }
                }
            }
        }
    }

    /**
     * 请求动漫数据，Handler用
     */
    fun getBangumiListWithSeason(season: BangumiSeason) {
        viewModel.season.value = season
    }

    /**
     * 绑定按键监听到Activity
     */
    private fun registerKeyEvent() {
        requireDispatchKeyEventDispatcher().getDispatchKeyEventDispatcher()
            .addCallback(this, this::dispatchKeyEvent)
    }

    /**
     * 返回前，先把焦点给到左侧列表
     */
    private fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (!binding.gridSeason.hasFocus()) {
                binding.gridSeason.requestFocus()
                return true
            }
        }
        return false
    }

}

/**
 * 放弃左侧季度列表，快速拖动时重复请求
 */
private class AreaHandler(fragment: BangumiAreaFragment) : Handler() {

    companion object {
        private const val POST_DELAY_TIME = 500L
        private const val HANDLER_WHAT_SEASON = 100
    }

    private val reference = WeakReference(fragment)

    fun send(item: BangumiSeason) {
        removeMessages(HANDLER_WHAT_SEASON)
        val msg = obtainMessage(HANDLER_WHAT_SEASON)
        msg.obj = item
        sendMessageDelayed(msg,
            POST_DELAY_TIME
        )
    }

    override fun handleMessage(msg: Message) {
        val fragment = reference.get() ?: return
        when(msg.what) {
            HANDLER_WHAT_SEASON -> {
                val item = msg.obj as? BangumiSeason ?: return
                fragment.getBangumiListWithSeason(item)
            }
        }
    }
}