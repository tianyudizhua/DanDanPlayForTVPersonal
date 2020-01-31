package com.dandanplay.tv.ui.card

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dandanplay.tv.data.model.HomeImageBean
import com.dandanplay.tv.databinding.ItemMainAreaBinding
import com.dandanplay.tv.util.diff.HomeImageBeanDiffCallback
import com.dandanplay.tv.util.loadImage
import com.seiko.common.ui.card.AbsBindingCardView

class MainAreaCardView(context: Context) : AbsBindingCardView<HomeImageBean>(context) {

    private lateinit var binding: ItemMainAreaBinding

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup) {
        binding = ItemMainAreaBinding.inflate(inflater, parent, true)
    }

    override fun bind(item: HomeImageBean) {
        binding.img.loadImage(item.imageUrl)
        binding.title.text = item.animeTitle
        binding.chapter.text = item.status
    }

    fun bind(bundle: Bundle) {
        if (bundle.containsKey(HomeImageBeanDiffCallback.ARGS_ANIME_IMAGE_URL)) {
            binding.img.loadImage(bundle.getString(HomeImageBeanDiffCallback.ARGS_ANIME_IMAGE_URL)!!)
        }
        if (bundle.containsKey(HomeImageBeanDiffCallback.ARGS_ANIME_TITLE)) {
            binding.title.text = bundle.getString(HomeImageBeanDiffCallback.ARGS_ANIME_TITLE)
        }
        if (bundle.containsKey(HomeImageBeanDiffCallback.ARGS_ANIME_STATUS)) {
            binding.chapter.text = bundle.getString(HomeImageBeanDiffCallback.ARGS_ANIME_STATUS)
        }
    }

}