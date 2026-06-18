package com.example.passpoint.feature.bookmark.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passpoint.feature.bookmark.data.BookmarkRepository
import com.example.passpoint.feature.bookmark.data.dto.response.BookmarkSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarkListViewModel : ViewModel() {

    private val bookmarkRepository = BookmarkRepository()

    private val _uiState = MutableStateFlow<BookmarkListUiState>(BookmarkListUiState.Loading)
    val uiState: StateFlow<BookmarkListUiState> = _uiState.asStateFlow()

    private val loadedBookmarks = mutableListOf<BookmarkSummaryResponse>()
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    init {
        loadFirstPage()
    }

    private fun loadFirstPage() {
        currentPage = 0
        isLastPage = false
        loadedBookmarks.clear()
        _uiState.value = BookmarkListUiState.Loading

        viewModelScope.launch {
            isLoading = true
            try {
                val pageData = bookmarkRepository.getList(page = 0)
                loadedBookmarks.addAll(pageData.content)
                isLastPage = pageData.last
                _uiState.value = BookmarkListUiState.Success(
                    bookmarks = loadedBookmarks.toList(),
                    isLastPage = isLastPage
                )
            } catch (e: Exception) {
                _uiState.value = BookmarkListUiState.Error(e.message ?: "불러오기 실패")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || isLastPage) return
        val currentState = _uiState.value as? BookmarkListUiState.Success ?: return

        viewModelScope.launch {
            isLoading = true
            _uiState.value = currentState.copy(isLoadingMore = true)
            try {
                val nextPage = currentPage + 1
                val pageData = bookmarkRepository.getList(page = nextPage)
                loadedBookmarks.addAll(pageData.content)
                currentPage = nextPage
                isLastPage = pageData.last
                _uiState.value = currentState.copy(
                    bookmarks = loadedBookmarks.toList(),
                    isLoadingMore = false,
                    isLastPage = isLastPage
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(isLoadingMore = false)
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh() = loadFirstPage()

    /**
     * 목록에서 먼저 지우고(낙관적 업데이트) 서버 요청이 실패하면 원래 자리로 되돌린다.
     */
    fun removeBookmark(questionId: Long) {
        val currentState = _uiState.value as? BookmarkListUiState.Success ?: return
        val removedIndex = loadedBookmarks.indexOfFirst { it.questionId == questionId }
        if (removedIndex == -1) return

        val removed = loadedBookmarks.removeAt(removedIndex)
        _uiState.value = currentState.copy(bookmarks = loadedBookmarks.toList())

        viewModelScope.launch {
            try {
                bookmarkRepository.remove(questionId)
            } catch (e: Exception) {
                loadedBookmarks.add(removedIndex, removed)
                val latestState = _uiState.value as? BookmarkListUiState.Success ?: currentState
                _uiState.value = latestState.copy(bookmarks = loadedBookmarks.toList())
            }
        }
    }
}
