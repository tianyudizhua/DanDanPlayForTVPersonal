package com.seiko.core.repo

import com.seiko.core.data.api.DanDanApiService
import com.seiko.core.data.api.ResDanDanApiService
import com.seiko.core.data.api.model.ResMagnetSearchResponse
import com.seiko.core.data.api.model.SearchAnimeResponse
import com.seiko.core.model.api.ResMagnetItem
import com.seiko.core.model.api.SearchAnimeDetails
import com.seiko.core.data.Result
import com.seiko.core.data.api.DanDanApiRemoteDataSource
import com.seiko.core.data.api.ResDanDanApiRemoteDataSource

internal class SearchRepositoryImpl(
    private val dataSource: DanDanApiRemoteDataSource,
    private val resDataSource: ResDanDanApiRemoteDataSource
) : SearchRepository {

    override suspend fun searchBangumiList(keyword: String, type: String): Result<List<SearchAnimeDetails>> {
        return dataSource.searchBangumiList(keyword, type)
    }

    override suspend fun searchMagnetList(keyword: String, typeId: Int, subGroupId: Int): Result<List<ResMagnetItem>> {
        return resDataSource.searchMagnetList(keyword, typeId, subGroupId)
    }

}