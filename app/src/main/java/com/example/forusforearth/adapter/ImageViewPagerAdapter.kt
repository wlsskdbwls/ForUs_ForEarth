package com.example.forusforearth.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.forusforearth.databinding.ViewholderImageBinding
import com.example.forusforearth.extensions.loadCenterCrop
import kotlinx.coroutines.NonDisposableHandle
import kotlinx.coroutines.NonDisposableHandle.parent
import java.text.FieldPosition

class ImageViewPagerAdapter(
    var uriList:List<Uri>
): RecyclerView.Adapter<ImageViewPagerAdapter.ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            ViewholderImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bindDate(uriList[position])
    }

    override fun getItemCount(): Int = uriList.size

    class ImageViewHolder(
        private val binding: ViewholderImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindDate(uri: Uri) = with(binding) {
            imageView.loadCenterCrop(uri.toString())
        }
    }
}