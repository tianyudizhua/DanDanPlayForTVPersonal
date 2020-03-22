package com.seiko.player.media.vlc.control

import android.net.Uri
import com.seiko.player.data.comments.VideoHistoryRepository
import com.seiko.player.data.db.model.VideoHistory
import com.seiko.player.data.model.PlayParam
import com.seiko.player.data.prefs.PrefDataSource
import com.seiko.player.media.vlc.util.MediaWrapperList
import com.seiko.player.util.constants.PLAYER_MIN_SAVE_POSITION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.videolan.libvlc.MediaPlayer
import org.videolan.medialibrary.MLServiceLocator
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.medialibrary.interfaces.media.MediaWrapper
import timber.log.Timber
import java.io.File

//@ExperimentalCoroutinesApi
class VlcPlayerListManager(
    private val player: VlcPlayerController,
    private val historyRepo: VideoHistoryRepository,
    private val prefDataSource: PrefDataSource
) : IPlayerController by player
    , MediaPlayer.EventListener
    , MediaWrapperList.EventListener {

    companion object {
        const val EVENT_RATE_CHANGE = 0x1000
    }

    private class Event(type: Int, value: Float) : MediaPlayer.Event(type, value)

    val mediaPlayer get() = player.mediaPlayer

    val seekable get() = player.seekable

    val pausable get() = player.pausable

    val rate get() = prefDataSource.videoRate

    /**
     * 播放列表
     */
    private val mediaList = MediaWrapperList()
    private var currentIndex = -1

    /**
     * 外部MediaListener
     */
    private var listener: MediaPlayer.EventListener? = null

    private fun hasMedia(): Boolean {
        return mediaList.size != 0
    }

    private fun isValidPosition(position: Int): Boolean {
        return position in 0 until mediaList.size
    }

    override fun setRate(rate: Float) {
        player.setRate(rate)
        prefDataSource.videoRate = rate
        listener?.onEvent(Event(EVENT_RATE_CHANGE, rate))
    }

    suspend fun load(param: PlayParam, listener: MediaPlayer.EventListener? = null) {
        val uri = Uri.parse(param.videoPath)
        var media = Medialibrary.getInstance().getMedia(uri)
        if (media == null) {
            media = MLServiceLocator.getAbstractMediaWrapper(uri)
        }
        load(listOf(media), 0, listener)

        GlobalScope.launch(Dispatchers.IO) {
            saveVideoHistory(param, media)
        }
    }

    suspend fun load(list: List<MediaWrapper>, position: Int, listener: MediaPlayer.EventListener? = null) {
        this.listener = listener
        mediaList.removeEventListener(this)
        mediaList.replaceWith(list)
        mediaList.addEventListener(this)

        // 设置视频播放速度
        val rate = prefDataSource.videoRate
        if (rate != 1.0f) {
            player.setRate(rate)
            listener?.onEvent(Event(EVENT_RATE_CHANGE, rate))
        }

        playIndex(position)
    }

    private suspend fun playIndex(position: Int) {
        if (!hasMedia()) {
            Timber.w("Warning: empty media list, nothing to play !")
            return
        }

        currentIndex = if (isValidPosition(position)) position else {
            Timber.w("Warning: index $position out of bounds")
            0
        }

        val mw = mediaList.getMedia(position)
        if (mw == null) {
            Timber.w("Warning: index $position media is null")
            return
        }

        val isVideoPlaying = mw.type == MediaWrapper.TYPE_VIDEO && player.isVideoPlaying()
        if (isVideoPlaying) {
            mw.addFlags(MediaWrapper.MEDIA_VIDEO)
        }

        var uri = mw.uri
        val title = mw.getMetaLong(MediaWrapper.META_TITLE)
        if (title > 0) uri = Uri.parse("$uri#$title")
        val chapter = mw.getMetaLong(MediaWrapper.META_CHAPTER)
        if (chapter > 0) uri = Uri.parse("$uri:$chapter")
        val start = getStartTime(mw)
        player.startPlayback(uri, this, start)
    }

    override fun play() {
        if (hasMedia()) {
            player.play()
        }
    }

    override fun stop() {
        GlobalScope.launch(Dispatchers.IO) {
            val mw = getCurrentMedia() ?: return@launch
            saveMediaMeta(mw)
        }
        mediaList.clear()
    }

    override suspend fun release() {
        listener = null
        mediaList.clear()
        player.release()
    }

    /**
     * 保存视频播放历史
     */
    private suspend fun saveVideoHistory(param: PlayParam, media: MediaWrapper) {

        historyRepo.saveVideoHistory(VideoHistory(
            videoMd5 = param.videoMd5,
            videoPath = param.videoPath,
            videoTitle = media.title,
            videoThumbnail = media.artworkMrl ?: "",
            videoSize = File(param.videoPath).length(),
            videoDuration = media.length))
    }

    /**
     * 保存视频播放进度
     */
    private suspend fun saveMediaMeta(mw: MediaWrapper) {
        var position = getCurrentPosition()
        // 少于5s，不保存进度
        if (position < PLAYER_MIN_SAVE_POSITION) {
            position = 0
        } else {
            val duration = getCurrentDuration()
            val progress = position / duration.toFloat()
            // 播放超过95%或还剩10s，不保存进度
            if (progress > 0.95f || duration - position < 10000) {
                position = 0
            }
        }
        historyRepo.savePosition(mw.uri.path!!, position)
    }

    private suspend fun getStartTime(mw: MediaWrapper): Long {
        return historyRepo.getPosition(mw.uri.path)
    }

    private fun getCurrentMedia() = mediaList.getMedia(currentIndex)

    override fun onEvent(event: MediaPlayer.Event?) {
        listener?.onEvent(event)
    }

    override fun onItemAdded(index: Int, mrl: String) {

    }

    override fun onItemRemoved(index: Int, mrl: String) {

    }

    override fun onItemMoved(indexBefore: Int, indexAfter: Int, mrl: String) {

    }

}