package com.eatradish.unitydemo

data class LyricInfo(
    var song_lines: ArrayList<LineInfo>, // 歌词
    var song_artist: String = "",  // 歌手
    var song_title: String = "", // 标题
    var song_album: String = "",  // 专辑
    var song_offset: Long = 0  // 偏移量
)

data class LineInfo(
    // 歌词
    var content: String = "",
    // 时间
    var start: Long = 0
)
