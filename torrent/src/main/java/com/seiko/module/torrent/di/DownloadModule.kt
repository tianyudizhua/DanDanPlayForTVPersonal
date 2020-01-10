package com.seiko.module.torrent.di

import com.seiko.data.repo.TorrentRepository
import com.seiko.data.domain.torrent.GetTorrentInfoFileUseCase
import com.seiko.module.torrent.service.DownloadManager
import com.seiko.module.torrent.service.Downloader
import com.seiko.torrent.TorrentEngineOptions
import org.koin.dsl.module

internal val downloadModule = module {
    single { createDownloader(get(), get(), get()) }
}

private fun createDownloader(
    options: TorrentEngineOptions,
    torrentRepo: TorrentRepository,
    getTorrentInfoFileUseCase: GetTorrentInfoFileUseCase
): Downloader {
    return DownloadManager(
        options = options,
        torrentRepo = torrentRepo,
        getTorrentInfoFileUseCase = getTorrentInfoFileUseCase
    )
}