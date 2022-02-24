package com.androiddevs.shoppinglisttestingyt.data.remote.responses

data class ImageResponse(
    val imageResults: List<ImageResult>,
    val total: Int,
    val totalHits: Int
)