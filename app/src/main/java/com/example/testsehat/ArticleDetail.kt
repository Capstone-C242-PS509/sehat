package com.example.testsehat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.testsehat.databinding.ArticleDetailBinding

class ArticleDetailActivity : AppCompatActivity() {
    private lateinit var binding: ArticleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleId = intent.getIntExtra("ARTICLE_ID", -1)
        val title = intent.getStringExtra("ARTICLE_TITLE") ?: "Article"
        val description = intent.getStringExtra("ARTICLE_DESCRIPTION") ?: ""
        val imageUrl = intent.getStringExtra("ARTICLE_IMAGE_URL")

        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivArticleImage)

        binding.tvArticleTitle.text = title
        binding.tvArticleDescription.text = description
    }
}