package com.example.testsehat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testsehat.data.dataArticle
import com.example.testsehat.databinding.ItemFeaturedArticleBinding

class FeaturedArticleAdapter(
    private val onItemClick: (dataArticle) -> Unit
) : RecyclerView.Adapter<FeaturedArticleAdapter.FeaturedArticleViewHolder>() {

    private var articles: List<dataArticle> = listOf()

    inner class FeaturedArticleViewHolder(
        private val binding: ItemFeaturedArticleBinding,
        private val onItemClick: (dataArticle) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: dataArticle) {
            binding.apply {
                tvArticleTitle.text = article.title
                tvArticleDescription.text = article.content

                Glide.with(itemView.context)
                    .load(article.url)
                    .centerCrop()
                    .into(ivArticleImage)

                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedArticleViewHolder {
        val binding = ItemFeaturedArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeaturedArticleViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: FeaturedArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    fun updateArticles(newArticles: List<dataArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}