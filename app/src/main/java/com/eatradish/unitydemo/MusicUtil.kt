package com.eatradish.unitydemo

import android.media.MediaPlayer
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


/**
 * 初始化歌词信息
 * @param inputStream  歌词文件的流信息
 * */
fun setupLyricResource(inputStream: InputStream, charsetName: String): LyricInfo {
    val lyricInfo = LyricInfo(arrayListOf())
    try {
        val inputStreamReader = InputStreamReader(inputStream, charsetName)
        val reader = BufferedReader(inputStreamReader);
        var line: String? = ""
        while (line != null) {
            analyzeLyric(lyricInfo, line)
            line = reader.readLine()
            Log.d("suihw", "setupLyricResource: $line")
        }
        reader.close();
        inputStream.close();
        inputStreamReader.close();
    } catch (e: Exception) {
        e.printStackTrace();
    }

    return lyricInfo
}

/**
 * 逐行解析歌词内容
 * */
private fun analyzeLyric(lyricInfo: LyricInfo, line: String) {
    if (line.isEmpty()) return
    val index = line.lastIndexOf("]")
    if (line.startsWith("[offset:")) {
        // 时间偏移量
        val string = line.substring(8, index).trim()
        lyricInfo.song_offset = string.toLong()
        return
    }
    if (line.startsWith("[ti:")) {
        // title 标题
        val string = line.substring(4, index).trim()
        lyricInfo.song_title = string
        return
    }
    if (line.startsWith("[ar:")) {
        // artist 作者
        val string = line.substring(4, index).trim()
        lyricInfo.song_artist = string
        return
    }
    if (line.startsWith("[al:")) {
        // album 所属专辑
        val string = line.substring(4, index).trim();
        lyricInfo.song_album = string
        return
    }
    if (line.startsWith("[by:")) {
        return
    }
    if (index == 10 && line.trim().length > 11) {
        // 歌词内容
        val lineInfo = LineInfo()
        lineInfo.content = line.substring(11, line.length)
        lineInfo.start = measureStartTimeMillis(line.substring(0, 10));
        lyricInfo.song_lines.add(lineInfo);
    }
}

/**
 * 从字符串中获得时间值
 * */
private fun measureStartTimeMillis(str: String): Long {
    val minute = str.substring(1, 3).toLong()
    val second = str.substring(4, 6).toLong()
    val millisecond = str.substring(7, 9).toLong()
    return millisecond + second * 1000 + minute * 60 * 1000;
}


fun getCurrentPosition(player: MediaPlayer, lrcBeanList: List<LineInfo>): Int {
    var currentPosition = 0
    val curTime = player.currentPosition
    //如果当前的时间大于10分钟，证明歌曲未播放，则当前位置应该为0
    if (curTime < lrcBeanList[0].start || curTime > 10 * 60 * 1000) {
        return 0
    } else if (curTime > lrcBeanList[lrcBeanList.size - 1].start) {
        return lrcBeanList.size - 1
    }

    lrcBeanList.forEachIndexed { index, _ ->
        if (curTime >= lrcBeanList[index].start) {
            currentPosition = index
        }
    }
    return currentPosition
}