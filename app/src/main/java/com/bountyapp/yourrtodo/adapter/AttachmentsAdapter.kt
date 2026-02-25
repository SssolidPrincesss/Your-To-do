package com.bountyapp.yourrtodo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bumptech.glide.Glide
import java.io.File

class AttachmentsAdapter(
    private val attachments: MutableList<String>
) : RecyclerView.Adapter<AttachmentsAdapter.ViewHolder>() {

    var onDelete: ((String) -> Unit)? = null
    var onItemClick: ((String) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.iv_attachment_thumbnail)
        val name: TextView = itemView.findViewById(R.id.tv_attachment_name)
        val delete: ImageView = itemView.findViewById(R.id.iv_delete_attachment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attachment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = attachments[position]
        val fileName = File(path).name
        holder.name.text = fileName

        if (isImageFile(path)) {
            holder.thumbnail.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(File(path))
                .centerCrop()
                .override(100, 100) // размер миниатюры
                .into(holder.thumbnail)
        } else {
            holder.thumbnail.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick?.invoke(path) }
        holder.delete.setOnClickListener { onDelete?.invoke(path) }
    }

    override fun getItemCount() = attachments.size

    private fun isImageFile(path: String): Boolean {
        val extension = File(path).extension.lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }
}